package com.jdacodes.graphqlanimedemo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jdacodes.graphqlanimedemo.favorite.FavoriteRoot

@Composable
fun HomeNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination?.let {
        when (it.route) {
            Navigation.Home.MediaListDetail.route -> Navigation.Home.MediaListDetail
            Navigation.Home.Dashboard.route -> Navigation.Home.Dashboard
            Navigation.Home.Favorites.route -> Navigation.Home.Favorites

            else ->  Navigation.Home.MediaListDetail
        }
    }
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            homeNavigationItems().forEach { (screen, icon) ->

                item(
                    icon = { Icon(icon, contentDescription = screen.route) },
                    label = { Text(screen.route.replace("_", " ").replaceFirstChar { it.uppercase() }) },
                    selected = screen.route == navController.currentDestination?.route,
                    onClick = {
                        navController.navigate(screen) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        content = {
                NavHost(navController, startDestination = currentScreen ?: Navigation.Home.MediaListDetail) {
                    composable<Navigation.Home.Dashboard> { DashboardScreen() }
                    composable<Navigation.Home.MediaListDetail> { MediaListDetailPaneScaffold() }
                    composable<Navigation.Home.Favorites> { FavoriteRoot() }
                }

        }
    )
}

@Composable
fun DashboardScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Dashboard Screen",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}


