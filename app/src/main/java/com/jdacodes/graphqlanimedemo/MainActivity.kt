package com.jdacodes.graphqlanimedemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jdacodes.graphqlanimedemo.navigation.HomeNavigation
import com.jdacodes.graphqlanimedemo.ui.theme.GraphQLAnimeDemoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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

