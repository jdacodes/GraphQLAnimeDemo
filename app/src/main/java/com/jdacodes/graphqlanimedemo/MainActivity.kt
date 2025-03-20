package com.jdacodes.graphqlanimedemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jdacodes.graphqlanimedemo.ui.theme.GraphQLAnimeDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GraphQLAnimeDemoTheme {
               HomeNavigation()


            }
        }
    }
}

@Composable
private fun MainNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Navigation.Home.MediaListDetail) {
        composable<Navigation.Home.MediaListDetail> {
            MediaListDetailPaneScaffold()
        }
    }

}
