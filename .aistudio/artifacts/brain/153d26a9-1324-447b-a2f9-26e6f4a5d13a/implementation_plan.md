# CineLedger — Cinematographer Shoot & Studio Earnings Ledger

CineLedger is a professional financial and project ledger designed for event and freelance cinematographers. Inspired by the sleek, high-contrast visual language of the Netflix app, it tracks every event shoot, manages multi-studio accounts, logs studio withdrawals, calculates real-time available balances per studio and overall, and exports studio reconciliation statements—all backed by lifetime cloud persistence via Google Sign-In and Firebase Firestore.

## User Review & Critical Decisions

> [!IMPORTANT]
> Review the confirmed product decisions below before we provision Firebase and build the application.

- **Confirmed Decision — Direct Final Payout Logging**: Every event shoot is logged directly with its final confirmed earnings amount, immediately updating both the total earnings and the associated studio's available balance.
- **Confirmed Decision — Multi-Studio Ledger & Global Rollup**: You can create and manage multiple production studios/clients. Each studio maintains its own dedicated ledger (Total Earned, Total Withdrawn, Available Balance to Collect), while the home dashboard aggregates your global lifetime earnings and net collectible balance across all studios.
- **Confirmed Decision — Rich Shoot Metadata & Reconciliation Exports**:
  - **Shoot Details**: Event Title, Studio, Shoot Date, Category/Type (Wedding, Commercial, Music Video, Corporate, Pre-Wedding, Documentary), Location, Duration (Days or Hours), Final Earnings, and Notes.
  - **Studio Withdrawals**: Studio, Withdrawal Amount, Date, Payment Method (Bank Transfer, UPI, Cash, Check), and Reference Notes.
  - **Statement Export**: One-tap generation and sharing of professional PDF and CSV studio statements (filterable by studio or all studios) for effortless payment reconciliation.
- **Confirmed Decision — Strict Zero-State & Lifetime Cloud Persistence**: The app launches with a clean `$0` / `0` state with zero mock or pre-filled records. Every shoot, studio, and withdrawal you create is stored under your authenticated Google account in Firebase Firestore until you explicitly delete it.

---

## 1. Overview & Core Concept

- **What It Does**: Gives cinematographers a centralized command center to log completed event shoots, assign them to specific studios, record partial or full cash/bank withdrawals received from each studio, and always know the exact remaining balance owed by every studio.
- **Target Audience / Persona**: Freelance and contract cinematographers, directors of photography (DPs), and event videographers who shoot for one or more studios and receive payouts or withdrawals across irregular schedules.
- **Key Value**: Eliminates spreadsheet guesswork and payment disputes by maintaining an immutable, cloud-synced ledger of every shoot earned versus every withdrawal collected, presented in a cinematic, fast-scanning interface.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Cinematic Google Sign-In Gate**:
   - Full-bleed dark theatrical welcome screen with a custom cinematic camera/reel backdrop, app branding, and a prominent **"Sign in with Google"** button powered by Jetpack Credential Manager.
2. **Zero-State First Launch**:
   - Upon first login, all dashboard metrics read `0.00` (Total Earned: `0`, Withdrawn: `0`, Available Balance: `0`), accompanied by a sleek Netflix-style empty state card inviting you to add your first Studio or log your first Shoot.
3. **Netflix-Style Home Dashboard ("Billboard & Rails")**:
   - **Featured Billboard Header**: Displays the primary financial spotlight—**Available Balance to Collect** in oversized display typography, flanked by **Total Lifetime Shoot Earnings** and **Total Studio Withdrawals**, with quick-action CTA buttons (`+ Log Shoot` and `+ Withdrawal`).
   - **Category Filter Pills**: Quick horizontal filter chips (`All`, `Wedding`, `Commercial`, `Music Video`, `Corporate`, `Pre-Wedding`, `Documentary`) just like Netflix genre pills.
   - **Horizontal Studio Spotlight Rail**: Swipeable poster-style studio cards showing each studio's name, shoot count, total earned, total withdrawn, and net receivable balance badge.
   - **Recent Shoots & Productions Feed**: Rich dark cards showing event title, studio tag, category badge, location, shoot date, duration (days/hours), earnings amount, and quick edit/delete actions.
4. **Studios & Balances Hub**:
   - Dedicated view listing all studios with individual progress bars showing the ratio of withdrawn payouts vs. total shoot earnings, plus a drill-down into a single studio's complete history of shoots and withdrawals.
5. **Studio Transactions & Withdrawal Ledger**:
   - Chronological ledger of all studio transactions (both shoot credits and withdrawal debits) with payment method badges (`UPI`, `Bank Transfer`, `Cash`, `Check`), notes, and one-tap delete confirmation.
6. **PDF & CSV Statement Exporter**:
   - Generate a formatted PDF or CSV reconciliation statement for any individual studio (or all studios combined) and share it directly via Android's native Share Sheet (WhatsApp, Email, Drive).

### Visual Identity & Theme (Netflix UI/UX Reference)

- **Aesthetic Direction**: **Cinematic Obsidian & Crimson** — Deep theater-black surfaces, high-contrast white/silver typography, edge-to-edge hero gradients, and unmistakable Netflix-red interactive highlights.
- **Color Palette & Mood**:
  - **Background Primary**: `#0B0B0B` (Deep Theater Black)
  - **Surface / Card Container**: `#181818` to `#222222` (Charcoal Slate with subtle `1.dp` `#2E2E2E` hairline borders)
  - **Primary Accent**: `#E50914` (Signature Cinema Red for primary CTAs, active indicators, and key badges)
  - **Secondary / Financial Semantic Accents**:
    - `#46D369` (Netflix Match Green for Available Balance & Shoot Earnings credits)
    - `#F5C518` (Warm Gold for Studio badges & duration highlights)
    - `#E87C03` (Amber for Withdrawal debits)
  - **Text Hierarchy**: `#FFFFFF` (Primary Headlines & Figures), `#B3B3B3` (Secondary Metadata), `#737373` (Muted Labels)
- **Typography & Hierarchy**:
  - **Display & Numeric Headlines**: **Bebas Neue** / **Oswald** (bundled locally via `res/font/`) for bold, condensed cinematic titles and large currency figures.
  - **Body & UI Controls**: **Montserrat** (Regular, Medium, SemiBold, Bold) for crisp readability across ledger rows, dates, and tags.
- **Interactive Feedback & Motion**:
  - Smooth spring transitions between tabs, animated counter updates when logging shoots or withdrawals, pill selection animations, and confirmation dialogs before permanently deleting lifetime records.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Unified Ledger Derived from Source-of-Truth Collections**
  - *Chosen Approach*: Store `Studios`, `Shoots`, and `Withdrawals` as distinct, strongly-typed Firestore collections under `/users/{userId}/...` and compute studio-level and global balances reactively in real time.
  - *Why*: Guarantees that adding or deleting a shoot or withdrawal immediately and accurately updates the available balance without risk of out-of-sync counter fields.
  - *Alternatives Considered*: Maintaining manual balance counters on studio documents, which can drift if an item is edited or deleted.
- **Decision 2: Native On-Device PDF & CSV Statement Generation**
  - *Chosen Approach*: Use Android's native `PdfDocument` API and CSV writer with `FileProvider` to generate shareable studio reconciliation statements directly on the device.
  - *Why*: Works instantaneously offline or online without external third-party server dependencies and formats cleanly for sending to studio managers.
- **Decision 3: Multi-Currency Symbol Flexibility**
  - *Chosen Approach*: Allow quick switching of the display currency symbol (e.g., `₹`, `$`, `€`, `£`, `AED`) in the header/profile sheet while storing exact numeric amounts.
  - *Why*: Cinematographers work globally; customizable currency formatting makes financial figures immediately natural for any user.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                         │
│                                                                         │
│  ┌──────────────────────┐        ┌───────────────────────────────────┐  │
│  │   AuthGateScreen     │───────▶│         MainCinemaScaffold        │  │
│  │ (Google Credential   │        │  (Bottom Nav / Adaptive Rail)     │  │
│  │  Manager Sign-In)    │        └─────────────────┬─────────────────┘  │
│  └──────────────────────┘                          │                    │
│                                                    ▼                    │
│        ┌─────────────────────────┬─────────────────┴───────────┐        │
│        ▼                         ▼                             ▼        │
│  ┌───────────┐           ┌───────────────┐             ┌─────────────┐  │
│  │ Dashboard │           │ Studios &     │             │ Withdrawal  │  │
│  │ & Shoots  │           │ Studio Detail │             │ Ledger &    │  │
│  │   Screen  │           │    Screen     │             │ PDF Export  │  │
│  └─────┬─────┘           └───────┬───────┘             └──────┬──────┘  │
└────────┼─────────────────────────┼────────────────────────────┼─────────┘
         │                         │                            │
         └─────────────────────────┼────────────────────────────┘
                                   ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                   CineLedgerViewModel (StateFlow / MVVM)                │
│  • Combines Studios, Shoots, and Withdrawals streams                    │
│  • Computes Global & Per-Studio (Earned - Withdrawn = Available)        │
│  • Filters by Category, Studio, Search Query, and Date Range            │
└──────────────────────────────────┬──────────────────────────────────────┘
                                   ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        CineLedgerRepository                             │
│  • Scoped to FirebaseFirestore(R.string.firestore_database_id)          │
│  • Enforces requireUserId() & structured handleFirestoreError()         │
└──────────────┬───────────────────┼───────────────────┬──────────────────┘
               ▼                   ▼                   ▼
     ┌──────────────────┐ ┌─────────────────┐ ┌────────────────────┐
     │ /users/{uid}/    │ │ /users/{uid}/   │ │ /users/{uid}/      │
     │     studios      │ │     shoots      │ │    withdrawals     │
     └──────────────────┘ └─────────────────┘ └────────────────────┘
```

### Data Model & State

All documents are strictly isolated per authenticated user under `/users/{userId}/`:

1. **Studio (`/users/{userId}/studios/{studioId}`)**:
   - `id`: Document ID
   - `userId`: Owner's Firebase Auth UID
   - `name`: Studio / Production House Name (e.g., "Redline Studios")
   - `contactPerson`: Optional producer/manager name or phone
   - `createdAt`, `updatedAt`: Server timestamps
2. **ShootRecord (`/users/{userId}/shoots/{shootId}`)**:
   - `id`: Document ID
   - `userId`: Owner's Firebase Auth UID
   - `title`: Event Title (e.g., "Aarav & Meera Royal Wedding")
   - `studioId`: Associated Studio ID
   - `studioName`: Denormalized Studio Name for fast display
   - `category`: Event Category (`Wedding`, `Commercial`, `Music Video`, `Corporate`, `Pre-Wedding`, `Documentary`, `Other`)
   - `shootDateMillis`: Event date in epoch milliseconds
   - `location`: Shoot venue / city
   - `durationValue`: Number of days or hours (Double)
   - `durationUnit`: `"Days"` or `"Hours"`
   - `earnings`: Confirmed total earnings for this project (Double)
   - `notes`: Gear or project notes
   - `createdAt`, `updatedAt`: Server timestamps
3. **WithdrawalRecord (`/users/{userId}/withdrawals/{withdrawalId}`)**:
   - `id`: Document ID
   - `userId`: Owner's Firebase Auth UID
   - `studioId`: Associated Studio ID
   - `studioName`: Denormalized Studio Name
   - `amount`: Amount withdrawn/received from studio (Double)
   - `withdrawalDateMillis`: Date of withdrawal in epoch milliseconds
   - `paymentMethod`: `"Bank Transfer"`, `"UPI"`, `"Cash"`, `"Check"`, `"Wire"`
   - `notes`: Reference ID or settlement note
   - `createdAt`, `updatedAt`: Server timestamps

### Interactive Component & State Mapping

- **Add / Edit Shoot Modal Sheet**: Validates non-empty title, selected studio (with inline quick-create studio option if no studio exists yet), positive earnings amount, date picker, category selector chips, location, and duration (days/hours).
- **Record Studio Withdrawal Modal Sheet**: Selects target studio, displays that studio's current available balance for reference, validates withdrawal amount, selects date and payment method, and saves to Firestore.
- **Lifetime Persistence & Deletion Safety**: Records persist indefinitely in Firestore across devices and reinstalls. Tapping delete on any shoot, withdrawal, or studio triggers a high-contrast confirmation dialog before executing the Firestore document deletion.
- **Security Rules Validation**: Zero-trust `firestore.rules` enforce `request.auth != null && request.auth.uid == userId` along with strict schema validation for `studios`, `shoots`, and `withdrawals`, verified against local Firebase emulators before deployment.
