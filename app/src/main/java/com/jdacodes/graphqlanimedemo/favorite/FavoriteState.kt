package com.jdacodes.graphqlanimedemo.favorite

data class FavoriteState(
    val paramOne: String = "default",
    val paramTwo: List<String> = emptyList(),
)