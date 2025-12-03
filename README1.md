This is a fantastic project idea. Porting a Rails/Hotwire application to a Go + Firestore + Native Android architecture is a classic "modernization" exercise. It shifts the application from a Monolithic Server-Side Rendered (SSR) app to a decoupled Client-Server model.
This stack is particularly powerful for a Kanban/Issue tracking tool because Firestore's real-time capabilities map perfectly to moving cards across a board without needing manual refreshes.
Here is a high-level architectural plan to get us started.

1. The Architecture Shift
We are moving from a stateful server (Rails) to a stateless API (Go) with a "thick" client (Android).

| Feature | Original (Basecamp Fizzy) | Our Port (Fizzy-Go) |
|---|---|---|
| Language | Ruby | Go (Golang) |
| Database | SQLite / MySQL (Relational) | Firestore (NoSQL Document) |
| Frontend | HTML over the wire (Hotwire) | Native Android (Kotlin + Jetpack Compose) |
| Real-time | ActionCable / Turbo Streams | Firestore Snapshots / Listeners |
| Auth | Devise / Rails Session | Firebase Auth (Integration is seamless with Firestore) |

2. Key Challenges & Strategy
The Data Model (SQL vs. NoSQL)
This is the biggest hurdle. Fizzy (Rails) likely uses normalized tables (e.g., a cards table with a column_id foreign key). Firestore requires denormalization to be efficient.
 * The Challenge: In SQL, to move a card from "To Do" to "Done", you update a column_id. In Firestore, if we nest cards inside columns, we might have to move the actual document or use a root-level collection with queries.
 * The Solution: We will likely use a "Root Collections" approach for scalability:
   * projects/ (Collection)
   * columns/ (Collection - with project_id)
   * cards/ (Collection - with column_id)

The Android UI

For a Kanban board in 2025, Jetpack Compose is the only way to go.
 * We will need a drag-and-drop library (or build a custom Modifier) to handle moving cards between lists, which triggers the Go API (or updates Firestore directly).
The "Go" Role

Since Firestore can be accessed directly from the Android Client, we need to decide what the Go server actually does.
 * Option A (Backend-as-a-Service): Android talks directly to Firestore. The Go server uses Cloud Functions/Triggers for background tasks (e.g., sending emails when a card is moved, sanitizing data).
 * Option B (Traditional API): Android talks to the Go API (REST/gRPC). The Go API talks to Firestore. This is more secure and abstracts the DB, but loses some of Firestore's "free" real-time magic.
 * Recommendation: We should probably go with Option B (Go as a REST API) but allow the Android client to "listen" to Firestore documents for real-time updates. This is a common hybrid pattern.

3. Proposed Roadmap

Phase 1: Backend Core (Go + Firestore)
 * Initialize the Go module.
 * Set up the Firestore client in Go.
 * Design the Document Schema (Projects -> Lanes -> Cards).
 * Create basic CRUD endpoints (Create Card, Move Card).

Phase 2: Android Skeleton (Kotlin)
 * Setup a new Android Studio project.
 * Implement Firebase Auth (Google Sign-in).
 * Build the Data Layer (Repositories talking to the Go API).

Phase 3: The UI (Jetpack Compose)
 * Build the "Lane" Composable.
 * Build the "Card" Composable.
 * Implement Drag-and-Drop state management.

Next Step
To kick this off effectively, we need to agree on the database structure before writing code.
Would you like to start by defining the Firestore Schema (JSON structure for Projects and Cards), or would you prefer to initialize the Go repository first?
