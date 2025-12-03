This is a high-value skill. Implementing Google Sign-In correctly involves a specific "dance" between your Android App, the Google Auth servers, and Firebase.
Here is the flow we are going to build:
Prerequisites (The "Console" Work)

Before writing code, we must configure the cloud. Google rejects sign-in attempts from unknown apps.
 * Get your SHA-1 Fingerprint:
   * Open the Gradle tab on the right side of Android Studio.
   * Navigate to Tasks -> android -> signingReport.
   * Double-click it. Look at the "Run" console at the bottom.
   * Copy the SHA1 key (it looks like AD:44:00:D3...).
 * Add to Firebase:
   * Go to Firebase Console -> Project Settings -> General.
   * Scroll down to "Your Apps" and click "Add fingerprint". Paste the SHA-1.
 * Enable Google Auth:
   * Go to Firebase Console -> Build -> Authentication -> Sign-in method.
   * Click Google. Enable it.
   * Crucial: Make sure the "Support email" is selected. Save.

Step 1: Dependencies
Open app/build.gradle.kts and add the Auth libraries:

```
dependencies {
    // ... existing ...
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.0.0") 
}
```

Step 2: The Auth Repository
We need a clean place to handle the "Exchange". When Google says "Yes, this is Kyle," they give us an ID Token. We must pass that token to Firebase to say "Log this user into Firebase."
Create data/AuthRepository.kt:

```
package com.ejectedmedia.fuzzygo.data

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // check if user is already logged in
    val currentUser get() = auth.currentUser

    suspend fun signInWithGoogle(idToken: String): Boolean {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
```

Step 3: The Login ViewModel
This View Model handles the result coming back from the Google Sign-In screen.
Create ui/viewmodel/LoginViewModel.kt:

```
package com.ejectedmedia.fuzzygo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ejectedmedia.fuzzygo.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun handleGoogleIdToken(idToken: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val success = repository.signInWithGoogle(idToken)
            if (success) {
                _loginState.value = LoginState.Success
            } else {
                _loginState.value = LoginState.Error("Authentication Failed")
            }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState() // This triggers navigation
    data class Error(val message: String) : LoginState()
}
```

Step 4: The Login Screen (UI)
This is the tricky part. We need to launch the Google Sign-In intent using a standard Android launcher.
Create ui/screens/LoginScreen.kt:

```
package com.ejectedmedia.fuzzygo.ui.screens

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ejectedmedia.fuzzygo.R // Ensure you have a logo resource or remove the Image
import com.ejectedmedia.fuzzygo.ui.viewmodel.LoginState
import com.ejectedmedia.fuzzygo.ui.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val loginState by viewModel.loginState.collectAsState()

    // 1. Setup the Google Sign In Client
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id)) // Magic string from google-services.json
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    // 2. Setup the Result Launcher (What happens when we come back from Google)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                // Pass the ID Token to ViewModel to exchange for Firebase Credential
                account?.idToken?.let { viewModel.handleGoogleIdToken(it) }
            } catch (e: ApiException) {
                // Log error
            }
        }
    }

    // 3. Observe State for Navigation
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onLoginSuccess()
        }
    }

    // 4. The UI
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Fuzzy-Go",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6200EE)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            if (loginState is LoginState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        // Launch the Sign In Intent
                        launcher.launch(googleSignInClient.signInIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Sign in with Google")
                }
            }
            
            if (loginState is LoginState.Error) {
                Text(
                    text = (loginState as LoginState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
```

CRITICAL FIX: You will see a red error on R.string.default_web_client_id.
This string is automatically generated by the Google Services Gradle plugin at build time. If it is red, try Build -> Rebuild Project. If it still fails, it means google-services.json is missing or the plugin isn't applied in build.gradle.
Step 5: Update the Navigation Graph
Now we need to protect the app. If no user is logged in, show Login.
Open MainActivity.kt:

```
// ... inside setContent ...
val navController = rememberNavController()
val auth = FirebaseAuth.getInstance()
val startDestination = if (auth.currentUser != null) "project_list" else "login"

NavHost(navController = navController, startDestination = startDestination) {

    // 1. Login Screen
    composable("login") {
        LoginScreen(
            onLoginSuccess = {
                // Pop login off the stack so back button doesn't return to login
                navController.navigate("project_list") {
                    popUpTo("login") { inclusive = true }
                }
            }
        )
    }

    // 2. Project List
    composable("project_list") {
        ProjectListScreen(
            onProjectClick = { projectId -> navController.navigate("board/$projectId") }
        )
    }

    // ... Board Screen ...
}
```

Step 6: Use the Real User ID
Finally, go back to FuzzyRepository.kt (and ProjectListViewModel if logic is there) and stop using "user_123".

```
// In FuzzyRepository.kt

private val auth = FirebaseAuth.getInstance()

fun createProject(name: String, description: String) {
    val userId = auth.currentUser?.uid ?: return // Guard clause

    val projectData = hashMapOf(
        // ... existing fields ...
        "owner_id" to userId // NOW IT IS REAL!
    )
    // ...
}
```

The Result
 * Fresh Install: App opens to the "Fuzzy-Go" logo and a "Sign in with Google" button.
 * Click: Google System Dialog appears. You pick your account.
 * Loading: Spinner spins while Firebase verifies the token.
 * Success: You are forwarded to the Project List.
 * Restart App: Since auth.currentUser is cached, you skip login and go straight to the list.
Would you like to build a "Profile" screen (so you can sign out), or move on to generating the APK to install this on your physical device?
