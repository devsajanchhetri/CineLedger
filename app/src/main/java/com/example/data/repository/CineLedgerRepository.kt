package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ShootRecord
import com.example.data.model.Studio
import com.example.data.model.WithdrawalRecord
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CineLedgerRepository(private val db: FirebaseFirestore) {

  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private val auth
    get() = Firebase.auth

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  // --- STUDIOS ---

  fun observeStudios(userId: String = requireUserId()): Flow<List<Studio>> = flow {
    val path = "users/$userId/studios"
    emitAll(
      db.collection("users").document(userId).collection("studios")
        .whereEqualTo("userId", userId)
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(Studio::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        }
        .catch { error ->
          if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
          throw error
        }
    )
  }

  suspend fun createStudio(name: String, contactPerson: String = ""): Result<String> {
    val uid = requireUserId()
    val collectionRef = db.collection("users").document(uid).collection("studios")
    val docRef = collectionRef.document()
    val payload = mapOf(
      "userId" to uid,
      "name" to name.trim(),
      "contactPerson" to contactPerson.trim(),
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
    return try {
      docRef.set(payload).await()
      Result.success(docRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun updateStudio(studio: Studio): Result<Unit> {
    val uid = requireUserId()
    require(studio.id.isNotBlank()) { "Studio ID must not be blank" }
    requireNotNull(studio.createdAt) { "Studio createdAt must not be null" }
    val docRef = db.collection("users").document(uid).collection("studios").document(studio.id)
    return try {
      docRef.set(studio.copy(userId = uid, updatedAt = null)).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun deleteStudio(studioId: String): Result<Unit> {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("studios").document(studioId)
    return try {
      docRef.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      Result.failure(e)
    }
  }

  // --- SHOOTS ---

  fun observeShoots(userId: String = requireUserId()): Flow<List<ShootRecord>> = flow {
    val path = "users/$userId/shoots"
    emitAll(
      db.collection("users").document(userId).collection("shoots")
        .whereEqualTo("userId", userId)
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(ShootRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        }
        .catch { error ->
          if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
          throw error
        }
    )
  }

  suspend fun createShoot(
    title: String,
    studioId: String,
    studioName: String,
    category: String,
    shootDateMillis: Long,
    location: String,
    durationValue: Double,
    durationUnit: String,
    earnings: Double,
    notes: String,
  ): Result<String> {
    val uid = requireUserId()
    val collectionRef = db.collection("users").document(uid).collection("shoots")
    val docRef = collectionRef.document()
    val payload = mapOf(
      "userId" to uid,
      "title" to title.trim(),
      "studioId" to studioId,
      "studioName" to studioName.trim(),
      "category" to category,
      "shootDateMillis" to shootDateMillis,
      "location" to location.trim(),
      "durationValue" to durationValue,
      "durationUnit" to durationUnit,
      "earnings" to earnings,
      "notes" to notes.trim(),
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
    return try {
      docRef.set(payload).await()
      Result.success(docRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun updateShoot(shoot: ShootRecord): Result<Unit> {
    val uid = requireUserId()
    require(shoot.id.isNotBlank()) { "Shoot ID must not be blank" }
    requireNotNull(shoot.createdAt) { "Shoot createdAt must not be null" }
    val docRef = db.collection("users").document(uid).collection("shoots").document(shoot.id)
    return try {
      docRef.set(shoot.copy(userId = uid, updatedAt = null)).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun deleteShoot(shootId: String): Result<Unit> {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("shoots").document(shootId)
    return try {
      docRef.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun getShootById(ownerUserId: String, shootId: String): Result<ShootRecord?> {
    val docRef = db.collection("users").document(ownerUserId).collection("shoots").document(shootId)
    return try {
      val snapshot = docRef.get().await()
      Result.success(
        snapshot.toObject(ShootRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, docRef.path)
      Result.failure(e)
    }
  }

  // --- WITHDRAWALS ---

  fun observeWithdrawals(userId: String = requireUserId()): Flow<List<WithdrawalRecord>> = flow {
    val path = "users/$userId/withdrawals"
    emitAll(
      db.collection("users").document(userId).collection("withdrawals")
        .whereEqualTo("userId", userId)
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(WithdrawalRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        }
        .catch { error ->
          if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
          throw error
        }
    )
  }

  suspend fun createWithdrawal(
    studioId: String,
    studioName: String,
    amount: Double,
    withdrawalDateMillis: Long,
    paymentMethod: String,
    notes: String,
  ): Result<String> {
    val uid = requireUserId()
    val collectionRef = db.collection("users").document(uid).collection("withdrawals")
    val docRef = collectionRef.document()
    val payload = mapOf(
      "userId" to uid,
      "studioId" to studioId,
      "studioName" to studioName.trim(),
      "amount" to amount,
      "withdrawalDateMillis" to withdrawalDateMillis,
      "paymentMethod" to paymentMethod,
      "notes" to notes.trim(),
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
    return try {
      docRef.set(payload).await()
      Result.success(docRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun updateWithdrawal(withdrawal: WithdrawalRecord): Result<Unit> {
    val uid = requireUserId()
    require(withdrawal.id.isNotBlank()) { "Withdrawal ID must not be blank" }
    requireNotNull(withdrawal.createdAt) { "Withdrawal createdAt must not be null" }
    val docRef = db.collection("users").document(uid).collection("withdrawals").document(withdrawal.id)
    return try {
      docRef.set(withdrawal.copy(userId = uid, updatedAt = null)).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun deleteWithdrawal(withdrawalId: String): Result<Unit> {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("withdrawals").document(withdrawalId)
    return try {
      docRef.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      Result.failure(e)
    }
  }
}
