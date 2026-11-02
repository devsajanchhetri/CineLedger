package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CineConstants
import com.example.data.model.LedgerTransactionItem
import com.example.data.model.ShootRecord
import com.example.data.model.Studio
import com.example.data.model.StudioBalanceSummary
import com.example.data.model.TransactionKind
import com.example.data.model.WithdrawalRecord
import com.example.data.repository.CineLedgerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface DataStreamState<out T> {
  data object Loading : DataStreamState<Nothing>
  data class Success<T>(val data: T) : DataStreamState<T>
  data class Error(val message: String) : DataStreamState<Nothing>
}

enum class LedgerFilterKind {
  ALL,
  SHOOTS_ONLY,
  WITHDRAWALS_ONLY,
}

data class FilterState(
  val selectedCategory: String = "All",
  val selectedStudioId: String? = null,
  val searchQuery: String = "",
  val ledgerFilterKind: LedgerFilterKind = LedgerFilterKind.ALL,
  val currencySymbol: String = "रु",
  val snackbarMessage: String? = null,
)

data class CineLedgerUiState(
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val studios: List<Studio> = emptyList(),
  val shoots: List<ShootRecord> = emptyList(),
  val withdrawals: List<WithdrawalRecord> = emptyList(),
  val studioSummaries: List<StudioBalanceSummary> = emptyList(),
  val filteredShoots: List<ShootRecord> = emptyList(),
  val filteredTransactions: List<LedgerTransactionItem> = emptyList(),
  val totalLifetimeEarnings: Double = 0.0,
  val totalLifetimeWithdrawn: Double = 0.0,
  val totalAvailableBalance: Double = 0.0,
  val selectedCategory: String = "All",
  val selectedStudioId: String? = null,
  val selectedStudioName: String? = null,
  val searchQuery: String = "",
  val ledgerFilterKind: LedgerFilterKind = LedgerFilterKind.ALL,
  val currencySymbol: String = "रु",
  val snackbarMessage: String? = null,
)

class CineLedgerViewModel(
  private val repository: CineLedgerRepository,
  private val currentUserId: String,
) : ViewModel() {

  private val filterState = MutableStateFlow(FilterState())

  private val studiosStream = repository.observeStudios(currentUserId)
    .map<List<Studio>, DataStreamState<List<Studio>>> { DataStreamState.Success(it) }
    .catch { e ->
      Log.w(TAG, "Error observing studios", e)
      emit(DataStreamState.Error(e.localizedMessage ?: "Failed to load studios"))
    }

  private val shootsStream = repository.observeShoots(currentUserId)
    .map<List<ShootRecord>, DataStreamState<List<ShootRecord>>> { DataStreamState.Success(it) }
    .catch { e ->
      Log.w(TAG, "Error observing shoots", e)
      emit(DataStreamState.Error(e.localizedMessage ?: "Failed to load shoots"))
    }

  private val withdrawalsStream = repository.observeWithdrawals(currentUserId)
    .map<List<WithdrawalRecord>, DataStreamState<List<WithdrawalRecord>>> { DataStreamState.Success(it) }
    .catch { e ->
      Log.w(TAG, "Error observing withdrawals", e)
      emit(DataStreamState.Error(e.localizedMessage ?: "Failed to load withdrawals"))
    }

  val uiState: StateFlow<CineLedgerUiState> = combine(
    studiosStream,
    shootsStream,
    withdrawalsStream,
    filterState,
  ) { studiosRes, shootsRes, withdrawalsRes, filters ->
    val isLoading = studiosRes is DataStreamState.Loading ||
      shootsRes is DataStreamState.Loading ||
      withdrawalsRes is DataStreamState.Loading

    val streamError = listOfNotNull(
      (studiosRes as? DataStreamState.Error)?.message,
      (shootsRes as? DataStreamState.Error)?.message,
      (withdrawalsRes as? DataStreamState.Error)?.message,
    ).firstOrNull()

    val studios = (studiosRes as? DataStreamState.Success)?.data
      ?.sortedBy { it.name.lowercase() }
      ?: emptyList()

    val shoots = (shootsRes as? DataStreamState.Success)?.data
      ?.sortedWith(
        compareByDescending<ShootRecord> { it.shootDateMillis }
          .thenByDescending { it.createdAt?.seconds ?: 0L }
      )
      ?: emptyList()

    val withdrawals = (withdrawalsRes as? DataStreamState.Success)?.data
      ?.sortedWith(
        compareByDescending<WithdrawalRecord> { it.withdrawalDateMillis }
          .thenByDescending { it.createdAt?.seconds ?: 0L }
      )
      ?: emptyList()

    // Compute per-studio balances
    val studioSummaries = studios.map { studio ->
      val studioShoots = shoots.filter { it.studioId == studio.id }
      val studioWithdrawals = withdrawals.filter { it.studioId == studio.id }
      val earned = studioShoots.sumOf { it.earnings }
      val withdrawn = studioWithdrawals.sumOf { it.amount }
      val lastShootDate = studioShoots.maxOfOrNull { it.shootDateMillis } ?: 0L
      val lastWdDate = studioWithdrawals.maxOfOrNull { it.withdrawalDateMillis } ?: 0L
      StudioBalanceSummary(
        studio = studio,
        totalEarned = earned,
        totalWithdrawn = withdrawn,
        availableBalance = earned - withdrawn,
        shootCount = studioShoots.size,
        withdrawalCount = studioWithdrawals.size,
        lastActivityMillis = maxOf(lastShootDate, lastWdDate, (studio.createdAt?.seconds ?: 0L) * 1000L),
      )
    }.sortedByDescending { it.availableBalance }

    val totalLifetimeEarnings = shoots.sumOf { it.earnings }
    val totalLifetimeWithdrawn = withdrawals.sumOf { it.amount }
    val totalAvailableBalance = totalLifetimeEarnings - totalLifetimeWithdrawn

    val query = filters.searchQuery.trim().lowercase()

    val filteredShoots = shoots.filter { shoot ->
      val matchesCategory = filters.selectedCategory == "All" ||
        shoot.category.equals(filters.selectedCategory, ignoreCase = true)
      val matchesStudio = filters.selectedStudioId == null ||
        shoot.studioId == filters.selectedStudioId
      val matchesQuery = query.isEmpty() ||
        shoot.title.lowercase().contains(query) ||
        shoot.studioName.lowercase().contains(query) ||
        shoot.location.lowercase().contains(query) ||
        shoot.category.lowercase().contains(query) ||
        shoot.notes.lowercase().contains(query)
      matchesCategory && matchesStudio && matchesQuery
    }

    val shootTransactions = shoots.map { shoot ->
      val durText = if (shoot.durationValue % 1.0 == 0.0) {
        "${shoot.durationValue.toInt()} ${shoot.durationUnit}"
      } else {
        "${shoot.durationValue} ${shoot.durationUnit}"
      }
      val locSuffix = if (shoot.location.isNotBlank()) " • ${shoot.location}" else ""
      LedgerTransactionItem(
        id = "shoot_${shoot.id}",
        kind = TransactionKind.SHOOT_EARNING,
        title = shoot.title,
        subtitle = "${shoot.studioName} • $durText$locSuffix",
        studioId = shoot.studioId,
        studioName = shoot.studioName,
        amount = shoot.earnings,
        dateMillis = shoot.shootDateMillis,
        badgeText = shoot.category,
        notes = shoot.notes,
        shootRecord = shoot,
      )
    }

    val withdrawalTransactions = withdrawals.map { wd ->
      LedgerTransactionItem(
        id = "wd_${wd.id}",
        kind = TransactionKind.STUDIO_WITHDRAWAL,
        title = "Withdrawal • ${wd.studioName}",
        subtitle = if (wd.notes.isNotBlank()) "${wd.paymentMethod} • ${wd.notes}" else wd.paymentMethod,
        studioId = wd.studioId,
        studioName = wd.studioName,
        amount = wd.amount,
        dateMillis = wd.withdrawalDateMillis,
        badgeText = wd.paymentMethod,
        notes = wd.notes,
        withdrawalRecord = wd,
      )
    }

    val allTransactions = (shootTransactions + withdrawalTransactions)
      .sortedByDescending { it.dateMillis }
      .filter { item ->
        val matchesStudio = filters.selectedStudioId == null || item.studioId == filters.selectedStudioId
        val matchesKind = when (filters.ledgerFilterKind) {
          LedgerFilterKind.ALL -> true
          LedgerFilterKind.SHOOTS_ONLY -> item.kind == TransactionKind.SHOOT_EARNING
          LedgerFilterKind.WITHDRAWALS_ONLY -> item.kind == TransactionKind.STUDIO_WITHDRAWAL
        }
        val matchesQuery = query.isEmpty() ||
          item.title.lowercase().contains(query) ||
          item.studioName.lowercase().contains(query) ||
          item.subtitle.lowercase().contains(query) ||
          item.badgeText.lowercase().contains(query)
        matchesStudio && matchesKind && matchesQuery
      }

    val selectedStudioName = studios.find { it.id == filters.selectedStudioId }?.name

    CineLedgerUiState(
      isLoading = isLoading,
      errorMessage = streamError,
      studios = studios,
      shoots = shoots,
      withdrawals = withdrawals,
      studioSummaries = studioSummaries,
      filteredShoots = filteredShoots,
      filteredTransactions = allTransactions,
      totalLifetimeEarnings = totalLifetimeEarnings,
      totalLifetimeWithdrawn = totalLifetimeWithdrawn,
      totalAvailableBalance = totalAvailableBalance,
      selectedCategory = filters.selectedCategory,
      selectedStudioId = filters.selectedStudioId,
      selectedStudioName = selectedStudioName,
      searchQuery = filters.searchQuery,
      ledgerFilterKind = filters.ledgerFilterKind,
      currencySymbol = filters.currencySymbol,
      snackbarMessage = filters.snackbarMessage,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000L),
    initialValue = CineLedgerUiState(isLoading = true),
  )

  fun setCategoryFilter(category: String) {
    filterState.update { it.copy(selectedCategory = category) }
  }

  fun setStudioFilter(studioId: String?) {
    filterState.update { it.copy(selectedStudioId = studioId) }
  }

  fun setSearchQuery(query: String) {
    filterState.update { it.copy(searchQuery = query) }
  }

  fun setLedgerFilterKind(kind: LedgerFilterKind) {
    filterState.update { it.copy(ledgerFilterKind = kind) }
  }

  fun setCurrencySymbol(symbol: String) {
    if (symbol in CineConstants.CURRENCIES) {
      filterState.update { it.copy(currencySymbol = symbol) }
    }
  }

  fun clearSnackbar() {
    filterState.update { it.copy(snackbarMessage = null) }
  }

  private fun notify(message: String) {
    filterState.update { it.copy(snackbarMessage = message) }
  }

  fun addStudio(name: String, contactPerson: String, onSuccess: (String) -> Unit = {}) {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) {
      notify("Please enter a studio name")
      return
    }
    viewModelScope.launch {
      val result = repository.createStudio(trimmed, contactPerson.trim())
      result.fold(
        onSuccess = { id ->
          notify("Studio \"$trimmed\" saved")
          onSuccess(id)
        },
        onFailure = { e ->
          notify(e.localizedMessage ?: "Failed to save studio")
        }
      )
    }
  }

  fun updateStudio(studio: Studio, newName: String, newContact: String) {
    val trimmed = newName.trim()
    if (trimmed.isEmpty()) {
      notify("Studio name cannot be empty")
      return
    }
    viewModelScope.launch {
      val result = repository.updateStudio(
        studio.copy(name = trimmed, contactPerson = newContact.trim())
      )
      result.fold(
        onSuccess = { notify("Studio updated") },
        onFailure = { e -> notify(e.localizedMessage ?: "Failed to update studio") }
      )
    }
  }

  fun deleteStudio(studio: Studio) {
    viewModelScope.launch {
      // Also delete associated shoots and withdrawals if user deletes studio, or just studio doc
      val result = repository.deleteStudio(studio.id)
      result.fold(
        onSuccess = {
          if (filterState.value.selectedStudioId == studio.id) {
            filterState.update { it.copy(selectedStudioId = null) }
          }
          notify("Studio \"${studio.name}\" deleted")
        },
        onFailure = { e -> notify(e.localizedMessage ?: "Failed to delete studio") }
      )
    }
  }

  fun saveShoot(
    existingShoot: ShootRecord?,
    title: String,
    selectedStudio: Studio?,
    newStudioName: String,
    newStudioContact: String,
    category: String,
    shootDateMillis: Long,
    location: String,
    durationValue: Double,
    durationUnit: String,
    earnings: Double,
    notes: String,
    onComplete: () -> Unit,
  ) {
    val cleanTitle = title.trim()
    if (cleanTitle.isEmpty()) {
      notify("Event shoot title is required")
      return
    }
    if (earnings < 0) {
      notify("Earnings amount must be 0 or greater")
      return
    }
    viewModelScope.launch {
      val resolvedStudioId: String
      val resolvedStudioName: String

      if (selectedStudio != null) {
        resolvedStudioId = selectedStudio.id
        resolvedStudioName = selectedStudio.name
      } else {
        val cleanNewStudio = newStudioName.trim()
        if (cleanNewStudio.isEmpty()) {
          notify("Please select a studio or enter a new studio name")
          return@launch
        }
        val studioCreateRes = repository.createStudio(cleanNewStudio, newStudioContact.trim())
        if (studioCreateRes.isFailure) {
          notify(studioCreateRes.exceptionOrNull()?.localizedMessage ?: "Could not create studio")
          return@launch
        }
        resolvedStudioId = studioCreateRes.getOrThrow()
        resolvedStudioName = cleanNewStudio
      }

      if (existingShoot == null) {
        val res = repository.createShoot(
          title = cleanTitle,
          studioId = resolvedStudioId,
          studioName = resolvedStudioName,
          category = category,
          shootDateMillis = shootDateMillis,
          location = location.trim(),
          durationValue = durationValue.coerceIn(0.5, 1000.0),
          durationUnit = durationUnit,
          earnings = earnings,
          notes = notes.trim(),
        )
        res.fold(
          onSuccess = {
            notify("Shoot \"$cleanTitle\" logged to cloud")
            onComplete()
          },
          onFailure = { e -> notify(e.localizedMessage ?: "Failed to save shoot") }
        )
      } else {
        val res = repository.updateShoot(
          existingShoot.copy(
            title = cleanTitle,
            studioId = resolvedStudioId,
            studioName = resolvedStudioName,
            category = category,
            shootDateMillis = shootDateMillis,
            location = location.trim(),
            durationValue = durationValue.coerceIn(0.5, 1000.0),
            durationUnit = durationUnit,
            earnings = earnings,
            notes = notes.trim(),
          )
        )
        res.fold(
          onSuccess = {
            notify("Shoot \"$cleanTitle\" updated")
            onComplete()
          },
          onFailure = { e -> notify(e.localizedMessage ?: "Failed to update shoot") }
        )
      }
    }
  }

  fun deleteShoot(shoot: ShootRecord) {
    viewModelScope.launch {
      val res = repository.deleteShoot(shoot.id)
      res.fold(
        onSuccess = { notify("Shoot \"${shoot.title}\" deleted") },
        onFailure = { e -> notify(e.localizedMessage ?: "Failed to delete shoot") }
      )
    }
  }

  fun saveWithdrawal(
    existingWithdrawal: WithdrawalRecord?,
    selectedStudio: Studio?,
    newStudioName: String,
    amount: Double,
    withdrawalDateMillis: Long,
    paymentMethod: String,
    notes: String,
    onComplete: () -> Unit,
  ) {
    if (amount <= 0.0) {
      notify("Withdrawal amount must be greater than 0")
      return
    }
    viewModelScope.launch {
      val resolvedStudioId: String
      val resolvedStudioName: String

      if (selectedStudio != null) {
        resolvedStudioId = selectedStudio.id
        resolvedStudioName = selectedStudio.name
      } else {
        val cleanNewStudio = newStudioName.trim()
        if (cleanNewStudio.isEmpty()) {
          notify("Please select a studio for this withdrawal")
          return@launch
        }
        val studioRes = repository.createStudio(cleanNewStudio, "")
        if (studioRes.isFailure) {
          notify(studioRes.exceptionOrNull()?.localizedMessage ?: "Could not create studio")
          return@launch
        }
        resolvedStudioId = studioRes.getOrThrow()
        resolvedStudioName = cleanNewStudio
      }

      if (existingWithdrawal == null) {
        val res = repository.createWithdrawal(
          studioId = resolvedStudioId,
          studioName = resolvedStudioName,
          amount = amount,
          withdrawalDateMillis = withdrawalDateMillis,
          paymentMethod = paymentMethod,
          notes = notes.trim(),
        )
        res.fold(
          onSuccess = {
            notify("Withdrawal from $resolvedStudioName recorded")
            onComplete()
          },
          onFailure = { e -> notify(e.localizedMessage ?: "Failed to save withdrawal") }
        )
      } else {
        val res = repository.updateWithdrawal(
          existingWithdrawal.copy(
            studioId = resolvedStudioId,
            studioName = resolvedStudioName,
            amount = amount,
            withdrawalDateMillis = withdrawalDateMillis,
            paymentMethod = paymentMethod,
            notes = notes.trim(),
          )
        )
        res.fold(
          onSuccess = {
            notify("Withdrawal updated")
            onComplete()
          },
          onFailure = { e -> notify(e.localizedMessage ?: "Failed to update withdrawal") }
        )
      }
    }
  }

  fun deleteWithdrawal(withdrawal: WithdrawalRecord) {
    viewModelScope.launch {
      val res = repository.deleteWithdrawal(withdrawal.id)
      res.fold(
        onSuccess = { notify("Withdrawal transaction deleted") },
        onFailure = { e -> notify(e.localizedMessage ?: "Failed to delete withdrawal") }
      )
    }
  }

  private companion object {
    const val TAG = "CineLedgerVM"
  }
}
