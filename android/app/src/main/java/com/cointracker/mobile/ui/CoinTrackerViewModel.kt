package com.cointracker.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cointracker.mobile.data.AdminStats
import com.cointracker.mobile.data.AdminUserRow
import com.cointracker.mobile.data.FirestoreRepository
import com.cointracker.mobile.data.ProfileEnvelope
import com.cointracker.mobile.data.QuickAction
import com.cointracker.mobile.data.Settings
import com.cointracker.mobile.data.Transaction
import com.cointracker.mobile.data.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUiState(
    val session: UserSession? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val profileEnvelope: ProfileEnvelope? = null,
    val profiles: List<String> = listOf("Default"),
    val adminStats: AdminStats? = null,
    val adminUsers: List<AdminUserRow> = emptyList()
)

class CoinTrackerViewModel(private val repo: FirestoreRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState

    fun register(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = repo.register(username, password)
            if (result.isSuccess) {
                login(username, password)
            } else {
                _uiState.update { it.copy(loading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = repo.login(username, password)
            if (result.isSuccess) {
                val session = result.getOrThrow()
                _uiState.update { it.copy(session = session) }
                refreshData()
                loadProfiles()
            } else {
                _uiState.update { it.copy(loading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun logout() {
        _uiState.value = AppUiState()
    }

    fun refreshData() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = repo.loadProfile(session.userId, session.currentProfile)
            _uiState.update {
                if (result.isSuccess) it.copy(profileEnvelope = result.getOrThrow(), loading = false)
                else it.copy(error = result.exceptionOrNull()?.message, loading = false)
            }
        }
    }

    fun switchProfile(profile: String) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = repo.switchProfile(session, profile)
            if (result.isSuccess) {
                val updatedSession = result.getOrThrow()
                _uiState.update { it.copy(session = updatedSession) }
                refreshData()
                loadProfiles()
            } else {
                _uiState.update { it.copy(loading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun createProfile(profile: String) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = repo.createProfile(session, profile)
            if (result.isSuccess) {
                _uiState.update { it.copy(profiles = result.getOrThrow(), loading = false) }
                refreshData()
            } else {
                _uiState.update { it.copy(loading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun addTransaction(amount: Int, source: String, dateIso: String?) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = repo.addTransaction(session, amount, source, dateIso)
            _uiState.update { state ->
                if (result.isSuccess) state.copy(profileEnvelope = result.getOrThrow(), loading = false)
                else state.copy(loading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun updateTransaction(transactionId: String, amount: Int, source: String, dateIso: String) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = repo.updateTransaction(session, transactionId, amount, source, dateIso)
            _uiState.update { state ->
                if (result.isSuccess) state.copy(profileEnvelope = result.getOrThrow(), loading = false)
                else state.copy(loading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = repo.deleteTransaction(session, transactionId)
            _uiState.update { state ->
                if (result.isSuccess) state.copy(profileEnvelope = result.getOrThrow(), loading = false)
                else state.copy(loading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun updateSettings(settings: Settings) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = repo.updateSettings(session, settings)
            _uiState.update { state ->
                if (result.isSuccess) state.copy(profileEnvelope = result.getOrThrow(), loading = false)
                else state.copy(loading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun addQuickAction(action: QuickAction) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = repo.addQuickAction(session, action)
            _uiState.update { state ->
                if (result.isSuccess) state.copy(profileEnvelope = result.getOrThrow(), loading = false)
                else state.copy(loading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun deleteQuickAction(index: Int) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = repo.deleteQuickAction(session, index)
            _uiState.update { state ->
                if (result.isSuccess) state.copy(profileEnvelope = result.getOrThrow(), loading = false)
                else state.copy(loading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun importData(transactions: List<Transaction>, settings: Settings) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = repo.importData(session, transactions, settings)
            _uiState.update { state ->
                if (result.isSuccess) state.copy(profileEnvelope = result.getOrThrow(), loading = false)
                else state.copy(loading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun loadAdmin() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val stats = repo.loadAdminStats()
            val users = repo.loadAdminUsers()
            _uiState.update { state ->
                state.copy(
                    adminStats = stats.getOrNull(),
                    adminUsers = users.getOrDefault(emptyList()),
                    loading = false,
                    error = stats.exceptionOrNull()?.message ?: users.exceptionOrNull()?.message
                )
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = repo.deleteUser(userId)
            if (result.isSuccess) loadAdmin() else _uiState.update { it.copy(loading = false, error = result.exceptionOrNull()?.message) }
        }
    }

    private fun loadProfiles() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            val profiles = repo.listProfiles(session)
            if (profiles.isSuccess) {
                _uiState.update { it.copy(profiles = profiles.getOrThrow(), loading = false) }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = FirestoreRepository()
                @Suppress("UNCHECKED_CAST")
                return CoinTrackerViewModel(repo) as T
            }
        }
    }
}

