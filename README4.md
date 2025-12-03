Since you are open to either, I will make the executive decision to do the .gitignore first.
This is critical security hygiene. If we accidentally commit that serviceAccountKey.json to GitHub, bots will find it in seconds and hijack your cloud resources.

Here is the plan:
 * Secure the Repo (.gitignore).
 * Define the Data Flow (Diagram).
 * Initialize the Android Client.

Step 1: The .gitignore (Do this immediately)
Create a file named .gitignore in the root of your project and paste this in. I've included rules for Go, Android (if you keep them in the same repo), and JetBrains IDEs.

```
# --- Security (CRITICAL) ---
serviceAccountKey.json
*.pem
.env

# --- Go ---
/bin/
/dist/
*.exe
*.test
vendor/

# --- Android / Gradle ---
.gradle/
/build/
/captures/
.externalNativeBuild/
.cxx/
local.properties

# --- OS / IDE ---
.DS_Store
.idea/
*.iml
```

Step 2: The Architecture "Hybrid" Model
Before we write the Kotlin code, we need to agree on how the Android app gets data.
Since we want Real-time updates (cards moving instantly) but also want a Go Backend for business logic, we should use a Hybrid Approach.
 * Reads (Green Line): The Android app listens directly to Firestore. When a document changes, the UI updates automatically.
 * Writes (Red Line): When a user drags a card, the Android app calls your Go API. The Go API validates the move (e.g., "Does this user have permission?"), and then Go updates Firestore.

Step 3: Initialize the Android Client (Kotlin + Compose)
Let's set up the Android dependencies for Fuzzy-Go.
Assuming you have created a new "Empty Activity" project in Android Studio (selecting Kotlin and Jetpack Compose):
Open your app/build.gradle.kts (Module level) and add these specific dependencies for Firestore and concurrency.

```
dependencies {
    // ... existing core dependencies ...

    // 1. Firebase Bill of Materials (BoM) - manages versions for us
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // 2. Firestore (NoSQL DB)
    implementation("com.google.firebase:firebase-firestore")

    // 3. Coroutines (for async background tasks)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3") // Vital for Firebase tasks

    // 4. Lifecycle (ViewModel integration)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    
    // 5. Navigation (Moving between Login -> Board)
    implementation("androidx.navigation:navigation-compose:2.7.7")
}
```

Sync your Gradle project.
Next Step: The First Feature
Now that the project is secure and the libraries are installed, we have two paths for the first actual code:
 * The "Lane" UI: We build the visual Kanban column component in Jetpack Compose.
 * The "Repository" Logic: We write the Kotlin code to fetch data from Firestore.
Which one feels like more fun right now: Visuals (UI) or Logic (Data)?
