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
