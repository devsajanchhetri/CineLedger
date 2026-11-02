package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.LedgerTransactionItem
import com.example.data.model.ShootRecord
import com.example.data.model.Studio
import com.example.data.model.StudioBalanceSummary
import com.example.data.model.TransactionKind
import com.example.data.model.WithdrawalRecord
import com.example.ui.theme.NetflixAmber
import com.example.ui.theme.NetflixBlack
import com.example.ui.theme.NetflixBorder
import com.example.ui.theme.NetflixCard
import com.example.ui.theme.NetflixCardElevated
import com.example.ui.theme.NetflixGold
import com.example.ui.theme.NetflixMatchGreen
import com.example.ui.theme.NetflixMuted
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.NetflixSilver
import com.example.ui.theme.NetflixWhite
import com.example.ui.viewmodel.CineLedgerUiState
import com.example.ui.viewmodel.LedgerFilterKind
import com.example.util.CurrencyFormatter

@Composable
fun StudiosScreen(
  uiState: CineLedgerUiState,
  inspectedStudioId: String?,
  onInspectStudio: (String?) -> Unit,
  onAddStudioClick: () -> Unit,
  onEditStudioClick: (Studio) -> Unit,
  onDeleteStudioClick: (Studio) -> Unit,
  onLogShootForStudio: (String) -> Unit,
  onRecordWithdrawalForStudio: (String) -> Unit,
  onEditShootClick: (ShootRecord) -> Unit,
  onDeleteShootClick: (ShootRecord) -> Unit,
  onEditWithdrawalClick: (WithdrawalRecord) -> Unit,
  onDeleteWithdrawalClick: (WithdrawalRecord) -> Unit,
  onExportStudioPdf: (StudioBalanceSummary) -> Unit,
  onExportStudioCsv: (StudioBalanceSummary) -> Unit,
) {
  val inspectedSummary = uiState.studioSummaries.find { it.studio.id == inspectedStudioId }

  if (inspectedSummary != null) {
    BackHandler { onInspectStudio(null) }
    StudioDetailLedgerView(
      summary = inspectedSummary,
      shoots = uiState.shoots.filter { it.studioId == inspectedSummary.studio.id },
      withdrawals = uiState.withdrawals.filter { it.studioId == inspectedSummary.studio.id },
      currencySymbol = uiState.currencySymbol,
      onBack = { onInspectStudio(null) },
      onEditStudio = { onEditStudioClick(inspectedSummary.studio) },
      onDeleteStudio = { onDeleteStudioClick(inspectedSummary.studio) },
      onLogShoot = { onLogShootForStudio(inspectedSummary.studio.id) },
      onRecordWithdrawal = { onRecordWithdrawalForStudio(inspectedSummary.studio.id) },
      onEditShoot = onEditShootClick,
      onDeleteShoot = onDeleteShootClick,
      onEditWithdrawal = onEditWithdrawalClick,
      onDeleteWithdrawal = onDeleteWithdrawalClick,
      onExportPdf = { onExportStudioPdf(inspectedSummary) },
      onExportCsv = { onExportStudioCsv(inspectedSummary) },
    )
    return
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(NetflixBlack),
    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "STUDIOS & BALANCES",
            style = MaterialTheme.typography.displaySmall,
            color = NetflixWhite,
          )
          Text(
            text = "Track earnings, withdrawals & pending balance per studio",
            style = MaterialTheme.typography.bodySmall,
            color = NetflixSilver,
          )
        }

        Button(
          onClick = onAddStudioClick,
          colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("studios_tab_add_studio_button"),
        ) {
          Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("New Studio", color = NetflixWhite, fontWeight = FontWeight.Bold)
        }
      }
    }

    // Global Rollup Strip
    item {
      Surface(
        color = NetflixCard,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, NetflixBorder, RoundedCornerShape(14.dp)),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column {
            Text("TOTAL EARNED", style = MaterialTheme.typography.labelSmall, color = NetflixMuted)
            Text(
              text = CurrencyFormatter.format(uiState.totalLifetimeEarnings, uiState.currencySymbol),
              style = MaterialTheme.typography.titleLarge,
              color = NetflixWhite,
            )
          }
          Column {
            Text("WITHDRAWN", style = MaterialTheme.typography.labelSmall, color = NetflixMuted)
            Text(
              text = CurrencyFormatter.format(uiState.totalLifetimeWithdrawn, uiState.currencySymbol),
              style = MaterialTheme.typography.titleLarge,
              color = NetflixAmber,
            )
          }
          Column(horizontalAlignment = Alignment.End) {
            Text("NET AVAILABLE", style = MaterialTheme.typography.labelSmall, color = NetflixMuted)
            Text(
              text = CurrencyFormatter.format(uiState.totalAvailableBalance, uiState.currencySymbol),
              style = MaterialTheme.typography.titleLarge,
              color = NetflixMatchGreen,
            )
          }
        }
      }
    }

    if (uiState.studioSummaries.isEmpty()) {
      item {
        Surface(
          color = NetflixCard,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NetflixBorder, RoundedCornerShape(16.dp)),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Icon(
              imageVector = Icons.Filled.Business,
              contentDescription = null,
              tint = NetflixRed,
              modifier = Modifier.size(40.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "NO STUDIOS SAVED YET",
              style = MaterialTheme.typography.headlineLarge,
              color = NetflixWhite,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Add the studios you shoot for to track how much each studio owes you and every withdrawal you've taken.",
              style = MaterialTheme.typography.bodyMedium,
              color = NetflixSilver,
            )
          }
        }
      }
    } else {
      items(uiState.studioSummaries, key = { it.studio.id }) { summary ->
        val progress = if (summary.totalEarned > 0.0) {
          (summary.totalWithdrawn / summary.totalEarned).toFloat().coerceIn(0f, 1f)
        } else {
          0f
        }

        Surface(
          color = NetflixCard,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NetflixBorder, RoundedCornerShape(16.dp))
            .clickable { onInspectStudio(summary.studio.id) },
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Top,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = summary.studio.name,
                  style = MaterialTheme.typography.headlineMedium,
                  color = NetflixWhite,
                )
                if (summary.studio.contactPerson.isNotBlank()) {
                  Text(
                    text = summary.studio.contactPerson,
                    style = MaterialTheme.typography.bodySmall,
                    color = NetflixSilver,
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "${summary.shootCount} Shoots • ${summary.withdrawalCount} Withdrawals",
                  style = MaterialTheme.typography.labelMedium,
                  color = NetflixGold,
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "AVAILABLE TO COLLECT",
                  style = MaterialTheme.typography.labelSmall,
                  color = NetflixMuted,
                )
                Text(
                  text = CurrencyFormatter.format(summary.availableBalance, uiState.currencySymbol),
                  style = MaterialTheme.typography.displaySmall,
                  color = NetflixMatchGreen,
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
              progress = { progress },
              color = NetflixRed,
              trackColor = NetflixCardElevated,
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = "Shoot Earnings: ${CurrencyFormatter.format(summary.totalEarned, uiState.currencySymbol)}",
                style = MaterialTheme.typography.bodySmall,
                color = NetflixSilver,
              )
              Text(
                text = "Withdrawn: ${CurrencyFormatter.format(summary.totalWithdrawn, uiState.currencySymbol)}",
                style = MaterialTheme.typography.bodySmall,
                color = NetflixAmber,
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Button(
                onClick = { onLogShootForStudio(summary.studio.id) },
                colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
              ) {
                Text("+ Shoot", color = NetflixWhite, style = MaterialTheme.typography.labelMedium)
              }
              OutlinedButton(
                onClick = { onRecordWithdrawalForStudio(summary.studio.id) },
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                  brush = SolidColor(NetflixBorder)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
              ) {
                Text("↓ Withdraw", color = NetflixAmber, style = MaterialTheme.typography.labelMedium)
              }
              IconButton(onClick = { onExportStudioPdf(summary) }) {
                Icon(Icons.Filled.PictureAsPdf, contentDescription = "Export Studio PDF", tint = NetflixWhite)
              }
              IconButton(onClick = { onEditStudioClick(summary.studio) }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Studio", tint = NetflixSilver)
              }
              IconButton(onClick = { onDeleteStudioClick(summary.studio) }) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete Studio", tint = NetflixRed)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StudioDetailLedgerView(
  summary: StudioBalanceSummary,
  shoots: List<ShootRecord>,
  withdrawals: List<WithdrawalRecord>,
  currencySymbol: String,
  onBack: () -> Unit,
  onEditStudio: () -> Unit,
  onDeleteStudio: () -> Unit,
  onLogShoot: () -> Unit,
  onRecordWithdrawal: () -> Unit,
  onEditShoot: (ShootRecord) -> Unit,
  onDeleteShoot: (ShootRecord) -> Unit,
  onEditWithdrawal: (WithdrawalRecord) -> Unit,
  onDeleteWithdrawal: (WithdrawalRecord) -> Unit,
  onExportPdf: () -> Unit,
  onExportCsv: () -> Unit,
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(NetflixBlack),
    contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to Studios",
              tint = NetflixWhite,
            )
          }
          Column {
            Text(
              text = summary.studio.name,
              style = MaterialTheme.typography.headlineLarge,
              color = NetflixWhite,
            )
            if (summary.studio.contactPerson.isNotBlank()) {
              Text(
                text = summary.studio.contactPerson,
                style = MaterialTheme.typography.bodySmall,
                color = NetflixSilver,
              )
            }
          }
        }
        Row {
          IconButton(onClick = onExportPdf) {
            Icon(Icons.Filled.PictureAsPdf, contentDescription = "Export PDF", tint = NetflixRed)
          }
          IconButton(onClick = onExportCsv) {
            Icon(Icons.Filled.TableChart, contentDescription = "Export CSV", tint = NetflixGold)
          }
          IconButton(onClick = onEditStudio) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit Studio", tint = NetflixSilver)
          }
          IconButton(onClick = onDeleteStudio) {
            Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete Studio", tint = NetflixRed)
          }
        }
      }
    }

    item {
      Surface(
        color = NetflixCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, NetflixBorder, RoundedCornerShape(16.dp)),
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "STUDIO AVAILABLE BALANCE",
            style = MaterialTheme.typography.labelSmall,
            color = NetflixSilver,
          )
          Text(
            text = CurrencyFormatter.format(summary.availableBalance, currencySymbol),
            style = MaterialTheme.typography.displayMedium,
            color = NetflixMatchGreen,
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = "Total Earned: ${CurrencyFormatter.format(summary.totalEarned, currencySymbol)}",
              style = MaterialTheme.typography.bodyMedium,
              color = NetflixWhite,
            )
            Text(
              text = "Withdrawn: ${CurrencyFormatter.format(summary.totalWithdrawn, currencySymbol)}",
              style = MaterialTheme.typography.bodyMedium,
              color = NetflixAmber,
            )
          }
          Spacer(modifier = Modifier.height(14.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
              onClick = onLogShoot,
              colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
              modifier = Modifier.weight(1f),
            ) {
              Text("+ Log Shoot", color = NetflixWhite)
            }
            OutlinedButton(
              onClick = onRecordWithdrawal,
              modifier = Modifier.weight(1f),
            ) {
              Text("↓ Record Withdrawal", color = NetflixGold)
            }
          }
        }
      }
    }

    item {
      Text(
        text = "STUDIO WITHDRAWALS (${withdrawals.size})",
        style = MaterialTheme.typography.headlineLarge,
        color = NetflixWhite,
        modifier = Modifier.padding(top = 8.dp),
      )
    }

    if (withdrawals.isEmpty()) {
      item {
        Text(
          text = "No withdrawals taken from ${summary.studio.name} yet.",
          style = MaterialTheme.typography.bodySmall,
          color = NetflixSilver,
        )
      }
    } else {
      items(withdrawals, key = { "wd_${it.id}" }) { wd ->
        WithdrawalTransactionRowCard(
          withdrawal = wd,
          currencySymbol = currencySymbol,
          onEdit = { onEditWithdrawal(wd) },
          onDelete = { onDeleteWithdrawal(wd) },
        )
      }
    }

    item {
      Text(
        text = "SHOOTS FOR THIS STUDIO (${shoots.size})",
        style = MaterialTheme.typography.headlineLarge,
        color = NetflixWhite,
        modifier = Modifier.padding(top = 12.dp),
      )
    }

    if (shoots.isEmpty()) {
      item {
        Text(
          text = "No shoots logged for ${summary.studio.name} yet.",
          style = MaterialTheme.typography.bodySmall,
          color = NetflixSilver,
        )
      }
    } else {
      items(shoots, key = { "shoot_${it.id}" }) { shoot ->
        ShootProjectCard(
          shoot = shoot,
          currencySymbol = currencySymbol,
          onEdit = { onEditShoot(shoot) },
          onDelete = { onDeleteShoot(shoot) },
        )
      }
    }
  }
}

@Composable
fun TransactionsLedgerScreen(
  uiState: CineLedgerUiState,
  onRecordWithdrawalClick: () -> Unit,
  onLogShootClick: () -> Unit,
  onFilterKindSelected: (LedgerFilterKind) -> Unit,
  onStudioFilterSelected: (String?) -> Unit,
  onEditShootClick: (ShootRecord) -> Unit,
  onDeleteShootClick: (ShootRecord) -> Unit,
  onEditWithdrawalClick: (WithdrawalRecord) -> Unit,
  onDeleteWithdrawalClick: (WithdrawalRecord) -> Unit,
  onExportPdfClick: () -> Unit,
  onExportCsvClick: () -> Unit,
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(NetflixBlack),
    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "STUDIO TRANSACTIONS & WITHDRAWALS",
            style = MaterialTheme.typography.displaySmall,
            color = NetflixWhite,
          )
          Text(
            text = "Complete chronological history of studio withdrawals & shoot credits",
            style = MaterialTheme.typography.bodySmall,
            color = NetflixSilver,
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          IconButton(onClick = onExportPdfClick) {
            Icon(
              imageVector = Icons.Filled.PictureAsPdf,
              contentDescription = "Export PDF Statement",
              tint = NetflixRed,
            )
          }
          IconButton(onClick = onExportCsvClick) {
            Icon(
              imageVector = Icons.Filled.TableChart,
              contentDescription = "Export CSV Statement",
              tint = NetflixGold,
            )
          }
        }
      }
    }

    // Summary Card with Quick Withdrawal Button
    item {
      Surface(
        color = NetflixCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, NetflixBorder, RoundedCornerShape(16.dp)),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = "TOTAL WITHDRAWN FROM STUDIOS",
                style = MaterialTheme.typography.labelSmall,
                color = NetflixSilver,
              )
              Text(
                text = CurrencyFormatter.format(uiState.totalLifetimeWithdrawn, uiState.currencySymbol),
                style = MaterialTheme.typography.displaySmall,
                color = NetflixAmber,
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "REMAINING BALANCE",
                style = MaterialTheme.typography.labelSmall,
                color = NetflixSilver,
              )
              Text(
                text = CurrencyFormatter.format(uiState.totalAvailableBalance, uiState.currencySymbol),
                style = MaterialTheme.typography.displaySmall,
                color = NetflixMatchGreen,
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = onRecordWithdrawalClick,
            colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp)
              .testTag("ledger_record_withdrawal_button"),
          ) {
            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Record New Studio Withdrawal", color = NetflixWhite, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Filter Chips: All / Withdrawals Only / Shoot Credits Only
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = uiState.ledgerFilterKind == LedgerFilterKind.ALL,
            onClick = { onFilterKindSelected(LedgerFilterKind.ALL) },
            label = { Text("All Transactions") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = NetflixRed,
              selectedLabelColor = NetflixWhite,
              containerColor = NetflixCard,
              labelColor = NetflixSilver,
            ),
          )
          FilterChip(
            selected = uiState.ledgerFilterKind == LedgerFilterKind.WITHDRAWALS_ONLY,
            onClick = { onFilterKindSelected(LedgerFilterKind.WITHDRAWALS_ONLY) },
            label = { Text("Withdrawals (${uiState.withdrawals.size})") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = NetflixAmber,
              selectedLabelColor = Color.Black,
              containerColor = NetflixCard,
              labelColor = NetflixSilver,
            ),
          )
          FilterChip(
            selected = uiState.ledgerFilterKind == LedgerFilterKind.SHOOTS_ONLY,
            onClick = { onFilterKindSelected(LedgerFilterKind.SHOOTS_ONLY) },
            label = { Text("Shoots (${uiState.shoots.size})") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = NetflixMatchGreen,
              selectedLabelColor = Color.Black,
              containerColor = NetflixCard,
              labelColor = NetflixSilver,
            ),
          )
        }

        if (uiState.studios.isNotEmpty()) {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
              FilterChip(
                selected = uiState.selectedStudioId == null,
                onClick = { onStudioFilterSelected(null) },
                label = { Text("All Studios") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = NetflixGold,
                  selectedLabelColor = Color.Black,
                  containerColor = NetflixCard,
                  labelColor = NetflixSilver,
                ),
              )
            }
            items(uiState.studios, key = { it.id }) { studio ->
              FilterChip(
                selected = uiState.selectedStudioId == studio.id,
                onClick = { onStudioFilterSelected(studio.id) },
                label = { Text(studio.name) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = NetflixGold,
                  selectedLabelColor = Color.Black,
                  containerColor = NetflixCard,
                  labelColor = NetflixSilver,
                ),
              )
            }
          }
        }
      }
    }

    if (uiState.filteredTransactions.isEmpty()) {
      item {
        Surface(
          color = NetflixCard,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NetflixBorder, RoundedCornerShape(16.dp)),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Icon(
              imageVector = Icons.Filled.AccountBalanceWallet,
              contentDescription = null,
              tint = NetflixAmber,
              modifier = Modifier.size(38.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "NO STUDIO TRANSACTIONS YET",
              style = MaterialTheme.typography.headlineLarge,
              color = NetflixWhite,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Every shoot earning and studio withdrawal you save will appear here chronologically.",
              style = MaterialTheme.typography.bodyMedium,
              color = NetflixSilver,
            )
          }
        }
      }
    } else {
      items(uiState.filteredTransactions, key = { it.id }) { tx ->
        LedgerTransactionCard(
          item = tx,
          currencySymbol = uiState.currencySymbol,
          onEdit = {
            tx.shootRecord?.let(onEditShootClick)
            tx.withdrawalRecord?.let(onEditWithdrawalClick)
          },
          onDelete = {
            tx.shootRecord?.let(onDeleteShootClick)
            tx.withdrawalRecord?.let(onDeleteWithdrawalClick)
          },
        )
      }
    }
  }
}

@Composable
private fun WithdrawalTransactionRowCard(
  withdrawal: WithdrawalRecord,
  currencySymbol: String,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  Surface(
    color = NetflixCard,
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, NetflixBorder, RoundedCornerShape(12.dp))
      .clickable(onClick = onEdit),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f),
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(NetflixAmber.copy(alpha = 0.18f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Filled.ArrowDownward,
            contentDescription = null,
            tint = NetflixAmber,
            modifier = Modifier.size(20.dp),
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "${withdrawal.paymentMethod} Withdrawal",
            style = MaterialTheme.typography.titleMedium,
            color = NetflixWhite,
          )
          Text(
            text = CurrencyFormatter.formatDate(withdrawal.withdrawalDateMillis) +
              if (withdrawal.notes.isNotBlank()) " • ${withdrawal.notes}" else "",
            style = MaterialTheme.typography.bodySmall,
            color = NetflixSilver,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "-${CurrencyFormatter.format(withdrawal.amount, currencySymbol)}",
          style = MaterialTheme.typography.headlineLarge,
          color = NetflixAmber,
        )
        IconButton(onClick = onDelete) {
          Icon(
            imageVector = Icons.Filled.DeleteOutline,
            contentDescription = "Delete Withdrawal",
            tint = NetflixRed,
            modifier = Modifier.size(18.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun LedgerTransactionCard(
  item: LedgerTransactionItem,
  currencySymbol: String,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  val isShoot = item.kind == TransactionKind.SHOOT_EARNING
  val accentColor = if (isShoot) NetflixMatchGreen else NetflixAmber
  val signPrefix = if (isShoot) "+" else "-"

  Surface(
    color = NetflixCard,
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, NetflixBorder, RoundedCornerShape(14.dp))
      .clickable(onClick = onEdit),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f),
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.16f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = if (isShoot) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(20.dp),
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Surface(
              color = accentColor.copy(alpha = 0.18f),
              shape = RoundedCornerShape(4.dp),
            ) {
              Text(
                text = if (isShoot) "SHOOT EARNING" else "WITHDRAWAL",
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              )
            }
            Text(
              text = CurrencyFormatter.formatDate(item.dateMillis),
              style = MaterialTheme.typography.labelSmall,
              color = NetflixSilver,
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
            color = NetflixWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )

          Text(
            text = item.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = NetflixSilver,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "$signPrefix${CurrencyFormatter.format(item.amount, currencySymbol)}",
            style = MaterialTheme.typography.headlineLarge,
            color = accentColor,
          )
          Text(
            text = item.badgeText,
            style = MaterialTheme.typography.labelSmall,
            color = NetflixMuted,
          )
        }

        IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
          Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = "Edit Transaction",
            tint = NetflixSilver,
            modifier = Modifier.size(17.dp),
          )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
          Icon(
            imageVector = Icons.Filled.DeleteOutline,
            contentDescription = "Delete Transaction",
            tint = NetflixRed,
            modifier = Modifier.size(17.dp),
          )
        }
      }
    }
  }
}
