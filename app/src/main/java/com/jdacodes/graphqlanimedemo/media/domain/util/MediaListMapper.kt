package com.jdacodes.graphqlanimedemo.media.domain.util

import com.jdacodes.graphqlanimedemo.MediaQuery
import com.jdacodes.graphqlanimedemo.media.domain.model.MediaListResult
import com.jdacodes.graphqlanimedemo.media.domain.model.PageInfo

fun MediaQuery.Page.toMediaListResult(): MediaListResult {
    val items = this.media?.filterNotNull()?.map { it.toMediaListItem() } ?: emptyList()
    val pageInfoDomain = this.pageInfo?.toDomainPageInfo() ?: PageInfo(false, 0, 0, 0)
    return MediaListResult(
        items = items,
        pageInfo = pageInfoDomain
    )
}