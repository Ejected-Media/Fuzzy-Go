Staying in Kotlin is a solid choice. Let's make this app functional by connecting it to the cloud.
To replace our dummy data with real data, we need a Repository. In modern Android development, the Repository is the single source of truth. It will listen to Firestore and pipe data directly to our UI.
Since we want "Fizzy-like" real-time updates (e.g., if someone else moves a card, it moves on your screen instantly), we will use Kotlin Flows.

Prerequisite: The Config File
Before this code works, ensure you have downloaded the google-services.json file from your Firebase Console (Project Settings > Android App) and placed it in your app/ folder. Without this, the app crashes on launch!

Step 1: The Repository
Create a new file: app/src/main/java/com/ejectedmedia/fuzzygo/data/FuzzyRepository.kt.
This class does the heavy lifting:
 * It listens to the lanes collection.
 * It listens to the cards collection.
 * It combines them efficiently. When a card updates, it figures out which lane it belongs to and emits a fresh list to the UI.
<!-- end list -->

```
package com.ejectedmedia.fuzzygo.data

import com.ejectedmedia.fuzzygo.model.FuzzyCard
import com.ejectedmedia.fuzzygo.model.FuzzyLane
import com.ejectedmedia.fuzzygo.model.FuzzyPriority
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FuzzyRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Watches a specific project. 
     * Returns a real-time stream of Lanes with their Cards populated.
     */
    fun getProjectBoardFlow(projectId: String): Flow<List<FuzzyLane>> {
        val projectRef = firestore.collection("projects").document(projectId)

        // 1. Stream of Lanes (Columns)
        val lanesFlow = projectRef.collection("lanes")
            .orderBy("order_index", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    FuzzyLane(
                        id = doc.id,
                        title = doc.getString("title") ?: "Untitled",
                        cards = emptyList() // We populate this in the combine step
                    )
                }
            }

        // 2. Stream of Cards (All cards for this project)
        val cardsFlow = projectRef.collection("cards")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    FuzzyCard(
                        id = doc.id,
                        title = doc.getString("title") ?: "No Title",
                        description = doc.getString("description") ?: "",
                        // Helper to safely parse the enum string
                        priority = try {
                            val p = doc.getString("priority") ?: "LOW"
                            FuzzyPriority.valueOf(p.uppercase())
                        } catch (e: Exception) { FuzzyPriority.LOW },
                        tags = (doc.get("tags") as? List<String>) ?: emptyList(),
                        // We store the laneID strictly for mapping, though it's not in the UI model directly
                        // We might need to add laneId to FuzzyCard model if it's missing
                    ) to (doc.getString("lane_id") ?: "") // Return a Pair: Card + LaneID
                }
            }

        // 3. Merge them!
        // This block re-runs whenever EITHER lanes OR cards change in the DB.
        return lanesFlow.combine(cardsFlow) { lanes, cardPairs ->
            lanes.map { lane ->
                // Filter all cards to find the ones belonging to this lane
                val cardsInLane = cardPairs
                    .filter { it.second == lane.id } // check lane_id match
                    .map { it.first } // extract the Card object
                
                lane.copy(cards = cardsInLane)
            }
        }
    }
    
    // Helper to add a test card easily
    fun addCard(projectId: String, laneId: String, title: String) {
        val cardData = hashMapOf(
            "title" to title,
            "description" to "Created via Android",
            "lane_id" to laneId,
            "priority" to "LOW",
            "created_at" to com.google.firebase.Timestamp.now()
        )
        firestore.collection("projects").document(projectId)
            .collection("cards").add(cardData)
    }
}
```


Note: You may need to quickly add val laneId: String to your FuzzyCard data class if you want to track it inside the object, but the Pair approach above works without changing your model.
Step 2: The ViewModel
Now we need a ViewModel to hold this data and survive screen rotations.
Create app/src/main/java/com/ejectedmedia/fuzzygo/ui/viewmodel/BoardViewModel.kt.

```
package com.ejectedmedia.fuzzygo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ejectedmedia.fuzzygo.data.FuzzyRepository
import com.ejectedmedia.fuzzygo.model.FuzzyLane
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class BoardViewModel : ViewModel() {
    private val repository = FuzzyRepository()

    // The UI observes this. It starts empty.
    private val _boardState = MutableStateFlow<List<FuzzyLane>>(emptyList())
    val boardState: StateFlow<List<FuzzyLane>> = _boardState

    init {
        // For now, we hardcode the Project ID until we build a Project Picker
        loadProject("my-first-project")
    }

    private fun loadProject(projectId: String) {
        viewModelScope.launch {
            repository.getProjectBoardFlow(projectId)
                .catch { e -> 
                    // Handle error (e.g., permission denied, no internet)
                    println("Error loading board: ${e.message}")
                }
                .collect { lanes ->
                    _boardState.value = lanes
                }
        }
    }
    
    fun createTestCard(laneId: String) {
        repository.addCard("my-first-project", laneId, "New Task")
    }
}
```

Step 3: Connect UI to ViewModel
Finally, go back to your ProjectBoardScreen.kt and update the function signature to take the ViewModel.

```
@Composable
fun ProjectBoardScreen(
    viewModel: BoardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(), // Auto-inject
    modifier: Modifier = Modifier
) {
    // Collect the data from the ViewModel
    val lanes by viewModel.boardState.collectAsStateWithLifecycle() 
    // Note: You might need dependency: "androidx.lifecycle:lifecycle-runtime-compose:2.6.0+"

    Scaffold(...) { paddingValues ->
         Box(...) {
            if (lanes.isEmpty()) {
                // Show loading or empty state
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Waiting for data...")
                }
            } else {
                LazyRow(...) {
                    items(lanes) { lane ->
                        TaskLane(
                            title = lane.title,
                            cards = lane.cards,
                            onAddCardClick = { 
                                viewModel.createTestCard(lane.id) 
                            }
                        )
                    }
                }
            }
         }
    }
}
```

How to Test This

Since we haven't built the "Create Project" screen yet, the app will load "my-first-project" and show "Waiting for data..." because that project doesn't exist in Firestore yet.
To see it light up immediately:
 * Go to Firebase Console > Firestore.
 * Create a collection projects.
 * Create a document my-first-project.
 * Inside it, create a collection lanes.
 * Add a document: (Auto-ID) -> { title: "Todo", order_index: 0 }.

Run the app. Boom, the "Todo" column should appear instantly!
Ready to try running it, or do you want to build the "Drag and Drop" logic next?
