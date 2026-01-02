package com.cointracker.mobile.data

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

class FirestoreRepository {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val usersRef get() = db.collection("users")
    private val userDataRef get() = db.collection("user_data")
    private val hasher = WerkzeugPasswordHasher()

    suspend fun register(username: String, password: String): Result<Unit> = runCatching {
        val usernameLower = username.lowercase().trim()
        val existing = usersRef.whereEqualTo("username_lower", usernameLower).limit(1).get().await()
        if (!existing.isEmpty) throw IllegalStateException("Username already exists")

        val userId = UUID.randomUUID().toString()
        val hash = hasher.hash(password)

        usersRef.document(userId).set(
            mapOf(
                "username" to username,
                "username_lower" to usernameLower,
                "password_hash" to hash,
                "created_at" to nowIso(),
                "role" to "user"
            )
        ).await()

        val profileData = mapOf(
            "transactions" to emptyList<Map<String, Any>>(),
            "settings" to settingsToMap(defaultSettings()),
            "last_updated" to nowIso()
        )
        userDataRef.document(userId).set(
            mapOf(
                "profiles" to mapOf("Default" to profileData),
                "last_active_profile" to "Default"
            )
        ).await()
    }

    suspend fun login(username: String, password: String): Result<UserSession> = runCatching {
        val usernameLower = username.lowercase().trim()
        val query = usersRef.whereEqualTo("username_lower", usernameLower).limit(1).get().await()
        if (query.isEmpty) throw IllegalArgumentException("Invalid username or password")

        val doc = query.documents.first()
        val data = doc.data ?: throw IllegalArgumentException("Invalid user record")
        val storedHash = data["password_hash"] as? String ?: ""
        if (!hasher.verify(password, storedHash)) throw IllegalArgumentException("Invalid username or password")

        val profileDoc = userDataRef.document(doc.id).get().await()
        val profileData = profileDoc.data
        val lastProfile = profileData?.get("last_active_profile") as? String ?: "Default"
        UserSession(
            userId = doc.id,
            username = data["username"] as? String ?: username,
            role = data["role"] as? String ?: "user",
            currentProfile = lastProfile
        )
    }

    suspend fun loadProfile(session: UserSession): Result<ProfileEnvelope> = runCatching {
        val (transactions, settings) = getData(session.userId, session.currentProfile)
        buildEnvelope(session.currentProfile, transactions, settings)
    }

    suspend fun listProfiles(session: UserSession): Result<List<String>> = runCatching {
        val doc = userDataRef.document(session.userId).get().await()
        val data = doc.data ?: return@runCatching listOf("Default")
        val profiles = (data["profiles"] as? Map<*, *>)?.keys?.map { it.toString() } ?: emptyList()
        if (profiles.isNotEmpty()) profiles.sorted() else listOf("Default")
    }

    suspend fun switchProfile(session: UserSession, profile: String): Result<UserSession> = runCatching {
        userDataRef.document(session.userId).set(mapOf("last_active_profile" to profile), com.google.firebase.firestore.SetOptions.merge()).await()
        session.copy(currentProfile = profile)
    }

    suspend fun createProfile(session: UserSession, profile: String): Result<List<String>> = runCatching {
        val doc = userDataRef.document(session.userId).get().await()
        val data = doc.data?.toMutableMap() ?: mutableMapOf()
        val profiles = (data["profiles"] as? Map<*, *>)?.toMutableMap() ?: mutableMapOf()
        if (profiles.containsKey(profile)) throw IllegalStateException("Profile already exists")
        val newProfile = mapOf(
            "transactions" to emptyList<Map<String, Any>>(),
            "settings" to settingsToMap(defaultSettings()),
            "last_updated" to nowIso()
        )
        profiles[profile] = newProfile
        data["profiles"] = profiles
        data["last_active_profile"] = profile
        userDataRef.document(session.userId).set(data).await()
        profiles.keys.map { it.toString() }
    }

    suspend fun addTransaction(session: UserSession, amount: Int, source: String, dateIso: String?): Result<ProfileEnvelope> = runCatching {
        val profileName = session.currentProfile
        val (transactions, settings) = getData(session.userId, profileName)
        val tx = Transaction(
            id = UUID.randomUUID().toString(),
            date = dateIso ?: nowIso(),
            amount = amount,
            source = source
        )
        val updated = recalcBalances(transactions + tx)
        saveProfile(session.userId, profileName, updated, settings)
        buildEnvelope(profileName, updated, settings)
    }

    suspend fun updateTransaction(session: UserSession, transactionId: String, amount: Int, source: String, dateIso: String): Result<ProfileEnvelope> = runCatching {
        val profileName = session.currentProfile
        val (transactions, settings) = getData(session.userId, profileName)
        val updatedList = transactions.map {
            if (it.id == transactionId) it.copy(amount = amount, source = source, date = dateIso) else it
        }
        val updated = recalcBalances(updatedList)
        saveProfile(session.userId, profileName, updated, settings)
        buildEnvelope(profileName, updated, settings)
    }

    suspend fun deleteTransaction(session: UserSession, transactionId: String): Result<ProfileEnvelope> = runCatching {
        val profileName = session.currentProfile
        val (transactions, settings) = getData(session.userId, profileName)
        val updatedList = transactions.filterNot { it.id == transactionId }
        val updated = recalcBalances(updatedList)
        saveProfile(session.userId, profileName, updated, settings)
        buildEnvelope(profileName, updated, settings)
    }

    suspend fun updateSettings(session: UserSession, updatedSettings: Settings): Result<ProfileEnvelope> = runCatching {
        val profileName = session.currentProfile
        val (transactions, _) = getData(session.userId, profileName)
        saveProfile(session.userId, profileName, transactions, updatedSettings)
        buildEnvelope(profileName, transactions, updatedSettings)
    }

    suspend fun addQuickAction(session: UserSession, action: QuickAction): Result<ProfileEnvelope> = runCatching {
        val profileName = session.currentProfile
        val (transactions, settings) = getData(session.userId, profileName)
        val newSettings = settings.copy(quickActions = settings.quickActions + action)
        saveProfile(session.userId, profileName, transactions, newSettings)
        buildEnvelope(profileName, transactions, newSettings)
    }

    suspend fun deleteQuickAction(session: UserSession, index: Int): Result<ProfileEnvelope> = runCatching {
        val profileName = session.currentProfile
        val (transactions, settings) = getData(session.userId, profileName)
        if (index < 0 || index >= settings.quickActions.size) throw IndexOutOfBoundsException("Invalid quick action index")
        val newSettings = settings.copy(quickActions = settings.quickActions.filterIndexed { i, _ -> i != index })
        saveProfile(session.userId, profileName, transactions, newSettings)
        buildEnvelope(profileName, transactions, newSettings)
    }

    suspend fun importData(session: UserSession, transactions: List<Transaction>, settings: Settings): Result<ProfileEnvelope> = runCatching {
        val profileName = session.currentProfile
        val validatedTx = recalcBalances(transactions)
        saveProfile(session.userId, profileName, validatedTx, settings)
        buildEnvelope(profileName, validatedTx, settings)
    }

    suspend fun loadAdminStats(): Result<AdminStats> = runCatching {
        val usersSnapshot = usersRef.get().await()
        val totalUsers = usersSnapshot.size()

        val thirtyDaysAgo = Instant.now().minusSeconds(30L * 24 * 3600)
        val signupsByDay = mutableMapOf<String, Int>()
        usersSnapshot.documents.forEach { doc ->
            val created = doc.getString("created_at") ?: return@forEach
            val instant = runCatching { Instant.parse(created) }.getOrNull() ?: return@forEach
            if (instant.isAfter(thirtyDaysAgo)) {
                val day = instant.atZone(ZoneOffset.UTC).toLocalDate().toString()
                signupsByDay[day] = (signupsByDay[day] ?: 0) + 1
            }
        }

        val labels = mutableListOf<String>()
        val data = mutableListOf<Int>()
        repeat(30) { i ->
            val day = LocalDate.now(ZoneOffset.UTC).minusDays((29 - i).toLong()).toString()
            labels += day
            data += signupsByDay[day] ?: 0
        }

        var totalCoins = 0
        var totalTransactions = 0
        val userData = userDataRef.get().await()
        userData.documents.forEach { doc ->
            val payload = doc.data ?: return@forEach
            val profiles = payload["profiles"] as? Map<*, *>
            if (profiles != null) {
                profiles.values.forEach { profileObj ->
                    val profile = profileObj as? Map<*, *> ?: return@forEach
                    val txns = parseTransactions(profile["transactions"]) ?: emptyList()
                    totalTransactions += txns.size
                    totalCoins += txns.sumOf { it.amount }
                }
            } else {
                val txns = parseTransactions(payload["transactions"]) ?: emptyList()
                totalTransactions += txns.size
                totalCoins += txns.sumOf { it.amount }
            }
        }

        AdminStats(totalUsers, totalCoins, totalTransactions, labels, data)
    }

    suspend fun loadAdminUsers(): Result<List<AdminUserRow>> = runCatching {
        val usersSnapshot = usersRef.orderBy("username_lower").get().await()
        val rows = mutableMapOf<String, AdminUserRow>()
        usersSnapshot.documents.forEach { doc ->
            val data = doc.data ?: return@forEach
            rows[doc.id] = AdminUserRow(
                userId = doc.id,
                username = data["username"] as? String ?: "N/A",
                balance = 0,
                txnCount = 0,
                createdAt = data["created_at"] as? String ?: "N/A",
                lastUpdated = "N/A"
            )
        }

        val userData = userDataRef.get().await()
        userData.documents.forEach { doc ->
            val target = rows[doc.id] ?: return@forEach
            val payload = doc.data ?: return@forEach
            var balance = 0
            var txnCount = 0
            var lastUpdated = target.lastUpdated

            val profiles = payload["profiles"] as? Map<*, *>
            if (profiles != null) {
                profiles.values.forEach { profileObj ->
                    val profile = profileObj as? Map<*, *> ?: return@forEach
                    val txns = parseTransactions(profile["transactions"]) ?: emptyList()
                    balance += txns.sumOf { it.amount }
                    txnCount += txns.size
                    val lu = profile["last_updated"] as? String
                    if (lu != null && (lastUpdated == "N/A" || lu > lastUpdated)) lastUpdated = lu
                }
            } else {
                val txns = parseTransactions(payload["transactions"]) ?: emptyList()
                balance += txns.sumOf { it.amount }
                txnCount += txns.size
            }

            rows[doc.id] = target.copy(balance = balance, txnCount = txnCount, lastUpdated = lastUpdated)
        }

        rows.values.toList()
    }

    suspend fun deleteUser(userId: String): Result<Unit> = runCatching {
        usersRef.document(userId).delete().await()
        userDataRef.document(userId).delete().await()
    }

    private suspend fun getData(userId: String, profile: String): Pair<List<Transaction>, Settings> {
        val doc = userDataRef.document(userId).get().await()
        val data = doc.data ?: emptyMap<String, Any>()
        val profiles = data["profiles"] as? Map<*, *>
        if (profiles != null) {
            val profileData = profiles[profile] as? Map<*, *> ?: emptyMap<String, Any>()
            val transactions = parseTransactions(profileData["transactions"]) ?: emptyList()
            val settings = parseSettings(profileData["settings"]) ?: defaultSettings()
            return recalcBalances(transactions) to settings
        }
        val transactions = parseTransactions(data["transactions"]) ?: emptyList()
        val settings = parseSettings(data["settings"]) ?: defaultSettings()
        return recalcBalances(transactions) to settings
    }

    private suspend fun saveProfile(userId: String, profile: String, transactions: List<Transaction>, settings: Settings) {
        val doc = userDataRef.document(userId).get().await()
        val existing = doc.data?.toMutableMap() ?: mutableMapOf()
        val profiles = (existing["profiles"] as? Map<*, *>)?.toMutableMap() ?: mutableMapOf()
        profiles[profile] = mapOf(
            "transactions" to transactions.map { transactionToMap(it) },
            "settings" to settingsToMap(settings),
            "last_updated" to nowIso()
        )
        existing["profiles"] = profiles
        existing["last_active_profile"] = profile
        userDataRef.document(userId).set(existing).await()
    }

    private fun buildEnvelope(profileName: String, transactions: List<Transaction>, settings: Settings): ProfileEnvelope {
        val balance = transactions.sumOf { it.amount }
        val goal = settings.goal
        val today = LocalDate.now(ZoneOffset.UTC)
        val weekStart = today.minusDays(today.dayOfWeek.ordinal.toLong())
        val monthStart = today.withDayOfMonth(1)

        var todayEarn = 0
        var weekEarn = 0
        var monthEarn = 0
        var totalEarnings = 0
        var firstEarningDate: Instant? = null

        transactions.forEach { t ->
            if (t.amount > 0) {
                totalEarnings += t.amount
                val tInstant = runCatching { Instant.parse(t.date) }.getOrNull()
                if (tInstant != null) {
                    if (firstEarningDate == null || tInstant.isBefore(firstEarningDate)) {
                        firstEarningDate = tInstant
                    }
                    val d = tInstant.atZone(ZoneOffset.UTC).toLocalDate()
                    if (d == today) todayEarn += t.amount
                    if (!d.isBefore(weekStart)) weekEarn += t.amount
                    if (!d.isBefore(monthStart)) monthEarn += t.amount
                }
            }
        }

        val estimatedDays: Int? = if (totalEarnings > 0 && firstEarningDate != null) {
            val daysSinceStart = maxOf(1, (Instant.now().epochSecond - firstEarningDate!!.epochSecond).div(86_400).toInt())
            val avgDaily = totalEarnings / daysSinceStart.toDouble()
            val remaining = goal - balance
            when {
                remaining <= 0 -> 0
                avgDaily > 0 -> (remaining / avgDaily).toInt()
                else -> null
            }
        } else null

        val earningsBreakdown = mutableMapOf<String, Int>()
        val spendingBreakdown = mutableMapOf<String, Int>()
        transactions.forEach { t ->
            if (t.amount > 0) earningsBreakdown[t.source] = (earningsBreakdown[t.source] ?: 0) + t.amount
            if (t.amount < 0) spendingBreakdown[t.source] = (spendingBreakdown[t.source] ?: 0) + -t.amount
        }

        val timeline = transactions.sortedBy { it.date }.map { t ->
            TimelinePoint(t.date, t.previousBalance + t.amount)
        }

        val achievements = AchievementCalculator().calculate(transactions, balance, goal)

        val allSources = transactions.map { it.source }.toSet().sorted()
        val settingsEnriched = settings.copy(
            firebaseAvailable = true,
            allSources = allSources
        )

        return ProfileEnvelope(
            profile = profileName,
            transactions = transactions,
            settings = settingsEnriched,
            balance = balance,
            goal = goal,
            progress = if (goal > 0) minOf(100, ((balance.toDouble() / goal) * 100).toInt()) else 0,
            estimatedDays = estimatedDays,
            dashboardStats = DashboardStats(todayEarn, weekEarn, monthEarn),
            analytics = AnalyticsSnapshot(
                totalEarnings = totalEarnings,
                totalSpending = -transactions.filter { it.amount < 0 }.sumOf { it.amount },
                netBalance = balance,
                earningsBreakdown = earningsBreakdown,
                spendingBreakdown = spendingBreakdown,
                timeline = timeline
            ),
            achievements = achievements
        )
    }

    private fun parseTransactions(raw: Any?): List<Transaction>? {
        val list = raw as? List<*> ?: return null
        return list.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            Transaction(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                date = map["date"] as? String ?: nowIso(),
                amount = (map["amount"] as? Number)?.toInt() ?: 0,
                source = map["source"] as? String ?: "",
                previousBalance = (map["previous_balance"] as? Number)?.toInt()
                    ?: (map["previousBalance"] as? Number)?.toInt() ?: 0
            )
        }
    }

    private fun parseSettings(raw: Any?): Settings? {
        val map = raw as? Map<*, *> ?: return null
        val goal = (map["goal"] as? Number)?.toInt() ?: 13500
        val darkMode = map["dark_mode"] as? Boolean ?: false
        val qaRaw = map["quick_actions"] as? List<*> ?: defaultQuickActions()
        val quickActions = qaRaw.mapNotNull { qa ->
            val qaMap = qa as? Map<*, *> ?: return@mapNotNull null
            QuickAction(
                text = qaMap["text"] as? String ?: return@mapNotNull null,
                value = (qaMap["value"] as? Number)?.toInt() ?: return@mapNotNull null,
                isPositive = qaMap["is_positive"] as? Boolean ?: true
            )
        }
        return Settings(goal, darkMode, quickActions, firebaseAvailable = true)
    }

    private fun settingsToMap(settings: Settings): Map<String, Any> = mapOf(
        "goal" to settings.goal,
        "dark_mode" to settings.darkMode,
        "quick_actions" to settings.quickActions.map {
            mapOf(
                "text" to it.text,
                "value" to it.value,
                "is_positive" to it.isPositive
            )
        }
    )

    private fun transactionToMap(tx: Transaction): Map<String, Any> = mapOf(
        "id" to tx.id,
        "date" to tx.date,
        "amount" to tx.amount,
        "source" to tx.source,
        "previous_balance" to tx.previousBalance
    )

    private fun recalcBalances(transactions: List<Transaction>): List<Transaction> {
        var balance = 0
        return transactions.sortedBy { it.date }.map { t ->
            val updated = t.copy(previousBalance = balance)
            balance += t.amount
            updated
        }
    }

    private fun nowIso(): String = Instant.now().atOffset(ZoneOffset.UTC).toString()
}
