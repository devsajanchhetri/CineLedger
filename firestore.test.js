const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or write studios, shoots, or withdrawals", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("studios").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("shoots").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("withdrawals").get());
});

test("Authenticated user: can create and query their own studio, shoot, and withdrawal", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("studios").doc("studio_1").set({
      userId: ALICE_UID,
      name: "Redline Cinema Studios",
      contactPerson: "Vikram Mehta",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("shoots").doc("shoot_1").set({
      userId: ALICE_UID,
      title: "Royal Palace Wedding",
      studioId: "studio_1",
      studioName: "Redline Cinema Studios",
      category: "Wedding",
      shootDateMillis: 1730500000000,
      location: "Udaipur",
      durationValue: 3,
      durationUnit: "Days",
      earnings: 45000,
      notes: "2x FX3 + Gimbal",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("withdrawals").doc("wd_1").set({
      userId: ALICE_UID,
      studioId: "studio_1",
      studioName: "Redline Cinema Studios",
      amount: 15000,
      withdrawalDateMillis: 1730600000000,
      paymentMethod: "UPI",
      notes: "Advance settlement",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("studios").where("userId", "==", ALICE_UID).get()
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("shoots").where("userId", "==", ALICE_UID).get()
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("withdrawals").where("userId", "==", ALICE_UID).get()
  );
});

test("Cross-user isolation: Bob cannot read, update, or delete Alice's records", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("shoots").doc("shoot_1").set({
      userId: ALICE_UID,
      title: "Private Commercial Shoot",
      studioId: "studio_1",
      studioName: "Apex Films",
      category: "Commercial",
      shootDateMillis: 1730500000000,
      location: "Mumbai",
      durationValue: 1,
      durationUnit: "Days",
      earnings: 25000,
      notes: "",
      createdAt: now,
      updatedAt: now,
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("shoots").doc("shoot_1").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("shoots").doc("shoot_1").delete()
  );
});

test("Schema validation: rejects shadow fields, negative earnings, and invalid categories", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();

  // Ghost field injection
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("studios").doc("studio_bad").set({
      userId: ALICE_UID,
      name: "Shadow Studio",
      isVerified: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Negative earnings
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("shoots").doc("shoot_bad").set({
      userId: ALICE_UID,
      title: "Bad Shoot",
      studioId: "studio_1",
      studioName: "Studio",
      category: "Wedding",
      shootDateMillis: 1730500000000,
      durationValue: 1,
      durationUnit: "Days",
      earnings: -500,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Zero withdrawal amount
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("withdrawals").doc("wd_bad").set({
      userId: ALICE_UID,
      studioId: "studio_1",
      studioName: "Studio",
      amount: 0,
      withdrawalDateMillis: 1730600000000,
      paymentMethod: "Cash",
      createdAt: now,
      updatedAt: now,
    })
  );
});
