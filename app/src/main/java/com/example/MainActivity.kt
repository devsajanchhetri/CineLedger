package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.ShootRecord
import com.example.data.model.Studio
import com.example.data.model.WithdrawalRecord
import com.example.data.repository.CineLedgerRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.ShootEditorDialog
import com.example.ui.components.StudioEditorDialog
import com.example.ui.components.WithdrawalEditorDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.StudiosScreen
import com.example.ui.screens.TransactionsLedgerScreen
import com.example.ui.theme.CineLedgerTheme
import com.example.ui.theme.NetflixBlack
import com.example.ui.theme.NetflixCard
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.NetflixSilver
import com.example.ui.theme.NetflixSurface
import com.example.ui.theme.NetflixWhite
import com.example.ui.viewmodel.CineLedgerViewModel
import com.example.util.StatementExporter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      CineLedgerTheme {
        AppNavigation()
      }
    }
  }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
  val listener = FirebaseAuth.AuthStateListener { auth ->
    trySend(auth.currentUser)
  }
  addAuthStateListener(listener)
  awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
  val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
  val user = currentUser

  if (user == null) {
    AuthScreen(onAuthSuccess = {})
  } else {
    MainCinemaAppScreen(
      currentUserId = user.uid,
      userEmail = user.email ?: user.displayName,
    )
  }
}

enum class CinemaTab(
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
) {
  HOME("Home & Shoots", Icons.Filled.Movie, Icons.Outlined.Movie, "nav_tab_home"),
  STUDIOS("Studios", Icons.Filled.Business, Icons.Outlined.Business, "nav_tab_studios"),
  LEDGER("Transactions", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "nav_tab_ledger"),
}

private sealed interface DeleteTarget {
  data class Shoot(val shoot: ShootRecord) : DeleteTarget
  data class Withdrawal(val withdrawal: WithdrawalRecord) : DeleteTarget
  data class StudioItem(val studio: Studio) : DeleteTarget
}

@Composable
fun MainCinemaAppScreen(
  currentUserId: String,
  userEmail: String?,
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  val viewModel: CineLedgerViewModel = viewModel(
    key = currentUserId,
    factory = viewModelFactory {
      initializer {
        val app = checkNotNull(this[APPLICATION_KEY]) {
          "APPLICATION_KEY missing from CreationExtras"
        }
        val databaseId = app.getString(R.string.firestore_database_id)
        val db = FirebaseFirestore.getInstance(databaseId)
        CineLedgerViewModel(CineLedgerRepository(db), currentUserId)
      }
    }
  )

  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  var currentTab by rememberSaveable { mutableStateOf(CinemaTab.HOME) }
  var inspectedStudioId by rememberSaveable { mutableStateOf<String?>(null) }

  // Modal Dialog States
  var showShootModal by remember { mutableStateOf(false) }
  var editingShoot by remember { mutableStateOf<ShootRecord?>(null) }
  var modalPreselectedStudioId by remember { mutableStateOf<String?>(null) }

  var showWithdrawalModal by remember { mutableStateOf(false) }
  var editingWithdrawal by remember { mutableStateOf<WithdrawalRecord?>(null) }

  var showStudioModal by remember { mutableStateOf(false) }
  var editingStudio by remember { mutableStateOf<Studio?>(null) }

  var deleteTarget by remember { mutableStateOf<DeleteTarget?>(null) }

  LaunchedEffect(uiState.snackbarMessage) {
    val msg = uiState.snackbarMessage
    if (msg != null) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearSnackbar()
    }
  }

  if (currentTab != CinemaTab.HOME && inspectedStudioId == null) {
    BackHandler {
      currentTab = CinemaTab.HOME
    }
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(NetflixBlack)
  ) {
    val isExpandedScreen = maxWidth >= 640.dp

    Scaffold(
      containerColor = NetflixBlack,
      contentWindowInsets = WindowInsets.safeDrawing,
      snackbarHost = { SnackbarHost(snackbarHostState) },
      bottomBar = {
        if (!isExpandedScreen) {
          NavigationBar(
            containerColor = NetflixSurface,
            windowInsets = WindowInsets.navigationBars,
          ) {
            CinemaTab.entries.forEach { tab ->
              val selected = currentTab == tab
              NavigationBarItem(
                selected = selected,
                onClick = { currentTab = tab },
                icon = {
                  Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = tab.label,
                  )
                },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = NetflixWhite,
                  selectedTextColor = NetflixRed,
                  indicatorColor = NetflixRed,
                  unselectedIconColor = NetflixSilver,
                  unselectedTextColor = NetflixSilver,
                ),
                modifier = Modifier.testTag(tab.testTag),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        if (isExpandedScreen) {
          NavigationRail(
            containerColor = NetflixSurface,
          ) {
            CinemaTab.entries.forEach { tab ->
              val selected = currentTab == tab
              NavigationRailItem(
                selected = selected,
                onClick = { currentTab = tab },
                icon = {
                  Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = tab.label,
                  )
                },
                label = { Text(tab.label) },
                colors = NavigationRailItemDefaults.colors(
                  selectedIconColor = NetflixWhite,
                  selectedTextColor = NetflixRed,
                  indicatorColor = NetflixRed,
                  unselectedIconColor = NetflixSilver,
                  unselectedTextColor = NetflixSilver,
                ),
                modifier = Modifier.testTag(tab.testTag),
              )
            }
          }
        }

        Box(modifier = Modifier.weight(1f)) {
          when (currentTab) {
            CinemaTab.HOME -> {
              DashboardScreen(
                uiState = uiState,
                userEmail = userEmail,
                onLogShootClick = {
                  editingShoot = null
                  modalPreselectedStudioId = uiState.selectedStudioId
                  showShootModal = true
                },
                onRecordWithdrawalClick = {
                  editingWithdrawal = null
                  modalPreselectedStudioId = uiState.selectedStudioId
                  showWithdrawalModal = true
                },
                onAddStudioClick = {
                  editingStudio = null
                  showStudioModal = true
                },
                onEditShootClick = { shoot ->
                  editingShoot = shoot
                  showShootModal = true
                },
                onDeleteShootClick = { shoot ->
                  deleteTarget = DeleteTarget.Shoot(shoot)
                },
                onStudioCardClick = { studioId ->
                  if (uiState.selectedStudioId == studioId) {
                    viewModel.setStudioFilter(null)
                  } else {
                    viewModel.setStudioFilter(studioId)
                  }
                },
                onCategorySelected = { viewModel.setCategoryFilter(it) },
                onClearStudioFilter = { viewModel.setStudioFilter(null) },
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onCurrencyChange = { viewModel.setCurrencySymbol(it) },
                onExportPdfClick = {
                  StatementExporter.exportAndSharePdf(
                    context = context,
                    cinematographerName = userEmail ?: "Cinematographer",
                    studioFilterName = uiState.selectedStudioName,
                    currencySymbol = uiState.currencySymbol,
                    totalEarned = uiState.totalLifetimeEarnings,
                    totalWithdrawn = uiState.totalLifetimeWithdrawn,
                    availableBalance = uiState.totalAvailableBalance,
                    studioSummaries = uiState.studioSummaries,
                    shoots = uiState.filteredShoots,
                    withdrawals = if (uiState.selectedStudioId != null) {
                      uiState.withdrawals.filter { it.studioId == uiState.selectedStudioId }
                    } else {
                      uiState.withdrawals
                    },
                    onError = { err -> coroutineScope.launch { snackbarHostState.showSnackbar(err) } },
                  )
                },
                onExportCsvClick = {
                  StatementExporter.exportAndShareCsv(
                    context = context,
                    studioFilterName = uiState.selectedStudioName,
                    shoots = uiState.filteredShoots,
                    withdrawals = if (uiState.selectedStudioId != null) {
                      uiState.withdrawals.filter { it.studioId == uiState.selectedStudioId }
                    } else {
                      uiState.withdrawals
                    },
                    onError = { err -> coroutineScope.launch { snackbarHostState.showSnackbar(err) } },
                  )
                },
                onSignOutClick = {
                  signOut(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = {},
                    scope = coroutineScope,
                  )
                },
              )
            }

            CinemaTab.STUDIOS -> {
              StudiosScreen(
                uiState = uiState,
                inspectedStudioId = inspectedStudioId,
                onInspectStudio = { inspectedStudioId = it },
                onAddStudioClick = {
                  editingStudio = null
                  showStudioModal = true
                },
                onEditStudioClick = { studio ->
                  editingStudio = studio
                  showStudioModal = true
                },
                onDeleteStudioClick = { studio ->
                  deleteTarget = DeleteTarget.StudioItem(studio)
                },
                onLogShootForStudio = { studioId ->
                  editingShoot = null
                  modalPreselectedStudioId = studioId
                  showShootModal = true
                },
                onRecordWithdrawalForStudio = { studioId ->
                  editingWithdrawal = null
                  modalPreselectedStudioId = studioId
                  showWithdrawalModal = true
                },
                onEditShootClick = { shoot ->
                  editingShoot = shoot
                  showShootModal = true
                },
                onDeleteShootClick = { shoot ->
                  deleteTarget = DeleteTarget.Shoot(shoot)
                },
                onEditWithdrawalClick = { wd ->
                  editingWithdrawal = wd
                  showWithdrawalModal = true
                },
                onDeleteWithdrawalClick = { wd ->
                  deleteTarget = DeleteTarget.Withdrawal(wd)
                },
                onExportStudioPdf = { summary ->
                  StatementExporter.exportAndSharePdf(
                    context = context,
                    cinematographerName = userEmail ?: "Cinematographer",
                    studioFilterName = summary.studio.name,
                    currencySymbol = uiState.currencySymbol,
                    totalEarned = summary.totalEarned,
                    totalWithdrawn = summary.totalWithdrawn,
                    availableBalance = summary.availableBalance,
                    studioSummaries = listOf(summary),
                    shoots = uiState.shoots.filter { it.studioId == summary.studio.id },
                    withdrawals = uiState.withdrawals.filter { it.studioId == summary.studio.id },
                    onError = { err -> coroutineScope.launch { snackbarHostState.showSnackbar(err) } },
                  )
                },
                onExportStudioCsv = { summary ->
                  StatementExporter.exportAndShareCsv(
                    context = context,
                    studioFilterName = summary.studio.name,
                    shoots = uiState.shoots.filter { it.studioId == summary.studio.id },
                    withdrawals = uiState.withdrawals.filter { it.studioId == summary.studio.id },
                    onError = { err -> coroutineScope.launch { snackbarHostState.showSnackbar(err) } },
                  )
                },
              )
            }

            CinemaTab.LEDGER -> {
              TransactionsLedgerScreen(
                uiState = uiState,
                onRecordWithdrawalClick = {
                  editingWithdrawal = null
                  modalPreselectedStudioId = uiState.selectedStudioId
                  showWithdrawalModal = true
                },
                onLogShootClick = {
                  editingShoot = null
                  modalPreselectedStudioId = uiState.selectedStudioId
                  showShootModal = true
                },
                onFilterKindSelected = { viewModel.setLedgerFilterKind(it) },
                onStudioFilterSelected = { viewModel.setStudioFilter(it) },
                onEditShootClick = { shoot ->
                  editingShoot = shoot
                  showShootModal = true
                },
                onDeleteShootClick = { shoot ->
                  deleteTarget = DeleteTarget.Shoot(shoot)
                },
                onEditWithdrawalClick = { wd ->
                  editingWithdrawal = wd
                  showWithdrawalModal = true
                },
                onDeleteWithdrawalClick = { wd ->
                  deleteTarget = DeleteTarget.Withdrawal(wd)
                },
                onExportPdfClick = {
                  StatementExporter.exportAndSharePdf(
                    context = context,
                    cinematographerName = userEmail ?: "Cinematographer",
                    studioFilterName = uiState.selectedStudioName,
                    currencySymbol = uiState.currencySymbol,
                    totalEarned = uiState.totalLifetimeEarnings,
                    totalWithdrawn = uiState.totalLifetimeWithdrawn,
                    availableBalance = uiState.totalAvailableBalance,
                    studioSummaries = uiState.studioSummaries,
                    shoots = uiState.filteredShoots,
                    withdrawals = if (uiState.selectedStudioId != null) {
                      uiState.withdrawals.filter { it.studioId == uiState.selectedStudioId }
                    } else {
                      uiState.withdrawals
                    },
                    onError = { err -> coroutineScope.launch { snackbarHostState.showSnackbar(err) } },
                  )
                },
                onExportCsvClick = {
                  StatementExporter.exportAndShareCsv(
                    context = context,
                    studioFilterName = uiState.selectedStudioName,
                    shoots = uiState.filteredShoots,
                    withdrawals = if (uiState.selectedStudioId != null) {
                      uiState.withdrawals.filter { it.studioId == uiState.selectedStudioId }
                    } else {
                      uiState.withdrawals
                    },
                    onError = { err -> coroutineScope.launch { snackbarHostState.showSnackbar(err) } },
                  )
                },
              )
            }
          }
        }
      }
    }
  }

  // Shoot Add/Edit Modal
  if (showShootModal) {
    ShootEditorDialog(
      existingShoot = editingShoot,
      studios = uiState.studios,
      preselectedStudioId = modalPreselectedStudioId,
      currencySymbol = uiState.currencySymbol,
      onDismiss = { showShootModal = false },
      onSave = { title, selectedStudio, newStudioName, newStudioContact, category, shootDateMillis, location, durationValue, durationUnit, earnings, notes ->
        viewModel.saveShoot(
          existingShoot = editingShoot,
          title = title,
          selectedStudio = selectedStudio,
          newStudioName = newStudioName,
          newStudioContact = newStudioContact,
          category = category,
          shootDateMillis = shootDateMillis,
          location = location,
          durationValue = durationValue,
          durationUnit = durationUnit,
          earnings = earnings,
          notes = notes,
          onComplete = { showShootModal = false },
        )
      },
    )
  }

  // Withdrawal Add/Edit Modal
  if (showWithdrawalModal) {
    WithdrawalEditorDialog(
      existingWithdrawal = editingWithdrawal,
      studios = uiState.studios,
      studioSummaries = uiState.studioSummaries,
      preselectedStudioId = modalPreselectedStudioId,
      currencySymbol = uiState.currencySymbol,
      onDismiss = { showWithdrawalModal = false },
      onSave = { selectedStudio, newStudioName, amount, withdrawalDateMillis, paymentMethod, notes ->
        viewModel.saveWithdrawal(
          existingWithdrawal = editingWithdrawal,
          selectedStudio = selectedStudio,
          newStudioName = newStudioName,
          amount = amount,
          withdrawalDateMillis = withdrawalDateMillis,
          paymentMethod = paymentMethod,
          notes = notes,
          onComplete = { showWithdrawalModal = false },
        )
      },
    )
  }

  // Studio Add/Edit Modal
  if (showStudioModal) {
    StudioEditorDialog(
      existingStudio = editingStudio,
      onDismiss = { showStudioModal = false },
      onSave = { name, contact ->
        val currentEditing = editingStudio
        if (currentEditing == null) {
          viewModel.addStudio(name, contact) {
            showStudioModal = false
          }
        } else {
          viewModel.updateStudio(currentEditing, name, contact)
          showStudioModal = false
        }
      },
    )
  }

  // Delete Confirmation Modal
  when (val target = deleteTarget) {
    is DeleteTarget.Shoot -> {
      ConfirmDeleteDialog(
        title = "Delete Shoot Record?",
        message = "Are you sure you want to permanently delete \"${target.shoot.title}\" from your lifetime cloud ledger?",
        onConfirmDelete = { viewModel.deleteShoot(target.shoot) },
        onDismiss = { deleteTarget = null },
      )
    }
    is DeleteTarget.Withdrawal -> {
      ConfirmDeleteDialog(
        title = "Delete Studio Withdrawal?",
        message = "Are you sure you want to permanently delete this ${target.withdrawal.paymentMethod} withdrawal from ${target.withdrawal.studioName}?",
        onConfirmDelete = { viewModel.deleteWithdrawal(target.withdrawal) },
        onDismiss = { deleteTarget = null },
      )
    }
    is DeleteTarget.StudioItem -> {
      ConfirmDeleteDialog(
        title = "Delete Studio?",
        message = "Are you sure you want to remove \"${target.studio.name}\" from your studio list?",
        onConfirmDelete = { viewModel.deleteStudio(target.studio) },
        onDismiss = { deleteTarget = null },
      )
    }
    null -> Unit
  }
}
