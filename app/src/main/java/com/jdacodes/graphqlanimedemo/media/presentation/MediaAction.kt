package com.jdacodes.graphqlanimedemo.media.presentation

sealed interface MediaAction {
    data class SearchTextChanged(val newText: String) : MediaAction
    data class MediaClicked(val mediaId: Int) : MediaAction
    data class SearchSubmitted(val newText: String) : MediaAction
    data object LoadMoreItems : MediaAction
    data class SetTrailerFullscreen(val isFullscreen: Boolean): MediaAction
    data class AdultCheckboxToggled(val isChecked: Boolean) : MediaAction
}