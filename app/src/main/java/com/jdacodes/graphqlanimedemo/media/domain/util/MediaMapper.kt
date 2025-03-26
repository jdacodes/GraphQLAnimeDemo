package com.jdacodes.graphqlanimedemo.media.domain.util

import com.jdacodes.graphqlanimedemo.MediaQuery
import com.jdacodes.graphqlanimedemo.media.domain.model.MediaListItem
import com.jdacodes.graphqlanimedemo.media.domain.model.Studio

fun MediaQuery.Medium.toMediaListItem(): MediaListItem {
    // Map studio edges to a list of Studio domain objects.
    val studiosList = this.studios?.edges?.mapNotNull { edge ->
        edge?.node?.let { Studio(isMain = edge.isMain, name = it.name) }
    } ?: emptyList()

    return MediaListItem(
        id = id,
        titleEnglish = title?.english,
        titleRomaji = title?.romaji,
        description = description, // Use or provide default if needed
        coverImageMedium = coverImage?.medium,
        coverImageLarge = coverImage?.large,
        averageScore = averageScore,
        studios = studiosList
    )
}
