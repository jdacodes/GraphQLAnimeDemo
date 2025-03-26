package com.jdacodes.graphqlanimedemo.media.domain.util

import com.jdacodes.graphqlanimedemo.MediaQuery
import com.jdacodes.graphqlanimedemo.media.domain.model.PageInfo

fun MediaQuery.PageInfo.toDomainPageInfo(): PageInfo {
    return PageInfo(
        hasNextPage = this.hasNextPage ?: false,
        currentPage = this.currentPage ?: 0,
        lastPage = this.lastPage ?: 0,
        perPage = this.perPage ?: 0
    )
}