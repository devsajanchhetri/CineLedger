package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CineConstants
import com.example.data.model.ShootRecord
import com.example.data.model.StudioBalanceSummary
import com.example.ui.components.netflixTextFieldColors
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
import com.example.ui.theme.NetflixSurface
import com.example.ui.theme.NetflixWhite
import com.example.ui.viewmodel.CineLedgerUiState
import com.example.util.CurrencyFormatter

@Composable
fun DashboardScreen(
  uiState: CineLedgerUiState,
  userEmail: String?,
  onLogShootClick: () -> Unit,
  onRecordWithdrawalClick: () -> Unit,
  onAddStudioClick: () -> Unit,
  onEditShootClick: (ShootRecord) -> Unit,
  onDeleteShootClick: (ShootRecord) -> Unit,
  onStudioCardClick: (String) -> Unit,
  onCategorySelected: (String) -> Unit,
  onClearStudioFilter: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onCurrencyChange: (String) -> Unit,
  onExportPdfClick: () -> Unit,
  onExportCsvClick: () -> Unit,
  onSignOutClick: () -> Unit,
) {
  var showCurrencyMenu by remember { mutableStateOf(false) }
  var showExportMenu by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(NetflixBlack),
    contentPadding = PaddingValues(bottom = 96.dp),
  ) {
    // 1. Netflix Top Brand Bar
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(RoundedCornerShape(7.dp))
              .background(NetflixRed),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.Videocam,
              contentDescription = null,
              tint = NetflixWhite,
              modifier = Modifier.size(20.dp),
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "CINELEDGER",
              style = MaterialTheme.typography.headlineLarge,
              color = NetflixRed,
              letterSpacing = 1.5.sp,
            )
            if (!userEmail.isNullOrBlank()) {
              Text(
                text = userEmail,
                style = MaterialTheme.typography.labelSmall,
                color = NetflixSilver,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          // Currency Picker Pill
          Box {
            Surface(
              color = NetflixCard,
              shape = RoundedCornerShape(50),
              modifier = Modifier
                .border(1.dp, NetflixBorder, RoundedCornerShape(50))
                .clickable { showCurrencyMenu = true }
                .testTag("currency_selector_button"),
            ) {
              Text(
                text = uiState.currencySymbol,
                style = MaterialTheme.typography.labelLarge,
                color = NetflixGold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
              )
            }
            DropdownMenu(
              expanded = showCurrencyMenu,
              onDismissRequest = { showCurrencyMenu = false },
              modifier = Modifier.background(NetflixCardElevated),
            ) {
              CineConstants.CURRENCIES.forEach { symbol ->
                DropdownMenuItem(
                  text = { Text("Currency: $symbol", color = NetflixWhite) },
                  onClick = {
                    onCurrencyChange(symbol)
                    showCurrencyMenu = false
                  }
                )
              }
            }
          }

          // Statement Export Menu Button
          Box {
            IconButton(
              onClick = { showExportMenu = true },
              modifier = Modifier.testTag("export_statement_button"),
            ) {
              Icon(
                imageVector = Icons.Filled.IosShare,
                contentDescription = "Export Studio Statement",
                tint = NetflixWhite,
              )
            }
            DropdownMenu(
              expanded = showExportMenu,
              onDismissRequest = { showExportMenu = false },
              modifier = Modifier.background(NetflixCardElevated),
            ) {
              DropdownMenuItem(
                text = { Text("Export PDF Statement", color = NetflixWhite) },
                onClick = {
                  showExportMenu = false
                  onExportPdfClick()
                }
              )
              DropdownMenuItem(
                text = { Text("Export CSV Spreadsheet", color = NetflixWhite) },
                onClick = {
                  showExportMenu = false
                  onExportCsvClick()
                }
              )
            }
          }

          IconButton(
            onClick = onSignOutClick,
            modifier = Modifier.testTag("sign_out_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.Logout,
              contentDescription = "Sign Out",
              tint = NetflixSilver,
            )
          }
        }
      }
    }

    // 2. Netflix Billboard Hero Financial Summary
    item {
      BillboardSummaryCard(
        currencySymbol = uiState.currencySymbol,
        availableBalance = uiState.totalAvailableBalance,
        totalEarned = uiState.totalLifetimeEarnings,
        totalWithdrawn = uiState.totalLifetimeWithdrawn,
        totalShootsCount = uiState.shoots.size,
        totalStudiosCount = uiState.studios.size,
        onLogShootClick = onLogShootClick,
        onRecordWithdrawalClick = onRecordWithdrawalClick,
      )
    }

    // 3. Horizontal Studio Balances Spotlight Rail
    item {
      Column(modifier = Modifier.padding(top = 20.dp)) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = "STUDIO BALANCES & ACCOUNTS",
              style = MaterialTheme.typography.headlineLarge,
              color = NetflixWhite,
            )
            Text(
              text = "Tap a studio to filter or inspect its balance",
              style = MaterialTheme.typography.bodySmall,
              color = NetflixSilver,
            )
          }
          TextButton(
            onClick = onAddStudioClick,
            modifier = Modifier.testTag("add_studio_quick_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = null,
              tint = NetflixRed,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Studio", color = NetflixRed, style = MaterialTheme.typography.labelLarge)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.studioSummaries.isEmpty()) {
          Surface(
            color = NetflixCard,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp)
              .border(1.dp, NetflixBorder, RoundedCornerShape(14.dp))
              .clickable { onAddStudioClick() },
          ) {
            Row(
              modifier = Modifier.padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(NetflixRed.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Filled.Business,
                  contentDescription = null,
                  tint = NetflixRed,
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "No Studios Added Yet — Balance ${CurrencyFormatter.format(0.0, uiState.currencySymbol)}",
                  style = MaterialTheme.typography.titleMedium,
                  color = NetflixWhite,
                )
                Text(
                  text = "Add the production houses you shoot for or log your first shoot directly.",
                  style = MaterialTheme.typography.bodySmall,
                  color = NetflixSilver,
                )
              }
            }
          }
        } else {
          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            items(uiState.studioSummaries, key = { it.studio.id }) { summary ->
              StudioSpotlightCard(
                summary = summary,
                isSelected = uiState.selectedStudioId == summary.studio.id,
                currencySymbol = uiState.currencySymbol,
                onClick = { onStudioCardClick(summary.studio.id) },
              )
            }
          }
        }
      }
    }

    // 4. Category Pills & Search Filter Bar
    item {
      Column(modifier = Modifier.padding(top = 24.dp)) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "EVENT SHOOTS & PROJECT EARNINGS",
            style = MaterialTheme.typography.headlineLarge,
            color = NetflixWhite,
          )
          if (uiState.selectedStudioName != null) {
            Surface(
              color = NetflixRed.copy(alpha = 0.2f),
              shape = RoundedCornerShape(50),
              modifier = Modifier
                .border(1.dp, NetflixRed, RoundedCornerShape(50))
                .clickable { onClearStudioFilter() },
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = uiState.selectedStudioName,
                  style = MaterialTheme.typography.labelSmall,
                  color = NetflixWhite,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Filled.Clear,
                  contentDescription = "Clear Studio Filter",
                  tint = NetflixWhite,
                  modifier = Modifier.size(14.dp),
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Input
        OutlinedTextField(
          value = uiState.searchQuery,
          onValueChange = onSearchQueryChange,
          placeholder = { Text("Search event title, studio, or city...") },
          leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = null, tint = NetflixSilver)
          },
          trailingIcon = {
            if (uiState.searchQuery.isNotEmpty()) {
              IconButton(onClick = { onSearchQueryChange("") }) {
                Icon(Icons.Filled.Clear, contentDescription = "Clear Search", tint = NetflixSilver)
              }
            }
          },
          singleLine = true,
          colors = netflixTextFieldColors(),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("shoot_search_input"),
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Netflix-style Category Pills
        val categories = listOf("All") + CineConstants.SHOOT_CATEGORIES
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          items(categories) { category ->
            val selected = uiState.selectedCategory == category
            FilterChip(
              selected = selected,
              onClick = { onCategorySelected(category) },
              label = { Text(category) },
              shape = RoundedCornerShape(50),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NetflixRed,
                selectedLabelColor = NetflixWhite,
                containerColor = NetflixCard,
                labelColor = NetflixSilver,
              ),
            )
          }
        }
      }
    }

    // 5. Shoot Records List or Clean Zero-State
    if (uiState.filteredShoots.isEmpty()) {
      item {
        EmptyShootsCard(
          hasAnyShoots = uiState.shoots.isNotEmpty(),
          currencySymbol = uiState.currencySymbol,
          onLogShootClick = onLogShootClick,
        )
      }
    } else {
      items(uiState.filteredShoots, key = { it.id }) { shoot ->
        ShootProjectCard(
          shoot = shoot,
          currencySymbol = uiState.currencySymbol,
          onEdit = { onEditShootClick(shoot) },
          onDelete = { onDeleteShootClick(shoot) },
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
      }
    }
  }
}

@Composable
private fun BillboardSummaryCard(
  currencySymbol: String,
  availableBalance: Double,
  totalEarned: Double,
  totalWithdrawn: Double,
  totalShootsCount: Int,
  totalStudiosCount: Int,
  onLogShootClick: () -> Unit,
  onRecordWithdrawalClick: () -> Unit,
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .clip(RoundedCornerShape(20.dp))
      .background(NetflixSurface)
      .border(1.dp, NetflixBorder, RoundedCornerShape(20.dp)),
  ) {
    // Netflix Billboard Spotlight Gradient Background
    Canvas(modifier = Modifier.matchParentSize()) {
      val w = size.width
      val h = size.height
      drawRect(
        brush = Brush.linearGradient(
          colors = listOf(
            Color(0xFF330609),
            Color(0xFF181818),
            Color(0xFF0E0E0E),
          ),
          start = Offset(0f, 0f),
          end = Offset(w, h),
        )
      )
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(NetflixRed.copy(alpha = 0.25f), Color.Transparent),
          center = Offset(w * 0.85f, h * 0.2f),
          radius = w * 0.55f,
        ),
        center = Offset(w * 0.85f, h * 0.2f),
        radius = w * 0.55f,
      )
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(NetflixRed)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        ) {
          Text(
            text = "LIVE LEDGER",
            style = MaterialTheme.typography.labelSmall,
            color = NetflixWhite,
            fontWeight = FontWeight.Bold,
          )
        }
        Text(
          text = "$totalShootsCount SHOOTS • $totalStudiosCount STUDIOS",
          style = MaterialTheme.typography.labelMedium,
          color = NetflixSilver,
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "AVAILABLE BALANCE TO COLLECT",
        style = MaterialTheme.typography.labelMedium,
        color = NetflixSilver,
      )

      Text(
        text = CurrencyFormatter.format(availableBalance, currencySymbol),
        style = MaterialTheme.typography.displayLarge,
        color = if (availableBalance >= 0) NetflixMatchGreen else NetflixRed,
        modifier = Modifier.testTag("hero_available_balance"),
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Dual Summary Metric Boxes (Total Earned vs Studio Withdrawals)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Column(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(NetflixBlack.copy(alpha = 0.65f))
            .border(1.dp, NetflixBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.TrendingUp,
              contentDescription = null,
              tint = NetflixMatchGreen,
              modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "TOTAL EARNED",
              style = MaterialTheme.typography.labelSmall,
              color = NetflixSilver,
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = CurrencyFormatter.format(totalEarned, currencySymbol),
            style = MaterialTheme.typography.titleLarge,
            color = NetflixWhite,
            modifier = Modifier.testTag("hero_total_earned"),
          )
        }

        Column(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(NetflixBlack.copy(alpha = 0.65f))
            .border(1.dp, NetflixBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.AccountBalanceWallet,
              contentDescription = null,
              tint = NetflixAmber,
              modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "WITHDRAWN",
              style = MaterialTheme.typography.labelSmall,
              color = NetflixSilver,
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = CurrencyFormatter.format(totalWithdrawn, currencySymbol),
            style = MaterialTheme.typography.titleLarge,
            color = NetflixAmber,
            modifier = Modifier.testTag("hero_total_withdrawn"),
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Netflix Play / My List style Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Button(
          onClick = onLogShootClick,
          colors = ButtonDefaults.buttonColors(
            containerColor = NetflixRed,
            contentColor = NetflixWhite,
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("hero_add_shoot_button"),
        ) {
          Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Log Shoot",
            style = MaterialTheme.typography.labelLarge,
          )
        }

        OutlinedButton(
          onClick = onRecordWithdrawalClick,
          border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = SolidColor(NetflixWhite.copy(alpha = 0.35f))
          ),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = NetflixCardElevated.copy(alpha = 0.85f),
            contentColor = NetflixWhite,
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("hero_add_withdrawal_button"),
        ) {
          Icon(
            imageVector = Icons.Filled.AccountBalanceWallet,
            contentDescription = null,
            tint = NetflixGold,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Withdrawal",
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }
    }
  }
}

@Composable
private fun StudioSpotlightCard(
  summary: StudioBalanceSummary,
  isSelected: Boolean,
  currencySymbol: String,
  onClick: () -> Unit,
) {
  val borderColor = if (isSelected) NetflixRed else NetflixBorder
  val progress = if (summary.totalEarned > 0.0) {
    (summary.totalWithdrawn / summary.totalEarned).toFloat().coerceIn(0f, 1f)
  } else {
    0f
  }

  Surface(
    color = NetflixCard,
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier
      .width(235.dp)
      .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
      .clickable(onClick = onClick),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = summary.studio.name,
          style = MaterialTheme.typography.titleMedium,
          color = NetflixWhite,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
        Surface(
          color = NetflixCardElevated,
          shape = RoundedCornerShape(6.dp),
        ) {
          Text(
            text = "${summary.shootCount} shoots",
            style = MaterialTheme.typography.labelSmall,
            color = NetflixGold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "AVAILABLE BALANCE",
        style = MaterialTheme.typography.labelSmall,
        color = NetflixMuted,
      )
      Text(
        text = CurrencyFormatter.format(summary.availableBalance, currencySymbol),
        style = MaterialTheme.typography.headlineLarge,
        color = NetflixMatchGreen,
      )

      Spacer(modifier = Modifier.height(8.dp))

      LinearProgressIndicator(
        progress = { progress },
        color = NetflixRed,
        trackColor = NetflixCardElevated,
        modifier = Modifier
          .fillMaxWidth()
          .height(5.dp)
          .clip(RoundedCornerShape(50)),
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "Earned: ${CurrencyFormatter.format(summary.totalEarned, currencySymbol)}",
          style = MaterialTheme.typography.labelSmall,
          color = NetflixSilver,
        )
        Text(
          text = "Paid: ${CurrencyFormatter.format(summary.totalWithdrawn, currencySymbol)}",
          style = MaterialTheme.typography.labelSmall,
          color = NetflixAmber,
        )
      }
    }
  }
}

@Composable
fun ShootProjectCard(
  shoot: ShootRecord,
  currencySymbol: String,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    color = NetflixCard,
    shape = RoundedCornerShape(14.dp),
    modifier = modifier
      .fillMaxWidth()
      .border(1.dp, NetflixBorder, RoundedCornerShape(14.dp))
      .clickable(onClick = onEdit),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Surface(
              color = NetflixRed.copy(alpha = 0.18f),
              shape = RoundedCornerShape(5.dp),
            ) {
              Text(
                text = shoot.category.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = NetflixRed,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
              )
            }
            Text(
              text = shoot.studioName,
              style = MaterialTheme.typography.labelMedium,
              color = NetflixGold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = shoot.title,
            style = MaterialTheme.typography.titleLarge,
            color = NetflixWhite,
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "+${CurrencyFormatter.format(shoot.earnings, currencySymbol)}",
            style = MaterialTheme.typography.headlineLarge,
            color = NetflixMatchGreen,
          )
          Text(
            text = "PROJECT EARNING",
            style = MaterialTheme.typography.labelSmall,
            color = NetflixMuted,
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.CalendarToday,
              contentDescription = null,
              tint = NetflixSilver,
              modifier = Modifier.size(13.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = CurrencyFormatter.formatDate(shoot.shootDateMillis),
              style = MaterialTheme.typography.bodySmall,
              color = NetflixSilver,
            )
          }

          val durFormatted = if (shoot.durationValue % 1.0 == 0.0) {
            "${shoot.durationValue.toInt()} ${shoot.durationUnit}"
          } else {
            "${shoot.durationValue} ${shoot.durationUnit}"
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.Schedule,
              contentDescription = null,
              tint = NetflixSilver,
              modifier = Modifier.size(13.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = durFormatted,
              style = MaterialTheme.typography.bodySmall,
              color = NetflixSilver,
            )
          }

          if (shoot.location.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = NetflixSilver,
                modifier = Modifier.size(13.dp),
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = shoot.location,
                style = MaterialTheme.typography.bodySmall,
                color = NetflixSilver,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        Row {
          IconButton(
            onClick = onEdit,
            modifier = Modifier.size(36.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Edit,
              contentDescription = "Edit Shoot",
              tint = NetflixSilver,
              modifier = Modifier.size(18.dp),
            )
          }
          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.DeleteOutline,
              contentDescription = "Delete Shoot",
              tint = NetflixRed,
              modifier = Modifier.size(18.dp),
            )
          }
        }
      }

      if (shoot.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = shoot.notes,
          style = MaterialTheme.typography.bodySmall,
          color = NetflixMuted,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun EmptyShootsCard(
  hasAnyShoots: Boolean,
  currencySymbol: String,
  onLogShootClick: () -> Unit,
) {
  Surface(
    color = NetflixCard,
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .border(1.dp, NetflixBorder, RoundedCornerShape(16.dp)),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(NetflixRed.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.Movie,
          contentDescription = null,
          tint = NetflixRed,
          modifier = Modifier.size(32.dp),
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = if (hasAnyShoots) {
          "NO MATCHING SHOOTS FOUND"
        } else {
          "YOUR SHOOT LEDGER IS AT ${CurrencyFormatter.format(0.0, currencySymbol)}"
        },
        style = MaterialTheme.typography.headlineLarge,
        color = NetflixWhite,
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = if (hasAnyShoots) {
          "Try clearing your category or search filter to see all saved shoots."
        } else {
          "Start logging your completed event shoots. Every record is saved in your cloud account for lifetime until you delete it."
        },
        style = MaterialTheme.typography.bodyMedium,
        color = NetflixSilver,
      )

      if (!hasAnyShoots) {
        Spacer(modifier = Modifier.height(18.dp))
        Button(
          onClick = onLogShootClick,
          colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("empty_state_add_shoot_button"),
        ) {
          Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Log Your First Shoot", color = NetflixWhite, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
