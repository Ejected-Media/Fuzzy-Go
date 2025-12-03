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
