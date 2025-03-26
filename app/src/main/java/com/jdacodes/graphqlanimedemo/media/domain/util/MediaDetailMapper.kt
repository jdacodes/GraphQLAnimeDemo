package com.jdacodes.graphqlanimedemo.media.domain.util

import com.jdacodes.graphqlanimedemo.MediaDetailsQuery
import com.jdacodes.graphqlanimedemo.media.domain.model.CharacterItem
import com.jdacodes.graphqlanimedemo.media.domain.model.DateParts
import com.jdacodes.graphqlanimedemo.media.domain.model.MediaDetails
import com.jdacodes.graphqlanimedemo.media.domain.model.RecommendationItem
import com.jdacodes.graphqlanimedemo.media.domain.model.StaffItem
import com.jdacodes.graphqlanimedemo.media.domain.model.Studio
import com.jdacodes.graphqlanimedemo.media.domain.model.TagItem
import com.jdacodes.graphqlanimedemo.media.domain.model.TrailerItem
import com.jdacodes.graphqlanimedemo.media.domain.model.TrendItem

fun MediaDetailsQuery.Media.toMediaDetails(): MediaDetails {
    // Map studios
    val studiosList = this.studios?.edges?.mapNotNull { edge ->
        edge?.node?.let { Studio(isMain = edge.isMain, name = it.name) }
    } ?: emptyList()

    // Map trends nodes
    val trendsList = this.trends?.nodes?.map { node ->
        TrendItem(episode = node?.episode)
    } ?: emptyList()

    // Map start and end dates
    val startDateParts = this.startDate?.let { DateParts(it.day, it.month, it.year) }
    val endDateParts = this.endDate?.let { DateParts(it.day, it.month, it.year) }

    // Map trailer
    val trailerItem =
        this.trailer?.let { TrailerItem(id = it.id, site = it.site, thumbnail = it.thumbnail) }

    // Map tags list
    val tagItems = this.tags?.mapNotNull { tag ->
        tag?.let { TagItem(name = it.name, rank = it.rank) }
    } ?: emptyList()

    // Map characters
    val characterItems = this.characters?.edges?.mapNotNull { edge ->
        edge?.node?.let { node ->
            // Extract voice actor names (if any)
            val voiceActorNames =
                edge.voiceActorRoles?.mapNotNull { it?.voiceActor?.name?.full } ?: emptyList()
            CharacterItem(
                id = node.id,
                name = node.name?.full,
                imageLarge = node.image?.large,
                imageMedium = node.image?.medium,
                role = edge.role?.name, // Assuming role is an enum; you might convert it to string
                voiceActorNames = voiceActorNames
            )
        }
    } ?: emptyList()

    // Map staff
    val staffItems = this.staff?.nodes?.mapNotNull { node ->
        node?.let {
            StaffItem(
                id = it.id,
                name = it.name?.full,
                primaryOccupations = it.primaryOccupations,
                imageLarge = it.image?.large,
                imageMedium = it.image?.medium
            )
        }
    } ?: emptyList()

    // Map recommendations
    val recommendationItems = this.recommendations?.nodes?.mapNotNull { node ->
        node?.mediaRecommendation?.let { rec ->
            RecommendationItem(
                id = node.id,
                coverImageExtraLarge = rec.coverImage?.extraLarge,
                coverImageLarge = rec.coverImage?.large,
                meanScore = rec.meanScore,
                episodes = rec.episodes,
                titleEnglish = rec.title?.english,
                titleNative = rec.title?.native,
                titleRomaji = rec.title?.romaji
            )
        }
    } ?: emptyList()

    return MediaDetails(
        bannerImage = bannerImage,
        averageScore = averageScore,
        titleEnglish = title?.english,
        titleNative = title?.native,
        titleRomaji = title?.romaji,
        description = description,
        id = id,
        studios = studiosList,
        coverImageLarge = coverImage?.large,
        meanScore = meanScore,
        status = status?.name, // Assuming status is an enum; otherwise, adjust accordingly.
        episodes = episodes,
        trends = trendsList,
        format = format?.name,
        source = source?.name,
        season = season?.name,
        seasonYear = seasonYear,
        startDate = startDateParts,
        endDate = endDateParts,
        popularity = popularity,
        favourites = favourites,
        synonyms = synonyms ?: emptyList(),
        trailer = trailerItem,
        genres = genres ?: emptyList(),
        tags = tagItems,
        characters = characterItems,
        staff = staffItems,
        recommendations = recommendationItems
    )
}
