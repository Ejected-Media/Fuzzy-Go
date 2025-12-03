package com.ejectedmedia.fuzzygo.model

import java.util.Date

data class FuzzyCard(
    val id: String,
    val title: String,
    val description: String,
    val priority: FuzzyPriority = FuzzyPriority.LOW,
    val tags: List<String> = emptyList()
)

enum class FuzzyPriority {
    LOW, HIGH, CRITICAL, FUZZY
}


data class FuzzyLane(
    val id: String,
    val title: String,
    val cards: List<FuzzyCard> = emptyList()
)
