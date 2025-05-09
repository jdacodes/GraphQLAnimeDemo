package com.jdacodes.graphqlanimedemo.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jdacodes.graphqlanimedemo.core.util.EventManager
import com.jdacodes.graphqlanimedemo.favorite.FavoriteRoot
import com.jdacodes.graphqlanimedemo.media.presentation.MediaListDetailRoot

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun HomeNavigation(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val navigator = rememberListDetailPaneScaffoldNavigator<Int>()

    // Save the route string instead of the Navigation object
    var currentRoute by rememberSaveable {
        mutableStateOf(Navigation.Home.MediaListDetail.route)
    }

    // Convert the route string to Navigation object
    val currentScreen = when (currentRoute) {
        Navigation.Home.MediaListDetail.route -> Navigation.Home.MediaListDetail
        Navigation.Home.Dashboard.route -> Navigation.Home.Dashboard
        Navigation.Home.Favorites.route -> Navigation.Home.Favorites
        else -> Navigation.Home.MediaListDetail
    }

    // Update currentRoute when navigation changes
    LaunchedEffect(backStackEntry?.destination?.route) {
        backStackEntry?.destination?.route?.let {
            currentRoute = it
        }
    }
    // Observe the centralized event flow
    LaunchedEffect(EventManager) {
        EventManager.eventFlow.collect { event ->
            when (event) {
                is EventManager.AppEvent.NavigateToDetail -> {

                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, event.mediaId)
                }

                else -> {}
            }
        }

    }
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            homeNavigationItems().forEach { (screen, icon) ->
                item(
                    icon = { Icon(icon, contentDescription = screen.route) },
                    label = {
                        Text(
                        stringResource(screen.labelResId).replaceFirstChar { it.uppercase() }
                        )
                    },
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
            NavHost(navController, startDestination = currentScreen) {
                composable<Navigation.Home.Dashboard> { DashboardScreen() }
                composable<Navigation.Home.MediaListDetail> { MediaListDetailRoot(navigator = navigator) }
                composable<Navigation.Home.Favorites> { FavoriteRoot() }
            }

        }
    )
}

//Placeholder screen
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


