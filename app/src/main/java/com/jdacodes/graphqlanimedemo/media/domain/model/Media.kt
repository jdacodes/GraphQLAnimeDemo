package com.jdacodes.graphqlanimedemo.media.domain.model

data class MediaListItem(
    val id: Int,
    val titleEnglish: String?,
    val titleRomaji: String?,
    val description: String?,
    val coverImageMedium: String?,
    val coverImageLarge: String?,
    val averageScore: Int?,
    val studios: List<Studio>
)

data class Studio(
    val isMain: Boolean,
    val name: String
)


