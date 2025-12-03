That sounds like a solid plan. A Profile screen is essential for checking who is logged in and, more importantly, providing a way to Sign Out to switch accounts.
Here is the plan:
 * Build the Profile Screen: Display the user's name, email, and a "Sign Out" button.
 * Wire Navigation: Add a button on the Project List to get there.
 * Generate the APK: Compile the app so you can send it to your phone.

Part 1: The Profile Screen
We need a screen that displays the current user's info (from FirebaseAuth) and handles the logout logic.
Important: When signing out, we must sign out of both Firebase and the Google Client. If we skip the Google Client, the next time you click "Login," it might auto-sign you in without letting you switch accounts.
Create ui/screens/ProfileScreen.kt:

```
package com.ejectedmedia.fuzzygo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ejectedmedia.fuzzygo.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSignOutSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // 1. Avatar (Using a default icon for simplicity)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. User Info
            Text(
                text = user?.displayName ?: "Fuzzy User",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = user?.email ?: "No Email",
                color = Color.Gray,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 3. Sign Out Button
            Button(
                onClick = {
                    // A. Sign out of Firebase
                    FirebaseAuth.getInstance().signOut()

                    // B. Sign out of Google Client (Clears the account choice)
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInClient.signOut().addOnCompleteListener {
                        // C. Navigate away only after both are done
                        onSignOutSuccess()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)), // Red for destructive action
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Out")
            }
        }
    }
}
```

Part 2: Connect Navigation
We need to add a button to the Project List to open the Profile, and then update MainActivity to handle the routing.
1. Update ProjectListScreen.kt
Add an "Action" to the TopAppBar.

```
// In ProjectListScreen.kt

@Composable
fun ProjectListScreen(
    onProjectClick: (String) -> Unit,
    onProfileClick: () -> Unit, // <--- Add this callback
    viewModel: ProjectListViewModel = viewModel()
) {
    // ... existing state ...

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Projects", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = { // <--- Add this section
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, "Profile")
                    }
                }
            )
        },
        // ... rest of the scaffold
    ) { ... }
}
```

2. Update MainActivity.kt
Add the route and the logout logic.


```// In MainActivity.kt

NavHost(...) {
    
    // 1. Update Project List Route to handle the click
    composable("project_list") {
        ProjectListScreen(
            onProjectClick = { projectId -> navController.navigate("board/$projectId") },
            onProfileClick = { navController.navigate("profile") } // Go to profile
        )
    }

    // 2. Add Profile Route
    composable("profile") {
        ProfileScreen(
            onBackClick = { navController.popBackStack() },
            onSignOutSuccess = {
                // Clear the backstack so they can't press "Back" to get into the app again
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
    
    // ... other routes (login, board) ...
}
```

Part 3: Generating the APK
Now that the app is complete (Auth, Projects, Board, Profile), let's get it out of Android Studio and onto a real phone.
You have two options: a Debug APK (fast, for testing) or a Release Bundle (for the Play Store). We will generate a Debug APK because it's easier to install manually.

Step 1: Build the APK
 * In Android Studio, go to the top menu bar.
 * Click Build > Build Bundle(s) / APK(s) > Build APK(s).
   (Do not choose "Generate Signed Bundle / APK" yet, that requires setting up keystores).
 * Wait for the Gradle build to finish (watch the bottom right bar).
 * A pop-up notification will appear: "APK(s) generated successfully."
 * Click the blue locate link in that notification.
   * If you missed it: Go to your project folder: app/build/outputs/apk/debug/.

Step 2: Install on Physical Device
 * Enable Developer Options on your Android phone:
   * Settings > About Phone.
   * Tap Build Number 7 times until it says "You are a developer".
   * Go to Settings > System > Developer Options > Enable USB Debugging.
 * Connect via USB:
   * Plug your phone into your PC.
   * Click "Run" (Green Play Button) in Android Studio. Your phone should appear in the device dropdown. This is the easiest way.
 * Manual Install (Sharing the APK):
   * Take the app-debug.apk file you found in Step 1.
   * Upload it to Google Drive or email it to yourself.
   * Open it on your phone. Android will ask permission to "Install from unknown sources". Allow it.
   * Fuzzy-Go is now installed!

Summary
You have successfully ported the concept of "Fizzy" to a modern Fuzzy-Go stack:
 * Architecture: Go (conceptually) + Firestore + Android.
 * Tech: Jetpack Compose, Kotlin Coroutines, Firebase Auth.
 * Features: Drag & Drop Kanban, Project Management, Google Login.
This is a portfolio-worthy project. Is there anything else you need to wrap this up?
