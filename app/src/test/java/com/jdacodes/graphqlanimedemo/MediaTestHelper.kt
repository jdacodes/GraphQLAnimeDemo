package com.jdacodes.graphqlanimedemo

import com.jdacodes.graphqlanimedemo.media.domain.model.MediaDetails
import com.jdacodes.graphqlanimedemo.media.domain.model.MediaListItem
import com.jdacodes.graphqlanimedemo.media.domain.model.Studio

// Helper methods to create test data
fun createTestMediaItems(): List<MediaListItem> {
    return listOf(
        MediaListItem(
            id = 1,
            titleEnglish = "Test Anime 1",
            titleRomaji = "Tesuto Anime Ichi",
            description = "Test description 1",
            coverImageMedium = "https://example.com/medium1.jpg",
            coverImageLarge = "https://example.com/large1.jpg",
            averageScore = 85,
            studios = listOf(Studio(isMain = true, name = "Test Studio 1"))
        ),
        MediaListItem(
            id = 2,
            titleEnglish = "Test Anime 2",
            titleRomaji = "Tesuto Anime Ni",
            description = "Test description 2",
            coverImageMedium = "https://example.com/medium2.jpg",
            coverImageLarge = "https://example.com/large2.jpg",
            averageScore = 90,
            studios = listOf(Studio(isMain = true, name = "Test Studio 2"))
        ),
        MediaListItem(
            id = 3,
            titleEnglish = "Test Anime 3",
            titleRomaji = "Tesuto Anime San",
            description = "Test description 3",
            coverImageMedium = "https://example.com/medium3.jpg",
            coverImageLarge = "https://example.com/large3.jpg",
            averageScore = 75,
            studios = listOf(Studio(isMain = true, name = "Test Studio 3"))
        ),
        MediaListItem(
            id = 4,
            titleEnglish = "Test Anime 4",
            titleRomaji = "Tesuto Anime Yon",
            description = "Test description 4",
            coverImageMedium = "https://example.com/medium4.jpg",
            coverImageLarge = "https://example.com/large4.jpg",
            averageScore = 95,
            studios = listOf(Studio(isMain = true, name = "Test Studio 4"))
        )
    )
}

fun createTestMediaDetails(id: Int): MediaDetails {
    return MediaDetails(
        id = id,
        bannerImage = "https://example.com/banner.jpg",
        averageScore = 85,
        titleEnglish = "Test Anime Details",
        titleNative = "テストアニメ",
        titleRomaji = "Tesuto Anime",
        description = "Detailed description for testing",
        studios = listOf(Studio(isMain = true, name = "Test Studio")),
        coverImageLarge = "https://example.com/large.jpg",
        meanScore = 84,
        status = "FINISHED",
        episodes = 24,
        trends = emptyList(),
        format = "TV",
        source = "MANGA",
        season = "WINTER",
        seasonYear = 2023,
        startDate = null,
        endDate = null,
        popularity = 10000,
        favourites = 5000,
        synonyms = listOf("Test Anime", "TA"),
        trailer = null,
        genres = listOf("Action", "Fantasy"),
        tags = emptyList(),
        characters = emptyList(),
        staff = emptyList(),
        recommendations = emptyList()
    )
}