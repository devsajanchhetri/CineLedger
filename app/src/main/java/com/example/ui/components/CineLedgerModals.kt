package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CineConstants
import com.example.data.model.ShootRecord
import com.example.data.model.Studio
import com.example.data.model.StudioBalanceSummary
import com.example.data.model.WithdrawalRecord
import com.example.ui.theme.NetflixAmber
import com.example.ui.theme.NetflixBlack
import com.example.ui.theme.NetflixBorder
import com.example.ui.theme.NetflixCard
import com.example.ui.theme.NetflixCardElevated
import com.example.ui.theme.NetflixGold
import com.example.ui.theme.NetflixMatchGreen
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.NetflixSilver
import com.example.ui.theme.NetflixSurface
import com.example.ui.theme.NetflixWhite
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShootEditorDialog(
  existingShoot: ShootRecord?,
  studios: List<Studio>,
  preselectedStudioId: String?,
  currencySymbol: String,
  onDismiss: () -> Unit,
  onSave: (
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
  ) -> Unit,
) {
  var title by remember { mutableStateOf(existingShoot?.title ?: "") }
  var selectedStudioId by remember {
    mutableStateOf(
      existingShoot?.studioId
        ?: preselectedStudioId
        ?: studios.firstOrNull()?.id
    )
  }
  var isCreatingNewStudio by remember { mutableStateOf(studios.isEmpty()) }
  var newStudioName by remember { mutableStateOf("") }
  var newStudioContact by remember { mutableStateOf("") }
  var category by remember { mutableStateOf(existingShoot?.category ?: CineConstants.SHOOT_CATEGORIES.first()) }
  var shootDateMillis by remember {
    mutableLongStateOf(
      existingShoot?.shootDateMillis?.takeIf { it > 0L } ?: System.currentTimeMillis()
    )
  }
  var location by remember { mutableStateOf(existingShoot?.location ?: "") }
  var durationText by remember {
    mutableStateOf(
      existingShoot?.durationValue?.let {
        if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
      } ?: "1"
    )
  }
  var durationUnit by remember { mutableStateOf(existingShoot?.durationUnit ?: "Days") }
  var earningsText by remember {
    mutableStateOf(
      existingShoot?.earnings?.let {
        if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
      } ?: ""
    )
  }
  var notes by remember { mutableStateOf(existingShoot?.notes ?: "") }
  var validationError by remember { mutableStateOf<String?>(null) }
  var showDatePicker by remember { mutableStateOf(false) }

  if (showDatePicker) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = shootDateMillis)
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let { shootDateMillis = it }
            showDatePicker = false
          }
        ) {
          Text("Confirm", color = NetflixRed, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) {
          Text("Cancel", color = NetflixSilver)
        }
      },
    ) {
      DatePicker(state = datePickerState)
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      color = NetflixSurface,
      shape = RoundedCornerShape(18.dp),
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .border(1.dp, NetflixBorder, RoundedCornerShape(18.dp)),
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState()),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = if (existingShoot == null) "LOG EVENT SHOOT" else "EDIT EVENT SHOOT",
              style = MaterialTheme.typography.headlineLarge,
              color = NetflixWhite,
            )
            Text(
              text = "Record project details & confirmed studio earnings",
              style = MaterialTheme.typography.bodySmall,
              color = NetflixSilver,
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Filled.Close, contentDescription = "Close", tint = NetflixSilver)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (validationError != null) {
          Text(
            text = validationError ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = NetflixRed,
            modifier = Modifier.padding(bottom = 8.dp),
          )
        }

        OutlinedTextField(
          value = title,
          onValueChange = { title = it; validationError = null },
          label = { Text("Event / Shoot Title *") },
          placeholder = { Text("e.g., Aarav & Meera Royal Wedding") },
          singleLine = true,
          colors = netflixTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("shoot_title_input"),
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Studio Selection Section
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "PRODUCTION STUDIO *",
            style = MaterialTheme.typography.labelMedium,
            color = NetflixSilver,
          )
          if (studios.isNotEmpty()) {
            TextButton(
              onClick = { isCreatingNewStudio = !isCreatingNewStudio }
            ) {
              Text(
                text = if (isCreatingNewStudio) "Select Existing Studio" else "+ New Studio",
                color = NetflixRed,
                style = MaterialTheme.typography.labelMedium,
              )
            }
          }
        }

        if (isCreatingNewStudio || studios.isEmpty()) {
          OutlinedTextField(
            value = newStudioName,
            onValueChange = { newStudioName = it; validationError = null },
            label = { Text("New Studio / Production House Name *") },
            placeholder = { Text("e.g., Redline Cinema Studio") },
            singleLine = true,
            colors = netflixTextFieldColors(),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("shoot_new_studio_input"),
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = newStudioContact,
            onValueChange = { newStudioContact = it },
            label = { Text("Studio Producer / Contact (Optional)") },
            singleLine = true,
            colors = netflixTextFieldColors(),
            modifier = Modifier.fillMaxWidth(),
          )
        } else {
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            studios.forEach { studio ->
              val selected = studio.id == selectedStudioId
              FilterChip(
                selected = selected,
                onClick = { selectedStudioId = studio.id },
                label = { Text(studio.name) },
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

        Spacer(modifier = Modifier.height(14.dp))

        // Earnings & Shoot Date Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          OutlinedTextField(
            value = earningsText,
            onValueChange = { earningsText = it; validationError = null },
            label = { Text("Total Earnings ($currencySymbol) *") },
            placeholder = { Text("e.g., 25000") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = netflixTextFieldColors(),
            modifier = Modifier
              .weight(1f)
              .testTag("shoot_earnings_input"),
          )

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "SHOOT DATE",
              style = MaterialTheme.typography.labelSmall,
              color = NetflixSilver,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NetflixCard)
                .border(1.dp, NetflixBorder, RoundedCornerShape(8.dp))
                .clickable { showDatePicker = true }
                .padding(horizontal = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = "Pick Shoot Date",
                tint = NetflixRed,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = CurrencyFormatter.formatDate(shootDateMillis),
                style = MaterialTheme.typography.bodyMedium,
                color = NetflixWhite,
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Event Category
        Text(
          text = "SHOOT CATEGORY",
          style = MaterialTheme.typography.labelMedium,
          color = NetflixSilver,
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          CineConstants.SHOOT_CATEGORIES.forEach { cat ->
            FilterChip(
              selected = category == cat,
              onClick = { category = cat },
              label = { Text(cat) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NetflixRed,
                selectedLabelColor = NetflixWhite,
                containerColor = NetflixCard,
                labelColor = NetflixSilver,
              ),
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Location & Duration Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location / Venue") },
            placeholder = { Text("City or Venue") },
            singleLine = true,
            colors = netflixTextFieldColors(),
            modifier = Modifier
              .weight(1.2f)
              .testTag("shoot_location_input"),
          )

          OutlinedTextField(
            value = durationText,
            onValueChange = { durationText = it },
            label = { Text("Duration") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = netflixTextFieldColors(),
            modifier = Modifier.weight(0.7f),
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "Duration Unit:",
            style = MaterialTheme.typography.bodySmall,
            color = NetflixSilver,
          )
          CineConstants.DURATION_UNITS.forEach { unit ->
            FilterChip(
              selected = durationUnit == unit,
              onClick = { durationUnit = unit },
              label = { Text(unit) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NetflixGold,
                selectedLabelColor = Color.Black,
                containerColor = NetflixCard,
                labelColor = NetflixSilver,
              ),
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Equipment / Shoot Notes (Optional)") },
          placeholder = { Text("e.g., Sony FX3 + Ronin RS3, Drone aerials") },
          maxLines = 3,
          colors = netflixTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("shoot_notes_input"),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .height(50.dp),
            shape = RoundedCornerShape(8.dp),
          ) {
            Text("Cancel", color = NetflixSilver)
          }

          Button(
            onClick = {
              val parsedEarnings = earningsText.trim().toDoubleOrNull()
              val parsedDuration = durationText.trim().toDoubleOrNull() ?: 1.0
              if (title.trim().isEmpty()) {
                validationError = "Please enter the event shoot title"
                return@Button
              }
              if (parsedEarnings == null || parsedEarnings < 0.0) {
                validationError = "Please enter a valid total earnings amount"
                return@Button
              }
              val chosenStudio = if (isCreatingNewStudio || studios.isEmpty()) {
                null
              } else {
                studios.find { it.id == selectedStudioId }
              }
              if (chosenStudio == null && newStudioName.trim().isEmpty()) {
                validationError = "Please select or enter a studio name"
                return@Button
              }
              onSave(
                title,
                chosenStudio,
                newStudioName,
                newStudioContact,
                category,
                shootDateMillis,
                location,
                parsedDuration,
                durationUnit,
                parsedEarnings,
                notes,
              )
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = NetflixRed,
              contentColor = NetflixWhite,
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .weight(1.4f)
              .height(50.dp)
              .testTag("save_shoot_button"),
          ) {
            Text(
              text = if (existingShoot == null) "Save Shoot" else "Update Shoot",
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WithdrawalEditorDialog(
  existingWithdrawal: WithdrawalRecord?,
  studios: List<Studio>,
  studioSummaries: List<StudioBalanceSummary>,
  preselectedStudioId: String?,
  currencySymbol: String,
  onDismiss: () -> Unit,
  onSave: (
    selectedStudio: Studio?,
    newStudioName: String,
    amount: Double,
    withdrawalDateMillis: Long,
    paymentMethod: String,
    notes: String,
  ) -> Unit,
) {
  var selectedStudioId by remember {
    mutableStateOf(
      existingWithdrawal?.studioId
        ?: preselectedStudioId
        ?: studios.firstOrNull()?.id
    )
  }
  var isCreatingNewStudio by remember { mutableStateOf(studios.isEmpty()) }
  var newStudioName by remember { mutableStateOf("") }
  var amountText by remember {
    mutableStateOf(
      existingWithdrawal?.amount?.let {
        if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
      } ?: ""
    )
  }
  var withdrawalDateMillis by remember {
    mutableLongStateOf(
      existingWithdrawal?.withdrawalDateMillis?.takeIf { it > 0L } ?: System.currentTimeMillis()
    )
  }
  var paymentMethod by remember {
    mutableStateOf(existingWithdrawal?.paymentMethod ?: CineConstants.PAYMENT_METHODS.first())
  }
  var notes by remember { mutableStateOf(existingWithdrawal?.notes ?: "") }
  var validationError by remember { mutableStateOf<String?>(null) }
  var showDatePicker by remember { mutableStateOf(false) }

  val selectedSummary = studioSummaries.find { it.studio.id == selectedStudioId }

  if (showDatePicker) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = withdrawalDateMillis)
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let { withdrawalDateMillis = it }
            showDatePicker = false
          }
        ) {
          Text("Confirm", color = NetflixRed, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) {
          Text("Cancel", color = NetflixSilver)
        }
      },
    ) {
      DatePicker(state = datePickerState)
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      color = NetflixSurface,
      shape = RoundedCornerShape(18.dp),
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .border(1.dp, NetflixBorder, RoundedCornerShape(18.dp)),
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState()),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = if (existingWithdrawal == null) "RECORD STUDIO WITHDRAWAL" else "EDIT WITHDRAWAL",
              style = MaterialTheme.typography.headlineLarge,
              color = NetflixWhite,
            )
            Text(
              text = "Log payout received from studio & update available balance",
              style = MaterialTheme.typography.bodySmall,
              color = NetflixSilver,
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Filled.Close, contentDescription = "Close", tint = NetflixSilver)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (validationError != null) {
          Text(
            text = validationError ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = NetflixRed,
            modifier = Modifier.padding(bottom = 8.dp),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "SELECT STUDIO *",
            style = MaterialTheme.typography.labelMedium,
            color = NetflixSilver,
          )
          if (studios.isNotEmpty()) {
            TextButton(onClick = { isCreatingNewStudio = !isCreatingNewStudio }) {
              Text(
                text = if (isCreatingNewStudio) "Choose Existing" else "+ New Studio",
                color = NetflixRed,
                style = MaterialTheme.typography.labelMedium,
              )
            }
          }
        }

        if (isCreatingNewStudio || studios.isEmpty()) {
          OutlinedTextField(
            value = newStudioName,
            onValueChange = { newStudioName = it; validationError = null },
            label = { Text("Studio Name *") },
            placeholder = { Text("e.g., Redline Cinema Studio") },
            singleLine = true,
            colors = netflixTextFieldColors(),
            modifier = Modifier.fillMaxWidth(),
          )
        } else {
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            studios.forEach { studio ->
              val summary = studioSummaries.find { it.studio.id == studio.id }
              val balStr = summary?.let { CurrencyFormatter.format(it.availableBalance, currencySymbol) } ?: ""
              FilterChip(
                selected = studio.id == selectedStudioId,
                onClick = { selectedStudioId = studio.id },
                label = {
                  Text(if (balStr.isNotEmpty()) "${studio.name} ($balStr)" else studio.name)
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = NetflixRed,
                  selectedLabelColor = NetflixWhite,
                  containerColor = NetflixCard,
                  labelColor = NetflixSilver,
                ),
              )
            }
          }

          if (selectedSummary != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(NetflixCardElevated)
                .padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column {
                Text(
                  text = "${selectedSummary.studio.name} Collectible Balance",
                  style = MaterialTheme.typography.labelSmall,
                  color = NetflixSilver,
                )
                Text(
                  text = CurrencyFormatter.format(selectedSummary.availableBalance, currencySymbol),
                  style = MaterialTheme.typography.titleMedium,
                  color = NetflixMatchGreen,
                  fontWeight = FontWeight.Bold,
                )
              }
              if (selectedSummary.availableBalance > 0) {
                TextButton(
                  onClick = {
                    val bal = selectedSummary.availableBalance
                    amountText = if (bal % 1.0 == 0.0) bal.toInt().toString() else bal.toString()
                  }
                ) {
                  Text(
                    text = "Withdraw Full",
                    color = NetflixGold,
                    style = MaterialTheme.typography.labelMedium,
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it; validationError = null },
            label = { Text("Withdrawal ($currencySymbol) *") },
            placeholder = { Text("e.g., 10000") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = netflixTextFieldColors(),
            modifier = Modifier
              .weight(1f)
              .testTag("withdrawal_amount_input"),
          )

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "WITHDRAWAL DATE",
              style = MaterialTheme.typography.labelSmall,
              color = NetflixSilver,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NetflixCard)
                .border(1.dp, NetflixBorder, RoundedCornerShape(8.dp))
                .clickable { showDatePicker = true }
                .padding(horizontal = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = "Pick Withdrawal Date",
                tint = NetflixAmber,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = CurrencyFormatter.formatDate(withdrawalDateMillis),
                style = MaterialTheme.typography.bodyMedium,
                color = NetflixWhite,
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "PAYMENT METHOD",
          style = MaterialTheme.typography.labelMedium,
          color = NetflixSilver,
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          CineConstants.PAYMENT_METHODS.forEach { method ->
            FilterChip(
              selected = paymentMethod == method,
              onClick = { paymentMethod = method },
              label = { Text(method) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NetflixAmber,
                selectedLabelColor = Color.Black,
                containerColor = NetflixCard,
                labelColor = NetflixSilver,
              ),
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Transaction Reference / Notes (Optional)") },
          placeholder = { Text("e.g., UPI Ref #42981 or Advance payout") },
          maxLines = 2,
          colors = netflixTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("withdrawal_notes_input"),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .height(50.dp),
            shape = RoundedCornerShape(8.dp),
          ) {
            Text("Cancel", color = NetflixSilver)
          }

          Button(
            onClick = {
              val parsedAmount = amountText.trim().toDoubleOrNull()
              if (parsedAmount == null || parsedAmount <= 0.0) {
                validationError = "Please enter a withdrawal amount greater than 0"
                return@Button
              }
              val chosenStudio = if (isCreatingNewStudio || studios.isEmpty()) {
                null
              } else {
                studios.find { it.id == selectedStudioId }
              }
              if (chosenStudio == null && newStudioName.trim().isEmpty()) {
                validationError = "Please select or enter a studio"
                return@Button
              }
              onSave(
                chosenStudio,
                newStudioName,
                parsedAmount,
                withdrawalDateMillis,
                paymentMethod,
                notes,
              )
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = NetflixRed,
              contentColor = NetflixWhite,
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .weight(1.4f)
              .height(50.dp)
              .testTag("save_withdrawal_button"),
          ) {
            Text(
              text = if (existingWithdrawal == null) "Record Withdrawal" else "Update Withdrawal",
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }
    }
  }
}

@Composable
fun StudioEditorDialog(
  existingStudio: Studio?,
  onDismiss: () -> Unit,
  onSave: (name: String, contactPerson: String) -> Unit,
) {
  var name by remember { mutableStateOf(existingStudio?.name ?: "") }
  var contactPerson by remember { mutableStateOf(existingStudio?.contactPerson ?: "") }
  var errorText by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = NetflixSurface,
    titleContentColor = NetflixWhite,
    textContentColor = NetflixSilver,
    title = {
      Text(
        text = if (existingStudio == null) "ADD PRODUCTION STUDIO" else "EDIT STUDIO",
        style = MaterialTheme.typography.headlineLarge,
      )
    },
    text = {
      Column {
        if (errorText != null) {
          Text(
            text = errorText ?: "",
            color = NetflixRed,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp),
          )
        }
        OutlinedTextField(
          value = name,
          onValueChange = { name = it; errorText = null },
          label = { Text("Studio / Production House Name *") },
          placeholder = { Text("e.g., Dharma Frames Studio") },
          singleLine = true,
          colors = netflixTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("studio_name_input"),
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
          value = contactPerson,
          onValueChange = { contactPerson = it },
          label = { Text("Producer / Contact Person (Optional)") },
          placeholder = { Text("e.g., Rohan Sharma • +91 9876543210") },
          singleLine = true,
          colors = netflixTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("studio_contact_input"),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.trim().isEmpty()) {
            errorText = "Studio name is required"
            return@Button
          }
          onSave(name.trim(), contactPerson.trim())
        },
        colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
        modifier = Modifier.testTag("save_studio_button"),
      ) {
        Text(if (existingStudio == null) "Save Studio" else "Update", color = NetflixWhite)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = NetflixSilver)
      }
    },
  )
}

@Composable
fun ConfirmDeleteDialog(
  title: String,
  message: String,
  onConfirmDelete: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = NetflixSurface,
    icon = {
      Icon(
        imageVector = Icons.Filled.DeleteForever,
        contentDescription = "Delete",
        tint = NetflixRed,
        modifier = Modifier.size(32.dp),
      )
    },
    title = {
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = NetflixWhite,
      )
    },
    text = {
      Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = NetflixSilver,
      )
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirmDelete()
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
        modifier = Modifier.testTag("confirm_delete_button"),
      ) {
        Text("Delete Permanently", color = NetflixWhite, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Keep Record", color = NetflixSilver)
      }
    },
  )
}

@Composable
fun netflixTextFieldColors() = OutlinedTextFieldDefaults.colors(
  focusedTextColor = NetflixWhite,
  unfocusedTextColor = NetflixWhite,
  focusedContainerColor = NetflixCard,
  unfocusedContainerColor = NetflixCard,
  focusedBorderColor = NetflixRed,
  unfocusedBorderColor = NetflixBorder,
  focusedLabelColor = NetflixRed,
  unfocusedLabelColor = NetflixSilver,
  cursorColor = NetflixRed,
)
