package com.jdacodes.graphqlanimedemo

import kotlinx.serialization.Serializable

@Serializable
sealed class Navigation {
    @Serializable
    data object MediaList : Navigation()
}