// app/src/androidTest/java/com/jdacodes/graphqlanimedemo/MediaListTest.kt

package com.jdacodes.graphqlanimedemo

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.jdacodes.graphqlanimedemo.core.util.TestTags
import com.jdacodes.graphqlanimedemo.media.presentation.MediaAction
import com.jdacodes.graphqlanimedemo.media.presentation.MediaList
import com.jdacodes.graphqlanimedemo.media.presentation.MediaListState
import com.jdacodes.graphqlanimedemo.media.domain.model.MediaListItem
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MediaListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingIndicatorIsDisplayed_whenIsLoadingTrue() {
        // Define a fake state with loading true
        val fakeState = MediaListState(isLoading = true)

        // Set the content to be tested
        composeTestRule.setContent {
            MediaList(
                listState = fakeState,
                onAction = {}
            )
        }

        composeTestRule
            .onNodeWithTag(TestTags.LoadingIndicator)
            .assertIsDisplayed()
    }

    @Test
    fun mediaItemsDisplayCorrectly() {
        val items = persistentListOf(
            MediaListItem(id = 1, titleEnglish = "Title 1",
                titleRomaji = null,
                description = null,
                coverImageMedium = null,
                coverImageLarge = null,
                averageScore = null,
                studios = listOf()
            ),
            MediaListItem(id = 2, titleEnglish = "Title 2",
                titleRomaji = null,
                description = null,
                coverImageMedium = null,
                coverImageLarge = null,
                averageScore = null,
                studios = listOf()
            )
        )
        val fakeState = MediaListState(items = items, isLoading = false)

        composeTestRule.setContent {
            MediaList(
                listState = fakeState,
                onAction = {}
            )
        }

        composeTestRule
            .onNodeWithTag(TestTags.PaginatedList)
            .assertIsDisplayed()

        items.forEach {
            composeTestRule
                .onNodeWithTag("${TestTags.MediaListItem}_${it.id}")
                .assertIsDisplayed()
        }
    }

    @Test
    fun loadingTransitionToDisplayItems() {
        // Initial state with loading
        val state = mutableStateOf(MediaListState(isLoading = true))
        val items = persistentListOf(
            MediaListItem(id = 1, titleEnglish = "Title 1",
                titleRomaji = null,
                description = null,
                coverImageMedium = null,
                coverImageLarge = null,
                averageScore = null,
                studios = listOf()
            ),
            MediaListItem(id = 2, titleEnglish = "Title 2",
                titleRomaji = null,
                description = null,
                coverImageMedium = null,
                coverImageLarge = null,
                averageScore = null,
                studios = listOf()
            )
        )

        // Set the initial content
        composeTestRule.setContent {
            MediaList(
                listState = state.value,
                onAction = {}
            )
        }
        
        // Verify the loading indicator is displayed
        composeTestRule
            .onNodeWithTag(TestTags.LoadingIndicator)
            .assertIsDisplayed()
        
        // Change the state to load items
        state.value = MediaListState(items = items, isLoading = false)
        
        // Wait for idle to ensure Compose recomposes with the updated state
        composeTestRule.waitForIdle()

        // Verify the paginated list is displayed
        composeTestRule
            .onNodeWithTag(TestTags.PaginatedList)
            .assertIsDisplayed()

        // Verify that items are displayed
        items.forEach {
            composeTestRule
                .onNodeWithTag("${TestTags.MediaListItem}_${it.id}")
                .assertIsDisplayed()
        }
    }

    @Test
    fun onClickMediaItemTriggersAction() {
        // Initial state with items to verify click
        val items = persistentListOf(
            MediaListItem(id = 1, titleEnglish = "Title 1",
                titleRomaji = null,
                description = null,
                coverImageMedium = null,
                coverImageLarge = null,
                averageScore = null,
                studios = listOf()
            ),
            MediaListItem(id = 2, titleEnglish = "Title 2",
                titleRomaji = null,
                description = null,
                coverImageMedium = null,
                coverImageLarge = null,
                averageScore = null,
                studios = listOf()
            )
        )
        val state = mutableStateOf(MediaListState(items = items))

        var actionTriggered = false
        var clickedMediaId: Int? = null

        // Set the content under test
        composeTestRule.setContent {
            MediaList(
                listState = state.value,
                onAction = { action ->
                    when (action) {
                        is MediaAction.MediaClicked -> {
                            actionTriggered = true
                            clickedMediaId = action.mediaId
                        }
                        else -> Unit
                    }
                }
            )
        }

        // Perform click action on the first media item
        composeTestRule
            .onNodeWithTag("${TestTags.MediaListItem}_1")
            .performClick()
        
        // Assert that the MediaClicked action has been triggered with the correct ID
        assertTrue(actionTriggered)
        assertTrue(clickedMediaId == 1)
    }
}