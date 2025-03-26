package com.jdacodes.graphqlanimedemo.media.domain.model

data class MediaListResult(
    val items: List<MediaListItem>,
    val pageInfo: PageInfo
)

data class PageInfo(
    val hasNextPage: Boolean,
    val currentPage: Int,
    val lastPage: Int,
    val perPage: Int
)