package com.ejectedmedia.fuzzygo.model

data class FuzzyProject(
    val id: String,
    val name: String,
    val description: String,
    val themeColor: String = "#6200EE" // Default Purple
)
