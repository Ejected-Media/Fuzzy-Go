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
