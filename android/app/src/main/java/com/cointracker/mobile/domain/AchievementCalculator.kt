package com.cointracker.mobile.domain

import com.cointracker.mobile.data.Achievement
import com.cointracker.mobile.data.Transaction
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class AchievementCalculator {
    fun calculate(transactions: List<Transaction>, balance: Int, goal: Int): List<Achievement> {
        val achievements = mutableListOf<Achievement>()
        val today = LocalDate.now(ZoneOffset.UTC)

        if (balance >= 1000) achievements += Achievement("💰", "Getting Started", "Reach 1,000 coins")
        if (balance >= 5000) achievements += Achievement("📈", "Serious Saver", "Reach 5,000 coins")
        if (balance >= 10000) achievements += Achievement("🏦", "Coin Hoarder", "Reach 10,000 coins")
        if (balance >= goal) achievements += Achievement("👑", "Epic Box Secured!", "You reached the ${goal} coin goal!")

        val loginDates = transactions.filter { it.amount > 0 && it.source.equals("login", ignoreCase = true) }
            .mapNotNull { runCatching { Instant.parse(it.date).atZone(ZoneOffset.UTC).toLocalDate() }.getOrNull() }
            .toSet()
        var streak = 0
        if (today in loginDates) {
            streak = 1
            var cursor = today.minusDays(1)
            while (cursor in loginDates) {
                streak += 1
                cursor = cursor.minusDays(1)
            }
        }
        if (streak >= 3) achievements += Achievement("🔥", "$streak-Day Streak", "Logged in $streak days in a row!")

        val sorted = transactions.sortedBy { it.date }
        val lastSpend = sorted.lastOrNull { it.amount < 0 }
        val lastSpendDate = lastSpend?.let { runCatching { Instant.parse(it.date).atZone(ZoneOffset.UTC).toLocalDate() }.getOrNull() }
        val noSpendDays = if (lastSpendDate != null) today.toEpochDay() - lastSpendDate.toEpochDay() else if (sorted.isNotEmpty()) today.toEpochDay() - runCatching { Instant.parse(sorted.first().date).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay() }.getOrElse { today.toEpochDay() } else 0
        if (noSpendDays >= 7) achievements += Achievement("🛡", "Disciplined", "No spending for $noSpendDays days!")

        return achievements
    }
}
