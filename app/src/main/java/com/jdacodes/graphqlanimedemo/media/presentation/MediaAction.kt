package com.jdacodes.graphqlanimedemo.media.presentation

sealed interface MediaAction {
    data class SearchTextChanged(val newText: String) : MediaAction
    data class MediaClicked(val mediaId: Int) : MediaAction
    data object ResetSearch : MediaAction
    data object LoadMoreItems : MediaAction

}