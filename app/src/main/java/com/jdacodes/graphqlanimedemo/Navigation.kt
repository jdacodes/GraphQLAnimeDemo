package com.jdacodes.graphqlanimedemo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import kotlinx.serialization.Serializable

@Serializable
sealed class Navigation {

    @Serializable
    sealed class Home(val route: String) : Navigation() {
        @Serializable
        data object MediaListDetail : Home("search")

        @Serializable
        data object Favorites : Home("favorites")

        @Serializable
        data object Dashboard : Home("dashboard")
    }

    @Serializable
    sealed class Authorization(val route: String) : Navigation() {
        @Serializable
        data object Login : Authorization("login")

        @Serializable
        data object Register : Authorization("register")
    }

}

fun homeNavigationItems() = listOf(
    Navigation.Home.Dashboard to Icons.Filled.Home,
    Navigation.Home.MediaListDetail to Icons.Filled.Search,
    Navigation.Home.Favorites to Icons.Filled.Favorite,
)
