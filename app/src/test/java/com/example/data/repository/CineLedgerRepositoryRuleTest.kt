package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class CineLedgerRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun createStudioShootAndWithdrawal_validPayload_succeedsAndEmitsRealtimeUpdates() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val repository = CineLedgerRepository(firestore)

    val studioResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createStudio(name = "Redline Studios", contactPerson = "Vikram")
    }
    assertTrue("Expected studio creation to succeed: ${studioResult.exceptionOrNull()}", studioResult.isSuccess)
    val studioId = studioResult.getOrThrow()
    assertTrue(studioId.isNotEmpty())

    val shootResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createShoot(
        title = "Grand Palace Wedding",
        studioId = studioId,
        studioName = "Redline Studios",
        category = "Wedding",
        shootDateMillis = 1730500000000L,
        location = "Jaipur",
        durationValue = 2.0,
        durationUnit = "Days",
        earnings = 35000.0,
        notes = "Sony FX3 Anamorphic",
      )
    }
    assertTrue("Expected shoot creation to succeed: ${shootResult.exceptionOrNull()}", shootResult.isSuccess)
    val shootId = shootResult.getOrThrow()

    val withdrawalResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createWithdrawal(
        studioId = studioId,
        studioName = "Redline Studios",
        amount = 10000.0,
        withdrawalDateMillis = 1730600000000L,
        paymentMethod = "UPI",
        notes = "First installment",
      )
    }
    assertTrue("Expected withdrawal creation to succeed: ${withdrawalResult.exceptionOrNull()}", withdrawalResult.isSuccess)
    val withdrawalId = withdrawalResult.getOrThrow()

    val emittedShoots = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeShoots(aliceUid).first { list -> list.any { it.id == shootId } }
    }
    assertTrue(emittedShoots.any { it.id == shootId && it.earnings == 35000.0 })

    val emittedWithdrawals = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeWithdrawals(aliceUid).first { list -> list.any { it.id == withdrawalId } }
    }
    assertTrue(emittedWithdrawals.any { it.id == withdrawalId && it.amount == 10000.0 })
  }

  @Test
  fun getShootById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = CineLedgerRepository(firestore)

    val studioId = withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.createStudio(name = "Alice Studio").getOrThrow()
    }
    val shootId = withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.createShoot(
        title = "Alice Exclusive Shoot",
        studioId = studioId,
        studioName = "Alice Studio",
        category = "Commercial",
        shootDateMillis = 1730500000000L,
        location = "Delhi",
        durationValue = 1.0,
        durationUnit = "Days",
        earnings = 20000.0,
        notes = "",
      ).getOrThrow()
    }

    signInTestUser(BOB_EMAIL)
    val bobRepo = CineLedgerRepository(firestore)
    val crossReadResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      bobRepo.getShootById(aliceUid, shootId)
    }
    assertTrue("Expected cross-user read to fail", crossReadResult.isFailure)
    val firestoreException = crossReadResult.exceptionOrNull()?.unwrapFirestoreException()
    assertNotNull("Expected FirebaseFirestoreException", firestoreException)
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
  }

  @Test
  fun observeShoots_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    auth.signOut()
    val repository = CineLedgerRepository(firestore)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repository.observeShoots(aliceUid).first()
      }
      fail("Expected unauthenticated observeShoots to fail with PERMISSION_DENIED")
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      assertNotNull("Expected nested FirebaseFirestoreException, got $e", firestoreException)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice@cineledger.test"
    const val BOB_EMAIL = "bob@cineledger.test"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 3000L
  }
}
