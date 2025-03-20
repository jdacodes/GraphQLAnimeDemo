package com.jdacodes.graphqlanimedemo

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun HomeNavigation() {
    val navController = rememberNavController()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            homeNavigationItems().forEach { (screen, icon) ->
                item(
                    icon = { Icon(icon, contentDescription = screen.route) },
                    label = { Text(screen.route.replace("_", " ").replaceFirstChar { it.uppercase() }) },
                    selected = screen.route == navController.currentDestination?.route,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        content = {
            NavHost(navController, startDestination = Navigation.Home.MediaListDetail) {
                composable<Navigation.Home.Dashboard> { DashboardScreen() }
                composable<Navigation.Home.MediaListDetail> { MediaListDetailPaneScaffold() }
                composable<Navigation.Home.Favorites> { FavoritesScreen() }
            }
        }
    )
}

@Composable
fun DashboardScreen() {
    TODO("Not yet implemented")
}

@Composable
fun FavoritesScreen() {
    TODO("Not yet implemented")
}
