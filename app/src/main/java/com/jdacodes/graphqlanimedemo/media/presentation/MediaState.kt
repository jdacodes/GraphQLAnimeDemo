package com.jdacodes.graphqlanimedemo.media.presentation

import com.jdacodes.graphqlanimedemo.media.domain.model.MediaDetails
import com.jdacodes.graphqlanimedemo.media.domain.model.MediaListItem
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class MediaListState(
    val items: PersistentList<MediaListItem> = persistentListOf(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val searchText: String = "",
    val hasNextPage: Boolean = true,
    val page: Int = 1,
    val perPage: Int = 10
)

data class MediaDetailState(
    var uiState: MediaDetailsUiState = MediaDetailsUiState.Loading,
)

sealed interface MediaDetailsUiState {
    object Loading : MediaDetailsUiState
    data class Error(val message: String) : MediaDetailsUiState
    data class Success(val media: MediaDetails) : MediaDetailsUiState
}

data class MediaState(
    val listState: MediaListState = MediaListState(),
    val detailState: MediaDetailState = MediaDetailState()
)