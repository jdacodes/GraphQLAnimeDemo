package com.jdacodes.graphqlanimedemo

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.jdacodes.graphqlanimedemo.core.util.Result
import com.jdacodes.graphqlanimedemo.media.domain.model.MediaListResult
import com.jdacodes.graphqlanimedemo.media.domain.model.PageInfo
import com.jdacodes.graphqlanimedemo.media.domain.usecase.GetMediaDetailsUseCase
import com.jdacodes.graphqlanimedemo.media.domain.usecase.GetMediaListUseCase
import com.jdacodes.graphqlanimedemo.media.presentation.MediaAction
import com.jdacodes.graphqlanimedemo.media.presentation.MediaDetailsUiState
import com.jdacodes.graphqlanimedemo.media.presentation.MediaViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MediaViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()


    private lateinit var viewModel: MediaViewModel
    private lateinit var testRepository: TestMediaRepository
    private lateinit var getMediaListUseCase: GetMediaListUseCase
    private lateinit var getMediaDetailsUseCase: GetMediaDetailsUseCase

    @Before
    fun setup() {
        testRepository = TestMediaRepository()
        getMediaListUseCase = GetMediaListUseCase(testRepository)
        getMediaDetailsUseCase = GetMediaDetailsUseCase(testRepository)
        viewModel = MediaViewModel(getMediaListUseCase, getMediaDetailsUseCase)
    }

    @Test
    fun `initial state should have expected default values`() = runTest {
        viewModel.state.test {
            val initialState = awaitItem()

            // Verify expected default values
            assertThat(initialState.listState.isLoading).isFalse()
            assertThat(initialState.listState.items).isEmpty()
            assertThat(initialState.listState.error).isNull()
            assertThat(initialState.listState.searchText).isEmpty()
            assertThat(initialState.listState.page).isEqualTo(1)
            assertThat(initialState.detailState.uiState).isInstanceOf(MediaDetailsUiState.Loading::class.java)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onAction MediaAction SearchTextChanged updates search text in state`() = runTest {
        viewModel.state.test {
            // Skip initial state
            awaitItem()

            // Act: Update search text
            val searchText = "One Piece"
            viewModel.onAction(MediaAction.SearchTextChanged(searchText))

            // Verify state update
            val newState = awaitItem()
            assertThat(newState.listState.searchText).isEqualTo(searchText)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onAction MediaAction LoadMoreItems sets loading state and updates with success data`() =
        runTest {
            // Arrange: Setup test data
            val testMediaItems = createTestMediaItems()
            val pageInfo =
                PageInfo(hasNextPage = true, currentPage = 1, lastPage = 10, perPage = 20)
            val mediaListResult = MediaListResult(testMediaItems, pageInfo)
            testRepository.setMediaListResult(Result.Success(mediaListResult))

            // Start collecting state
            viewModel.state.test {
                // Skip initial state
                awaitItem()

                // Handle initial data loading from ViewModel's onStart behavior
                var currentState = awaitItem() // This should be the loading state
                assertThat(currentState.listState.isLoading).isTrue()

                currentState = awaitItem() // This should be the success state after initial load
                assertThat(currentState.listState.isLoading).isFalse()
                assertThat(currentState.listState.items).hasSize(testMediaItems.size)
                assertThat(currentState.listState.page).isEqualTo(pageInfo.currentPage + 1)

                // Setup for LoadMoreItems test - prepare next page data
                val nextPageItems =
                    createTestMediaItems().map { it.copy(id = it.id + 100) } // Ensure unique IDs
                val nextPageInfo =
                    PageInfo(hasNextPage = false, currentPage = 2, lastPage = 10, perPage = 20)
                val nextPageResult = MediaListResult(nextPageItems, nextPageInfo)
                testRepository.setMediaListResult(Result.Success(nextPageResult))

                // Act: Call LoadMoreItems
                viewModel.onAction(MediaAction.LoadMoreItems)

                // Verify loading state
                val loadingState = awaitItem()
                assertThat(loadingState.listState.isLoading).isTrue()

                // Verify success state
                val successState = awaitItem()
                assertThat(successState.listState.isLoading).isFalse()
                assertThat(successState.listState.error).isNull()
                assertThat(successState.listState.items).hasSize(testMediaItems.size + nextPageItems.size)
                assertThat(successState.listState.hasNextPage).isEqualTo(nextPageInfo.hasNextPage)
                assertThat(successState.listState.page).isEqualTo(nextPageInfo.currentPage + 1)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onAction MediaAction LoadMoreItems sets loading state and updates with error on failure`() =
        runTest {
            // Arrange: Setup error response
            val errorMessage = "Network error"
            testRepository.setMediaListResult(Result.Error(Exception(errorMessage)))

            // Start collecting state
            viewModel.state.test {
                // Skip initial state
                awaitItem()

                // Act: Call method that triggers loadMediaList
                viewModel.onAction(MediaAction.LoadMoreItems)

                // Verify loading state
                val loadingState = awaitItem()
                assertThat(loadingState.listState.isLoading).isTrue()

                // Verify error state
                val errorState = awaitItem()
                assertThat(errorState.listState.isLoading).isFalse()
                assertThat(errorState.listState.error).isNotNull()
                assertThat(errorState.listState.error).contains(errorMessage)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `MediaClicked action loads details and updates detail state`() = runTest {
        // Arrange: Setup success response for media details
        val mediaId = 1
        val mediaDetails = createTestMediaDetails(mediaId)
        testRepository.setMediaDetailsResult(Result.Success(mediaDetails))

        // Start collecting state
        viewModel.state.test {
            // Skip initial state
            awaitItem()

            // Act: Trigger media clicked action
            viewModel.onAction(MediaAction.MediaClicked(mediaId))

            // There may be multiple state updates, so we need to find the success state
            var currentState = awaitItem()
            // Skip loading states if any
            while (currentState.detailState.uiState !is MediaDetailsUiState.Success &&
                currentState.detailState.uiState !is MediaDetailsUiState.Error
            ) {
                currentState = awaitItem()
            }

            // Assert: Verify detail state
            assertThat(currentState.detailState.uiState).isInstanceOf(MediaDetailsUiState.Success::class.java)
            val successState = currentState.detailState.uiState as MediaDetailsUiState.Success
            assertThat(successState.media).isEqualTo(mediaDetails)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SetTrailerFullscreen action updates isTrailerFullscreen state`() = runTest {
        // Setup initial data load to succeed
        val initialItems = createTestMediaItems().take(2)
        val initialPageInfo =
            PageInfo(hasNextPage = false, currentPage = 1, lastPage = 1, perPage = 20)
        testRepository.setMediaListResult(
            Result.Success(
                MediaListResult(
                    initialItems,
                    initialPageInfo
                )
            )
        )

        viewModel.state.test {
            // Initial state (this comes from default values)
            val initialState = awaitItem()
            assertThat(initialState.detailState.isTrailerFullscreen).isFalse()

            // We may see a loading state due to initial data load in onStart
            // Keep collecting until the loading state is done if it exists
            var currentState = initialState
            while (currentState.listState.isLoading) {
                currentState = awaitItem()
            }

            // Act: Set trailer to fullscreen
            viewModel.onAction(MediaAction.SetTrailerFullscreen(true))

            // Assert: Verify state update
            val updatedState = awaitItem()
            assertThat(updatedState.detailState.isTrailerFullscreen).isTrue()

            // Act: Set trailer to exit fullscreen
            viewModel.onAction(MediaAction.SetTrailerFullscreen(false))

            // Assert: Verify state update
            val finalState = awaitItem()
            assertThat(finalState.detailState.isTrailerFullscreen).isFalse()

            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `LoadMoreItems loads next page when available and appends to existing items`() = runTest {
        // Arrange: Setup first page data that will be loaded on initialization
        val firstPageItems = createTestMediaItems().take(2)
        val secondPageItems = createTestMediaItems().drop(2)

        val firstPageInfo = PageInfo(hasNextPage = true, currentPage = 1, lastPage = 2, perPage = 2)
        val secondPageInfo =
            PageInfo(hasNextPage = false, currentPage = 2, lastPage = 2, perPage = 2)

        // Set up repository to return first page result on first call
        testRepository.setMediaListResult(
            Result.Success(
                MediaListResult(
                    firstPageItems,
                    firstPageInfo
                )
            )
        )

        viewModel.state.test {
            // First, we should get the initial state
            val initialState = awaitItem()
            assertThat(initialState.listState.isLoading).isFalse()
            assertThat(initialState.listState.items).isEmpty()

            // Then, once the onStart loads data, we should get a loading state
            val loadingState = awaitItem()
            assertThat(loadingState.listState.isLoading).isTrue()

            // Then we should get the success state with first page data
            val firstPageState = awaitItem()
            assertThat(firstPageState.listState.isLoading).isFalse()
            assertThat(firstPageState.listState.items).hasSize(2)
            assertThat(firstPageState.listState.hasNextPage).isTrue()
            assertThat(firstPageState.listState.page).isEqualTo(2) // Page is incremented after successful load

            // Now setup repository for second page load
            testRepository.setMediaListResult(
                Result.Success(
                    MediaListResult(
                        secondPageItems,
                        secondPageInfo
                    )
                )
            )

            // Trigger loading of second page
            viewModel.onAction(MediaAction.LoadMoreItems)

            // We should see another loading state
            val secondLoadingState = awaitItem()
            assertThat(secondLoadingState.listState.isLoading).isTrue()

            // And finally the success state with both pages
            val combinedPagesState = awaitItem()
            assertThat(combinedPagesState.listState.isLoading).isFalse()
            assertThat(combinedPagesState.listState.items).hasSize(4)
            assertThat(combinedPagesState.listState.hasNextPage).isFalse()
            assertThat(combinedPagesState.listState.page).isEqualTo(3) // Page is incremented again

            // Verify items are distinct even with duplicate IDs
            val distinctIds = combinedPagesState.listState.items.distinctBy { it.id }.size
            assertThat(distinctIds).isEqualTo(combinedPagesState.listState.items.size)

            cancelAndIgnoreRemainingEvents()
        }
    }
}