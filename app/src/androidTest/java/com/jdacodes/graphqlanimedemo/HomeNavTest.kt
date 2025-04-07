package com.jdacodes.graphqlanimedemo

import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.jdacodes.graphqlanimedemo.media.domain.model.MediaListItem
import com.jdacodes.graphqlanimedemo.navigation.HomeNavigation
import com.jdacodes.graphqlanimedemo.navigation.Navigation
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

@HiltAndroidTest
class HomeNavTest {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var navController: TestNavHostController
    private lateinit var searchItemLabel: String
    private lateinit var dashboardItemLabel: String
    private lateinit var favoriteItemLabel: String

    private val fakeMediaItem = MediaListItem(
        id = 1899,
        titleEnglish = "One Piece",
        titleRomaji = "ONE PIECE",
        description = "One Piece is a long-running anime series that follows the adventures of Monkey D. Luffy and his crew, the Straw Hat Pirates, as they search for the ultimate treasure known as 'One Piece'.",
        coverImageMedium = "https://example.com/one-piece-medium.jpg",
        coverImageLarge = "https://example.com/one-piece-large.jpg",
        averageScore = 10,
        studios = listOf()
    )

    @Before
    fun setup() {
        // Ensure Hilt dependencies are injected before the activity is launched
        hiltRule.inject()

        composeTestRule.activity.setContent {
            searchItemLabel = stringResource(R.string.search)
            dashboardItemLabel = stringResource(R.string.dashboard)
            favoriteItemLabel = stringResource(R.string.favorites)
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            HomeNavigation(navController = navController)
        }
    }

    @Test
    fun verifyStartDestinationIsSearch() {
        composeTestRule.runOnIdle {
            assertTrue { navController.currentBackStackEntry?.destination?.hasRoute<Navigation.Home.MediaListDetail>() == true }
        }
    }
}