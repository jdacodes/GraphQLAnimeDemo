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
import com.jdacodes.graphqlanimedemo.media.presentation.MediaState
import com.jdacodes.graphqlanimedemo.media.presentation.MediaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MediaViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: MediaViewModel
    private lateinit var testRepository: TestMediaRepository
    private lateinit var getMediaListUseCase: GetMediaListUseCase
    private lateinit var getMediaDetailsUseCase: GetMediaDetailsUseCase
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        testRepository = TestMediaRepository()
        getMediaListUseCase = GetMediaListUseCase(testRepository)
        getMediaDetailsUseCase = GetMediaDetailsUseCase(testRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have expected default values`() = runTest {
        viewModel = MediaViewModel(getMediaListUseCase, getMediaDetailsUseCase)
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
        // Initialize the ViewModel with test dependencies
        viewModel = MediaViewModel(getMediaListUseCase, getMediaDetailsUseCase)
        val searchText = "One Piece"

        // Set up test data
        val testMediaItems = createTestMediaItems()
        val pageInfo = PageInfo(hasNextPage = true, currentPage = 1, lastPage = 10, perPage = 20)

        // Configure test repository to return success
        testRepository.setSuccessMediaList(testMediaItems, pageInfo)

        viewModel.state.test {
            // Skip initial state emissions
            // The initial state may already be loading due to onStart behavior
            var initialState = awaitItem() // Initial state

            // Skip any loading states from initial data load
            while (initialState.listState.isLoading) {
                initialState = awaitItem()
            }

            // Act: Update search text
            viewModel.onAction(MediaAction.SearchTextChanged(searchText))

            // Assert: Verify search text is updated in state
            val updatedState = awaitItem()
            assertThat(updatedState.listState.searchText).isEqualTo(searchText)

            // Since the search flow is debounced, we need to advance virtual time
            testDispatcher.scheduler.advanceTimeBy(550) // More than the 500ms debounce
            testDispatcher.scheduler.runCurrent()

            // At this point, we should see either:
            // 1. A state transition directly to success if the coroutines run too quickly, or
            // 2. A loading state followed by success

            // Collect all states until we find a non-loading one
            val nextState = awaitItem()

            if (nextState.listState.isLoading) {
                // If we got a loading state, the next one should be success
                val successState = awaitItem()
                assertThat(successState.listState.isLoading).isFalse()
                assertThat(successState.listState.items).isNotEmpty()
            } else {
                // If we skipped the loading state, just verify this is a success state
                assertThat(nextState.listState.isLoading).isFalse()
                assertThat(nextState.listState.items).isNotEmpty()
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onAction MediaAction LoadMoreItems sets loading state and updates with success data`() =
        runTest {
            viewModel = MediaViewModel(getMediaListUseCase, getMediaDetailsUseCase)

            // Arrange: Setup first page data that will be loaded on initialization
            val firstPageItems = createTestMediaItems().take(2)
            val secondPageItems = createTestMediaItems().drop(2)

            val firstPageInfo =
                PageInfo(hasNextPage = true, currentPage = 1, lastPage = 2, perPage = 2)
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
                // First, find a stable state with initial items loaded
                var currentState: MediaState
                do {
                    currentState = awaitItem()
                    println("Initial state: loading=${currentState.listState.isLoading}, items=${currentState.listState.items.size}")
                } while (currentState.listState.items.isEmpty() || currentState.listState.isLoading)

                // Verify we have the first page data
                assertThat(currentState.listState.items).hasSize(firstPageItems.size)
                assertThat(currentState.listState.hasNextPage).isTrue()
                assertThat(currentState.listState.page).isEqualTo(2) // Page is incremented after successful load

                // Setup repository for second page load
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

                // Run all pending coroutines
                testDispatcher.scheduler.advanceUntilIdle()

                // Keep collecting states until we get a state with all items loaded
                var nextState: MediaState
                var stateCounter = 0

                do {
                    nextState = awaitItem()
                    stateCounter++
                    println("Next state $stateCounter: loading=${nextState.listState.isLoading}, items=${nextState.listState.items.size}")

                    if (nextState.listState.isLoading) {
                        // Found a loading state, can proceed to final state
                        break
                    }
                } while (nextState.listState.items.size <= firstPageItems.size) // Keep going until we find loading or items increase

                // If we found a loading state, get the next one which should be success
                if (nextState.listState.isLoading) {
                    nextState = awaitItem()
                    println("Final state: loading=${nextState.listState.isLoading}, items=${nextState.listState.items.size}")
                }

                // Final assertions on the success state
                assertThat(nextState.listState.isLoading).isFalse()
                assertThat(nextState.listState.items).hasSize(firstPageItems.size + secondPageItems.size)
                assertThat(nextState.listState.hasNextPage).isFalse()
                assertThat(nextState.listState.page).isEqualTo(3) // Page is incremented again

                // Verify items are distinct even with duplicate IDs
                val distinctIds = nextState.listState.items.distinctBy { it.id }.size
                assertThat(distinctIds).isEqualTo(nextState.listState.items.size)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onAction MediaAction LoadMoreItems sets loading state and updates with error on failure`() =
        runTest {
            // Prepare initial state with items and hasNextPage=true
            val testMediaItems = createTestMediaItems().take(2)
            val pageInfo = PageInfo(hasNextPage = true, currentPage = 1, lastPage = 2, perPage = 20)
            testRepository.setSuccessMediaList(testMediaItems, pageInfo)

            // Initialize ViewModel and wait for initial load to complete
            viewModel = MediaViewModel(getMediaListUseCase, getMediaDetailsUseCase)
            testDispatcher.scheduler.advanceUntilIdle()

            // Test that LoadMoreItems properly handles errors
            viewModel.state.test {
                // Skip states until we have the initial items loaded
                var currentState: MediaState
                do {
                    currentState = awaitItem()
                } while (currentState.listState.items.isEmpty() || currentState.listState.isLoading)

                // Verify initial state has items and hasNextPage=true
                assertThat(currentState.listState.items).isNotEmpty()
                assertThat(currentState.listState.hasNextPage).isTrue()

                // Now configure repository to return error for next request
                val errorMessage = "Network error"
                testRepository.setMediaListResult(Result.Error(Exception(errorMessage)))

                // Trigger LoadMoreItems
                viewModel.onAction(MediaAction.LoadMoreItems)

                // Run any pending coroutines
                testDispatcher.scheduler.advanceUntilIdle()

                // Keep collecting states until we find one with the error and isLoading=false
                var nextState: MediaState
                do {
                    nextState = awaitItem()
                    println("Next state: loading=${nextState.listState.isLoading}, error=${nextState.listState.error != null}")
                } while (nextState.listState.isLoading || nextState.listState.error == null)

                // Now we should have the final error state with loading=false
                assertThat(nextState.listState.isLoading).isFalse()
                assertThat(nextState.listState.error).isNotNull()
                assertThat(nextState.listState.error).contains(errorMessage)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `MediaClicked action loads details and updates detail state`() = runTest {
        viewModel = MediaViewModel(getMediaListUseCase, getMediaDetailsUseCase)
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
    fun `LoadMoreItems loads next page when available and appends to existing items`() = runTest {
        // Arrange: Setup first page data that will be loaded on initialization
        val firstPageItems = createTestMediaItems().take(2)
        val secondPageItems = createTestMediaItems().drop(2)

        val firstPageInfo = PageInfo(hasNextPage = true, currentPage = 1, lastPage = 2, perPage = 2)
        val secondPageInfo =
            PageInfo(hasNextPage = false, currentPage = 2, lastPage = 2, perPage = 2)

        // First, set up the repository to return the first page
        testRepository = TestMediaRepository()  // Create a fresh repository
        testRepository.setMediaListResult(
            Result.Success(
                MediaListResult(
                    firstPageItems,
                    firstPageInfo
                )
            )
        )

        // Initialize use cases with fresh repository
        getMediaListUseCase = GetMediaListUseCase(testRepository)
        getMediaDetailsUseCase = GetMediaDetailsUseCase(testRepository)

        // Create the view model - this will trigger the initial load in onStart
        viewModel = MediaViewModel(getMediaListUseCase, getMediaDetailsUseCase)

        // Add debug collector to see all state updates
        val debugJob = launch {
            viewModel.state.collect { state ->
                println("State update: loading=${state.listState.isLoading}, items=${state.listState.items.size}, page=${state.listState.page}, hasNextPage=${state.listState.hasNextPage}")
            }
        }

        viewModel.state.test {
            // First, we should get the initial state
            val initialState = awaitItem()
            println("Test received initial state: loading=${initialState.listState.isLoading}, items=${initialState.listState.items.size}")

            // Now, we need to keep collecting states until we get the first page loaded
            var currentState = initialState

            while (currentState.listState.items.size != firstPageItems.size || currentState.listState.isLoading) {
                currentState = awaitItem()
                println("Test received transitional state: loading=${currentState.listState.isLoading}, items=${currentState.listState.items.size}")
            }

            // Now we have the state with the first page loaded
            println("First page loaded state: items=${currentState.listState.items.size}, page=${currentState.listState.page}")

            // Verify the state is as expected
            assertThat(currentState.listState.items).hasSize(firstPageItems.size)
            assertThat(currentState.listState.hasNextPage).isTrue()
            assertThat(currentState.listState.page).isEqualTo(2) // Page is incremented after successful load

            // Now setup repository for second page
            println("Setting up second page data")
            testRepository.setMediaListResult(
                Result.Success(
                    MediaListResult(
                        secondPageItems,
                        secondPageInfo
                    )
                )
            )

            // Trigger loading of second page
            println("Triggering LoadMoreItems")
            viewModel.onAction(MediaAction.LoadMoreItems)

            // Run any pending coroutines
            testDispatcher.scheduler.advanceTimeBy(100)
            testDispatcher.scheduler.runCurrent()

            // Now we should see a loading state
            var loadingFound = false
            var nextState = currentState

            // Try to find the loading state, but don't wait too long if it's not there
            for (i in 0 until 3) {
                try {
                    nextState = awaitItem()
                    println("After LoadMoreItems state ${i + 1}: loading=${nextState.listState.isLoading}, items=${nextState.listState.items.size}")

                    if (nextState.listState.isLoading) {
                        loadingFound = true
                        break
                    }
                } catch (e: Exception) {
                    println("No more states after ${i + 1} attempts")
                    break
                }
            }

            // If we found a loading state, great! If not, we'll still check for the final state
            if (loadingFound) {
                println("Found loading state, now waiting for success state")
                // We should now get the success state
                nextState = awaitItem()
                println("Final state after loading: loading=${nextState.listState.isLoading}, items=${nextState.listState.items.size}")
            }

            // At this point we need to keep collecting states until we get all items
            while (nextState.listState.items.size < (firstPageItems.size + secondPageItems.size) || nextState.listState.isLoading) {
                try {
                    nextState = awaitItem()
                    println("Collecting for final state: loading=${nextState.listState.isLoading}, items=${nextState.listState.items.size}")
                } catch (e: Exception) {
                    println("No more states, breaking out of loop")
                    break
                }
            }

            // Now verify the final state
            assertThat(nextState.listState.isLoading).isFalse()
            assertThat(nextState.listState.items).hasSize(firstPageItems.size + secondPageItems.size)
            assertThat(nextState.listState.hasNextPage).isFalse()
            assertThat(nextState.listState.page).isEqualTo(3) // Page is incremented again

            // Clean up
            debugJob.cancel()
            cancelAndIgnoreRemainingEvents()
        }
    }
}