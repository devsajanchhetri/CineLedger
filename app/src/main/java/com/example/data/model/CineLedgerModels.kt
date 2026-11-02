package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Studio(
  @DocumentId val id: String = "",
  val userId: String = "",
  val name: String = "",
  val contactPerson: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class ShootRecord(
  @DocumentId val id: String = "",
  val userId: String = "",
  val title: String = "",
  val studioId: String = "",
  val studioName: String = "",
  val category: String = "Wedding",
  val shootDateMillis: Long = 0L,
  val location: String = "",
  val durationValue: Double = 1.0,
  val durationUnit: String = "Days",
  val earnings: Double = 0.0,
  val notes: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class WithdrawalRecord(
  @DocumentId val id: String = "",
  val userId: String = "",
  val studioId: String = "",
  val studioName: String = "",
  val amount: Double = 0.0,
  val withdrawalDateMillis: Long = 0L,
  val paymentMethod: String = "Bank Transfer",
  val notes: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

data class StudioBalanceSummary(
  val studio: Studio,
  val totalEarned: Double,
  val totalWithdrawn: Double,
  val availableBalance: Double,
  val shootCount: Int,
  val withdrawalCount: Int,
  val lastActivityMillis: Long,
)

enum class TransactionKind {
  SHOOT_EARNING,
  STUDIO_WITHDRAWAL,
}

data class LedgerTransactionItem(
  val id: String,
  val kind: TransactionKind,
  val title: String,
  val subtitle: String,
  val studioId: String,
  val studioName: String,
  val amount: Double,
  val dateMillis: Long,
  val badgeText: String,
  val notes: String,
  val shootRecord: ShootRecord? = null,
  val withdrawalRecord: WithdrawalRecord? = null,
)

object CineConstants {
  val SHOOT_CATEGORIES = listOf(
    "Wedding",
    "Commercial",
    "Music Video",
    "Corporate",
    "Pre-Wedding",
    "Documentary",
    "Other",
  )

  val DURATION_UNITS = listOf("Days", "Hours")

  val PAYMENT_METHODS = listOf(
    "Bank Transfer",
    "UPI",
    "Cash",
    "Check",
    "Wire",
  )

  val CURRENCIES = listOf("रु", "₹", "$", "€", "£", "AED")
}
