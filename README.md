# Fuzzy-Go
~ Software Issue Tracking ° . +

___

Here is a professional, developer-ready README.md file for your repository.

I have structured it to highlight the modern tech stack (Compose, Firebase, Coroutines) and the unique "Hybrid" architecture we discussed. You can copy-paste this directly into your GitHub repo.

👾 Fuzzy-Go
Fuzzy-Go is a modern, native Android port of the Fizzy issue tracking concept. It reimagines the classic Kanban board using a reactive, real-time architecture powered by Jetpack Compose, Google Firestore, and a Go backend philosophy.

> "Fuzzy" because software requirements are rarely clear. "Go" because it moves fast.
> 
📱 Screenshots

| Project Picker | Kanban Board | Drag & Drop |
|---|---|---|
| (Add Screenshot) | (Add Screenshot) | (Add Screenshot) |

✨ Features
 * Real-Time Kanban Board: Cards move instantly across devices using Firestore snapshots.
 * Native Drag-and-Drop: Smooth, touch-optimized gestures built with Jetpack Compose DragAndDrop APIs.
 * Multi-Project Support: Manage separate boards for Mobile, Backend, and Design tasks.
 * Authentication: Secure Google Sign-In integration via Firebase Auth.
 * Reactive UI: Built 100% in Kotlin/Jetpack Compose using Material3 Design.
 * Dark/Light Mode: (Coming soon) Support for system themes.

🛠 Tech Stack
Android Client
 * Language: Kotlin
 * UI Framework: Jetpack Compose (Material3)
 * Architecture: MVVM (Model-View-ViewModel) + Clean Architecture
 * Async: Kotlin Coroutines & Flows
 * Navigation: Jetpack Compose Navigation
 * Dependency Injection: (Planned: Hilt/Koin)
Backend & Data
 * Database: Google Cloud Firestore (NoSQL)
 * Auth: Firebase Authentication
 * API: Go (Golang) - Planned for server-side business logic and validation.

🏗 Architecture
Fuzzy-Go uses a Hybrid Cloud Architecture.
 * Reads (Real-time): The Android client listens directly to Firestore collections via Flows. This ensures zero-latency UI updates when data changes.
 * Writes (Transactional): User actions (moves, edits) are sent to the backend (or direct to Firestore via Repository pattern) to ensure data integrity.
<!-- end list -->

graph LR
    Android[Android Client] -- Real-time Listener --> Firestore[(Google Firestore)]
    Android -- Actions (Write) --> GoAPI[Go Backend Service]
    GoAPI -- Validates & Updates --> Firestore

🚀 Getting Started
Prerequisites
 * Android Studio Koala (or newer)
 * JDK 17+
 * A Firebase Project

Installation
 * Clone the repo

```
   git clone https://github.com/Ejected-Media/Fuzzy-Go.git
cd Fuzzy-Go
```

 * Firebase Setup (Crucial)
   * Create a project in the Firebase Console.
   * Enable Firestore Database and Authentication (Google Sign-In).
   * Download google-services.json and place it in the /app directory.
   * Note: This file is ignored by git for security.
 * Build & Run
   * Open the project in Android Studio.
   * Sync Gradle files.
   * Run on an Emulator or Physical Device.

📂 Project Structure
com.ejectedmedia.fuzzygo

```
├── data           # Repository layer (Firestore & Auth logic)
├── model          # Data classes (Card, Lane, Project)
├── ui
│   ├── components # Reusable UI (TaskCard, TaskLane, Dialogs)
│   ├── screens    # Full screens (Login, ProjectList, Board)
│   ├── theme      # Type, Color, Theme
│   └── viewmodel  # State management
└── MainActivity.kt
```

🗺 Roadmap. 
 * [x] Project Structure & Firestore Setup
 * [x] Kanban Board UI (Lanes & Cards)
 * [x] Drag and Drop Logic
 * [x] Google Authentication
 * [x] Multi-Project Management
 * [ ] Go Backend API Integration
 * [ ] Offline Support (Firestore Cache)
 * [ ] Push Notifications for Card Assignments

🤝 Contributing. 
This is an Ejected Media project.  Contributions are welcome! Please fork the repository and submit a pull request.

📄 License

This project is open source.  
Built with 💜 and ☕ in Phoenix, AZ.
