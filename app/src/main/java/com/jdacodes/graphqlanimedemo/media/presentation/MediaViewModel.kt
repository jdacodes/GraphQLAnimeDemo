package com.jdacodes.graphqlanimedemo.media.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.exception.ApolloNetworkException
import com.jdacodes.graphqlanimedemo.MediaDetailsQuery
import com.jdacodes.graphqlanimedemo.MediaQuery
import com.jdacodes.graphqlanimedemo.media.data.remote.apolloClient
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MediaViewModel : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(MediaState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                /** Load initial data here **/
                loadMediaList(_state.value.listState.page, _state.value.listState.perPage, null)
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = MediaState()
        )

    @OptIn(FlowPreview::class)
    val debouncedSearchText: Flow<String> =
        combine(_state) { rootState ->
            val searchText = rootState[0].listState.searchText
            // Only create a new derived state if the search text is at least 3 characters
            // or empty (to show all results)
            if (searchText.isEmpty() || searchText.length >= 3) {
                searchText
            } else {
                // Return previous value or empty if less than minimum characters
                ""
            }
        }.debounce(500L).distinctUntilChanged()

    // Channel for navigation events
    private val _navigationChannel = Channel<Int>()
    val navigationChannel = _navigationChannel.receiveAsFlow()

    init {
        // Load media list initially
        viewModelScope.launch {
            debouncedSearchText.collect { debouncedSearchText ->
                // Reset and load new results when derived search text changes
                resetPaginationState()
                loadMediaList(
                    _state.value.listState.page,
                    _state.value.listState.perPage,
                    debouncedSearchText.ifEmpty { null }
                )
            }
        }
    }

    fun onAction(action: MediaAction) {
        when (action) {
            is MediaAction.SearchTextChanged -> {
                _state.update {
                    it.copy(listState = it.listState.copy(searchText = action.newText))
                }
            }

            is MediaAction.MediaClicked -> {
                loadMediaDetails(action.mediaId)
            }

            MediaAction.ResetSearch -> {
                _state.update {
                    it.copy(listState = it.listState.copy(searchText = ""))
                }
            }

            MediaAction.LoadMoreItems -> {
                val currentState = _state.value
                if (currentState.listState.hasNextPage && !currentState.listState.isLoading) {
                    loadMediaList(
                        currentState.listState.page,
                        currentState.listState.perPage,
                        currentState.listState.searchText.ifEmpty { null })
                }
            }

        }
    }

    private fun loadMediaList(
        page: Int,
        perPage: Int,
        search: String?
    ) {
        _state.update { currentState ->
            currentState.copy(listState = currentState.listState.copy(isLoading = true))
        }
        viewModelScope.launch {
            try {
                val response = apolloClient.query(
                    MediaQuery(
                        Optional.present(page),
                        Optional.present(perPage),
                        Optional.present(search)
                    )
                ).execute()

                val newMediaItems = response.data?.Page?.media?.filterNotNull().orEmpty()
                val currentPageInfo = response.data?.Page?.pageInfo

                _state.update { currentState ->
                    val updatedMediaList =
                        (currentState.listState.items + newMediaItems).distinctBy { it.id }
                    currentState.copy(
                        listState = currentState.listState.copy(
                            items = updatedMediaList.toPersistentList(),
                            isLoading = false,
                            hasNextPage = currentPageInfo?.hasNextPage ?: false,
                            page = currentPageInfo?.currentPage?.plus(1) ?: page
                        )
                    )
                }
            } catch (e: Exception) {
                _state.update { currentState ->
                    currentState.copy(
                        listState = currentState.listState.copy(
                            isLoading = false,
                            error = "Error loading media: ${e.message}"
                        )
                    )
                }
            }
        }
    }

    private fun resetPaginationState() {
        _state.update { currentState ->
            currentState.copy(
                listState = currentState.listState.copy(
                    page = 1,
                    hasNextPage = true,
                    items = persistentListOf()
                )
            )
        }
    }

    private fun loadMediaDetails(mediaId: Int) {
        _state.update { currentState ->
            currentState.copy(detailState = currentState.detailState.copy(uiState = MediaDetailsUiState.Loading))
        }
        viewModelScope.launch {
            try {
                val response =
                    apolloClient.query(MediaDetailsQuery(mediaId = Optional.present(mediaId)))
                        .execute()
                Log.d("QueryDebug", "Received response: ${response.data}")
                val newUiState = when {
                    response.errors.orEmpty().isNotEmpty() -> {
                        MediaDetailsUiState.Error(response.errors!!.first().message)
                    }

                    response.exception is ApolloNetworkException -> {
                        MediaDetailsUiState.Error("Please check your network connectivity.")
                    }

                    response.data?.Media != null -> {
                        response.data!!.Media?.let { MediaDetailsUiState.Success(it) }
                        // Send navigation event only on success
                        _navigationChannel.send(mediaId)
                        response.data!!.Media?.let { MediaDetailsUiState.Success(it) }
                    }

                    else -> {
                        MediaDetailsUiState.Error("Oh no... An error happened.")
                    }
                }
                Log.d("Fetch error", response.exception.toString())
                Log.d("Request error", response.errors.toString() + response.data.toString())
                Log.d("Field error", response.errors.toString() + response.data.toString())

                if (newUiState != null) {
                    _state.update { currentState ->
                        currentState.copy(
                            detailState = currentState.detailState.copy(
                                uiState = newUiState
                            )
                        )
                    }
                }

            } catch (e: Exception) {
                _state.update { currentState ->
                    currentState.copy(
                        detailState = currentState.detailState.copy(
                            uiState = MediaDetailsUiState.Error("Error loading media details: ${e.message}")
                        )
                    )
                }
            }
        }
    }
}