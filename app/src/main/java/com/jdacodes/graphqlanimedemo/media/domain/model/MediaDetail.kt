package com.jdacodes.graphqlanimedemo.media.domain.model

data class MediaDetails(
    val bannerImage: String?,
    val averageScore: Int?,
    val titleEnglish: String?,
    val titleNative: String?,
    val titleRomaji: String?,
    val description: String?,
    val id: Int,
    val studios: List<Studio>,
    val coverImageLarge: String?,
    val meanScore: Int?,
    val status: String?,
    val episodes: Int?,
    val trends: List<TrendItem>,
    val format: String?,
    val source: String?,
    val season: String?,
    val seasonYear: Int?,
    val startDate: DateParts?,
    val endDate: DateParts?,
    val popularity: Int?,
    val favourites: Int?,
    val synonyms: List<String?>,
    val trailer: TrailerItem?,
    val genres: List<String?>,
    val tags: List<TagItem>,
    val characters: List<CharacterItem>,
    val staff: List<StaffItem>,
    val recommendations: List<RecommendationItem>
)

data class TrendItem(val episode: Int?)

data class DateParts(val day: Int?, val month: Int?, val year: Int?)

data class TrailerItem(val id: String?, val site: String?, val thumbnail: String?)

data class TagItem(val name: String, val rank: Int?)

data class CharacterItem(
    val id: Int,
    val name: String?,
    val imageLarge: String?,
    val imageMedium: String?,
    val role: String?,
    val voiceActorNames: List<String>
)

data class StaffItem(
    val id: Int,
    val name: String?,
    val primaryOccupations: List<String?>?,
    val imageLarge: String?,
    val imageMedium: String?
)

data class RecommendationItem(
    val id: Int,
    val coverImageExtraLarge: String?,
    val coverImageLarge: String?,
    val meanScore: Int?,
    val episodes: Int?,
    val titleEnglish: String?,
    val titleNative: String?,
    val titleRomaji: String?
)
