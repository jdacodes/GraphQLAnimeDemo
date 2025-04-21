package com.jdacodes.graphqlanimedemo.media.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdacodes.graphqlanimedemo.core.util.EventManager
import com.jdacodes.graphqlanimedemo.core.util.EventManager.AppEvent
import com.jdacodes.graphqlanimedemo.core.util.Result
import com.jdacodes.graphqlanimedemo.media.domain.usecase.GetMediaDetailsUseCase
import com.jdacodes.graphqlanimedemo.media.domain.usecase.GetMediaListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
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
                loadMediaList(
                    page = _state.value.listState.page,
                    perPage = _state.value.listState.perPage,
                    search = null,
                    isAdult = _state.value.listState.isAdultChecked
                )
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = MediaState()
        )

    @OptIn(FlowPreview::class)
    val debouncedSearchText: Flow<String> = _state
        .map { it.listState.searchText }
        .debounce(500L)
        .distinctUntilChanged()
        .map { searchText ->
            if (searchText.isEmpty() || searchText.length >= 3) {
                searchText
            } else {
                ""
            }
        }
        .distinctUntilChanged()

    private val adultStateFlow = _state
        .map { it.listState.isAdultChecked }
        .distinctUntilChanged()

    init {
        viewModelScope.launch(IO) {
            debouncedSearchText.collectLatest { debouncedText ->
                if (hasLoadedInitialData) {
                    resetPaginationState()
                    loadMediaList(
                        page = 1,
                        perPage = _state.value.listState.perPage,
                        search = debouncedText.ifEmpty { null },
                        isAdult = _state.value.listState.isAdultChecked
                    )
                }
            }
        }

        adultStateFlow
            .filter { hasLoadedInitialData }
            .onEach { isAdult ->
                resetPaginationState()
                loadMediaList(
                    page = 1,
                    perPage = _state.value.listState.perPage,
                    search = _state.value.listState.searchText.let {
                        if (it.length >= 3 || it.isEmpty()) it.ifEmpty { null } else null
                    },
                    isAdult = isAdult
                )
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: MediaAction) {
        when (action) {
            is MediaAction.SearchTextChanged -> {
                _state.update {
                    it.copy(listState = it.listState.copy(searchText = action.newText))
                }
            }

            is MediaAction.AdultCheckboxToggled -> {
                _state.update {
                    it.copy(listState = it.listState.copy(isAdultChecked = action.isChecked))
                }
            }

            is MediaAction.MediaClicked -> {
                loadMediaDetails(action.mediaId)
            }

            is MediaAction.SearchSubmitted -> {
                val currentState = _state.value.listState
                if (currentState.searchText.length >= 3 || currentState.searchText.isEmpty()) {
                    resetPaginationState()
                    loadMediaList(
                        page = 1,
                        perPage = currentState.perPage,
                        search = currentState.searchText.ifEmpty { null },
                        isAdult = currentState.isAdultChecked
                    )
                }
            }

            MediaAction.LoadMoreItems -> {
                val currentState = _state.value.listState
                if (currentState.hasNextPage && !currentState.isLoading) {
                    val currentSearch = currentState.searchText.let { if (it.length >= 3 || it.isEmpty()) it.ifEmpty { null } else null }
                    loadMediaList(
                        page = currentState.page,
                        perPage = currentState.perPage,
                        search = currentSearch,
                        isAdult = currentState.isAdultChecked
                    )
                }
            }

            is MediaAction.SetTrailerFullscreen -> {
                _state.update { currentState ->
                    currentState.copy(
                        detailState = currentState.detailState.copy(
                            isTrailerFullscreen = action.isFullscreen
                        )
                    )
                }
            }
        }
    }

    private fun loadMediaList(page: Int, perPage: Int, search: String?, isAdult: Boolean) {
        val listState = _state.value.listState
        val currentSearch = listState.searchText.let { if (it.length >= 3 || it.isEmpty()) it.ifEmpty { null } else null }
        if (page == 1 && listState.items.isNotEmpty() && !listState.isLoading) {
            val currentFilters = Pair(currentSearch, listState.isAdultChecked)
            val newFilters = Pair(search, isAdult)
            if (currentFilters == newFilters) {
                return
            }
        }

        _state.update { currentState ->
            currentState.copy(
                listState = currentState.listState.copy(isLoading = true)
            )
        }

        viewModelScope.launch(IO) {
            when (val result = getMediaListUseCase(page, perPage, search, isAdult)) {
                is Result.Success -> {
                    val currentPageInfo = result.data.pageInfo
                    _state.update { currentState ->
                        val newItems = if (page == 1) {
                            result.data.items
                        } else {
                            (currentState.listState.items + result.data.items)
                                .distinctBy { it.id }
                        }
                        currentState.copy(
                            listState = currentState.listState.copy(
                                items = newItems.toPersistentList(),
                                isLoading = false,
                                hasNextPage = currentPageInfo.hasNextPage,
                                page = if (currentPageInfo.hasNextPage) currentPageInfo.currentPage.plus(1) else currentState.listState.page,
                                searchText = search ?: "",
                                isAdultChecked = isAdult
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
                Result.Loading -> { /* Handled by isLoading state */ }
            }
        }
    }

    private fun resetPaginationState() {
        _state.update { currentState ->
            currentState.copy(
                listState = currentState.listState.copy(
                    page = 1,
                    hasNextPage = true,
                    items = persistentListOf(),
                    isLoading = false,
                    error = null
                )
            )
        }
    }

    private fun loadMediaDetails(mediaId: Int) {
        _state.update { currentState ->
            currentState.copy(detailState = currentState.detailState.copy(uiState = MediaDetailsUiState.Loading))
        }
        viewModelScope.launch(IO) {

            when (val result = getMediaDetailsUseCase(mediaId)) {
                is Result.Success -> {
                    _state.update { currentState ->
                        currentState.copy(
                            detailState = currentState.detailState.copy(
                                uiState = MediaDetailsUiState.Success(result.data)
                            )
                        )
                    }
                    EventManager.triggerEvent(AppEvent.NavigateToDetail(mediaId))
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