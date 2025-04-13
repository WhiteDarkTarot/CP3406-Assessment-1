package com.example.assessment1.model

data class Recipe(
    val id: String,
    val title: String,
    val imageUrl: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val hasGluten: Boolean,
    val category: String
)