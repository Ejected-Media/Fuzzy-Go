That is an excellent rebrand. Fuzzy-Go works on multiple levels:
 * Thematic: In software testing, "fuzzing" is a technique to find bugs, and undefined requirements are often called "fuzzy." It fits the problem domain perfectly.
 * Distinct: It separates us enough from the original Basecamp "Fizzy" project to stand on its own while keeping the GoLang suffix convention.
 * Vibe: It sounds friendlier and less corporate, which fits a modern, lightweight tool.
So, the official project is Fuzzy-Go.
Revised Tech Stack: "Fuzzy-Go"
 * Backend: GoLang (API & Business Logic)
 * Database: Google Firestore (NoSQL)
 * Client: Android Native (Kotlin + Jetpack Compose)
Since we are locking in the name, let’s tackle the Data Structure first. Firestore is schema-less, but if we don't plan the document hierarchy correctly now, querying "Done" items or moving cards later becomes a nightmare.
Here is a proposed Fuzzy-Go Schema optimized for a Kanban board.
Proposed Firestore Schema
We should use a Sub-collection pattern to keep data organized by project.

1. Collection: projects
 * Document ID: (Auto-ID)
 * Fields:
   * name: string ("Fuzzy-Go Mobile")
   * owner_id: string (User UID)
   * created_at: timestamp
   * theme_color: string (Hex code for the project UI)

2. Sub-collection: projects/{projectID}/lanes
 * Why a sub-collection? It creates a specific path for security rules (only project members can read).
 * Document ID: (Auto-ID or "todo", "doing", "done")
 * Fields:
   * title: string ("Backlog", "In Progress")
   * order_index: number (0, 1, 2 - crucial for sorting columns left-to-right)

3. Sub-collection: projects/{projectID}/cards
 * Note: I am deliberately putting cards at the Project level, NOT inside the Lane documents. This makes moving a card from "To Do" to "Done" a simple field update, rather than a delete-and-recreate operation.
 * Document ID: (Auto-ID)
 * Fields:
   * title: string ("Fix memory leak")
   * description: string ("Happens on login screen...")
   * lane_id: string (Reference to the Lane doc ID)
   * priority: string ("Low", "Fuzzy", "Critical")
   * assigned_to: array of strings (User UIDs)
   * tags: array of strings ("bug", "ui", "backend")

Does this flat "Cards at Project Level" structure make sense to you?
(The alternative is nesting cards inside lanes, but that makes dragging a card to a new lane much harder in Firestore.)
