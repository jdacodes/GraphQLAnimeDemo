package com.jdacodes.graphqlanimedemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jdacodes.graphqlanimedemo.ui.theme.GraphQLAnimeDemoTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GraphQLAnimeDemoTheme {


                MainNavHost()


            }
        }
    }
}

@Composable
private fun MainNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = NavigationDestinations.MEDIA_LIST) {
        composable(route = NavigationDestinations.MEDIA_LIST) {
            MediaListDetailPaneScaffold()
        }
    }

}
