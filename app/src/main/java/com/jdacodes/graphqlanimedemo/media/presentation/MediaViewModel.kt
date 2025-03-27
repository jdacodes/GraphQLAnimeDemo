package com.jdacodes.graphqlanimedemo.media.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdacodes.graphqlanimedemo.core.util.Result
import com.jdacodes.graphqlanimedemo.media.domain.usecase.GetMediaDetailsUseCase
import com.jdacodes.graphqlanimedemo.media.domain.usecase.GetMediaListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val getMediaListUseCase: GetMediaListUseCase,
    private val getMediaDetailsUseCase: GetMediaDetailsUseCase
) : ViewModel() {

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

            is MediaAction.SearchSubmitted -> {

                val currentState = state.value
                if (currentState.listState.searchText.isNotEmpty()) {
                    loadMediaList(
                        currentState.listState.page,
                        currentState.listState.perPage,
                        currentState.listState.searchText.ifEmpty { null })
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

    private fun loadMediaList(page: Int, perPage: Int, search: String?) {
        _state.update { currentState ->
            currentState.copy(listState = currentState.listState.copy(isLoading = true))
        }
        viewModelScope.launch {
            when (val result = getMediaListUseCase(page, perPage, search)) {
                is Result.Success -> {
                    val currentPageInfo = result.data.pageInfo
                    _state.update { currentState ->
                        val updatedMediaList = (currentState.listState.items + result.data.items)
                            .distinctBy { it.id }
                        currentState.copy(
                            listState = currentState.listState.copy(
                                items = updatedMediaList.toPersistentList(),
                                isLoading = false,
                                hasNextPage = currentPageInfo.hasNextPage ?: false,
                                page = currentPageInfo.currentPage.plus(1) ?: page
                            )
                        )
                    }
                }

                is Result.Error -> {
                    _state.update { currentState ->
                        currentState.copy(
                            listState = currentState.listState.copy(
                                isLoading = false,
                                error = "Error loading media: ${result.exception.message}"
                            )
                        )
                    }
                }

                Result.Loading -> {
                    //Loading is handled in Success and Error
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

            when (val result = getMediaDetailsUseCase(mediaId)) {
                is Result.Success -> {
                    _state.update { currentState ->
                        currentState.copy(
                            detailState = currentState.detailState.copy(
                                uiState = MediaDetailsUiState.Success(result.data)
                            )
                        )
                    }
                    _navigationChannel.send(mediaId)
                }

                is Result.Error -> {
                    _state.update { currentState ->
                        currentState.copy(
                            detailState = currentState.detailState.copy(
                                uiState = MediaDetailsUiState.Error("Error loading media details: ${result.exception.message}")
                            )
                        )
                    }
                }

                Result.Loading -> {
                    //Loading is handled in Success and Error
                }
            }
        }
    }
}