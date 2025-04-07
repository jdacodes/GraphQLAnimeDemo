package com.jdacodes.graphqlanimedemo.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import com.jdacodes.graphqlanimedemo.R
import kotlinx.serialization.Serializable

@Serializable
sealed class Navigation {

    @Serializable
    sealed class Home(val route: String) : Navigation() {
        @Serializable
        data object MediaListDetail : Home(R.string.search.toString())

        @Serializable
        data object Favorites : Home(R.string.favorites.toString())

        @Serializable
        data object Dashboard : Home(R.string.dashboard.toString())
    }

    @Serializable
    sealed class Authorization(val route: String) : Navigation() {
        @Serializable
        data object Login : Authorization(R.string.login.toString())

        @Serializable
        data object Register : Authorization(R.string.register.toString())
    }

}

fun homeNavigationItems() = listOf(
    Navigation.Home.Dashboard to Icons.Filled.Home,
    Navigation.Home.MediaListDetail to Icons.Filled.Search,
    Navigation.Home.Favorites to Icons.Filled.Favorite,
)
