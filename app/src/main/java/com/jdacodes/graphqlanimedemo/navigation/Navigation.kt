package com.jdacodes.graphqlanimedemo.navigation

import androidx.annotation.StringRes 
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import com.jdacodes.graphqlanimedemo.R
import kotlinx.serialization.Serializable

@Serializable
sealed class Navigation {

    @Serializable
    sealed class Home(val route: String, @StringRes val labelResId: Int) : Navigation() {
        @Serializable
        data object MediaListDetail : Home(
            route = "media_list_detail", 
            labelResId = R.string.search 
        )

        @Serializable
        data object Favorites : Home(
            route = "favorites",
            labelResId = R.string.favorites
        )

        @Serializable
        data object Dashboard : Home(
            route = "dashboard",
            labelResId = R.string.dashboard
        )
    }

    @Serializable
    sealed class Authorization(val route: String, @StringRes val labelResId: Int) : Navigation() {
        @Serializable
        data object Login : Authorization(
            route = "login",
            labelResId = R.string.login
        )

        @Serializable
        data object Register : Authorization(
            route = "register",
            labelResId = R.string.register
        )
    }
}

fun homeNavigationItems() = listOf(
    Navigation.Home.Dashboard to Icons.Filled.Home,
    Navigation.Home.MediaListDetail to Icons.Filled.Search,
    Navigation.Home.Favorites to Icons.Filled.Favorite,
)
