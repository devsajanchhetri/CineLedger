# Security Specification — CineLedger

## 1. Data Invariants
1. **Strict Multi-Tenant Isolation**: Every document in `/users/{userId}/studios/{studioId}`, `/users/{userId}/shoots/{shootId}`, and `/users/{userId}/withdrawals/{withdrawalId}` belongs exclusively to `userId` and may only be read, listed, created, updated, or deleted when `request.auth != null && request.auth.uid == userId`.
2. **Identity Integrity**: On creation and update, `request.resource.data.userId` must equal `request.auth.uid` and `userId`. On update, `userId` and `createdAt` are strictly immutable (`incoming().userId == existing().userId && incoming().createdAt == existing().createdAt`).
3. **Temporal Integrity**: `createdAt` and `updatedAt` must be server timestamps (`is timestamp && <= request.time`).
4. **Schema & Boundary Integrity**:
   - All document IDs (`userId`, `studioId`, `shootId`, `withdrawalId`) must satisfy `isValidId()` (`^[a-zA-Z0-9_\-]+$`, `size() <= 128`).
   - All strings have explicit `.size()` bounds.
   - Numeric earnings and withdrawal amounts are bounded (`>= 0` or `> 0`, `<= 1000000000`).
   - Enumerated strings (`category`, `durationUnit`, `paymentMethod`) must match exact allowlisted values.

## 2. The "Dirty Dozen" Payloads
1. **Unauthenticated Read**: `auth = null` attempting `get` or `list` on `/users/alice_123/shoots` -> REJECT.
2. **Cross-User Read**: `auth.uid = 'bob_456'` attempting `get` or `list` on `/users/alice_123/shoots` -> REJECT.
3. **Identity Spoofing on Create**: `auth.uid = 'alice_123'` creating `/users/alice_123/studios/st_1` with `userId: 'bob_456'` -> REJECT.
4. **Ghost Field Injection (Shadow Create/Update)**: Adding `isAdmin: true` or `verified: true` to a studio, shoot, or withdrawal document -> REJECT via `.keys().hasOnly(...)` and `affectedKeys().hasOnly(...)`.
5. **Immutable Field Mutation**: Updating `createdAt` or `userId` on an existing shoot -> REJECT.
6. **Negative Shoot Earnings**: Creating a shoot with `earnings: -500` -> REJECT.
7. **Zero or Negative Withdrawal Amount**: Creating a withdrawal with `amount: 0` or `amount: -100` -> REJECT.
8. **Invalid Category Enum**: Creating a shoot with `category: 'HackedCategory'` -> REJECT.
9. **Invalid Payment Method Enum**: Creating a withdrawal with `paymentMethod: 'Crypto'` -> REJECT.
10. **Oversized String Resource Poisoning**: Creating a shoot with a 10,000-character `title` or `notes` -> REJECT.
11. **Future Timestamp Spoofing**: Submitting a `createdAt` timestamp in the future (`> request.time`) -> REJECT.
12. **Malformed Document ID**: Creating a document with invalid characters or >128 chars in `{studioId}` -> REJECT.
