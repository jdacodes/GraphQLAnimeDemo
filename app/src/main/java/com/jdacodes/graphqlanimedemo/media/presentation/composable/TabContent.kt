package com.jdacodes.graphqlanimedemo.media.presentation.composable

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.jdacodes.graphqlanimedemo.MediaDetailsQuery
import com.jdacodes.graphqlanimedemo.R
import com.jdacodes.graphqlanimedemo.type.CharacterRole
import com.jdacodes.graphqlanimedemo.type.MediaFormat
import com.jdacodes.graphqlanimedemo.type.MediaSource
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.launch

@Composable
fun TabContent(
    media: MediaDetailsQuery.Media,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState { 3 }

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Tabs(pagerState = pagerState)
        TabsContent(
            media = media,
            pagerState = pagerState
        )
    }
}

@Composable
fun Tabs(pagerState: PagerState) {
    val list = listOf(
        "Info" to R.drawable.ic_info_tab,
        "Characters" to R.drawable.ic_character_tab,
        "Staff" to R.drawable.ic_staff_tab,
    )

    val scope = rememberCoroutineScope()

    TabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        list.forEachIndexed { index, pair ->
            // on below line we are creating a tab.
            Tab(
                icon = {
                    Icon(
                        painter = painterResource(id = pair.second),  // Changed this line
                        contentDescription = null,
                        tint = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                },
                text = {
                    Text(
                        pair.first,
                        color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else
                            MaterialTheme.colorScheme.onSurface
                    )
                },
                selected = pagerState.currentPage == index,

                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    }
}


@Composable
fun TabsContent(
    pagerState: PagerState,
    media: MediaDetailsQuery.Media
) {
    HorizontalPager(state = pagerState) { page ->
        when (page) {
            0 -> InfoTabContent(media = media)
            1 -> CharactersTabContent(media = media)
            2 -> StaffTabContent(media = media)
        }
    }
}


@Composable
fun TabContentScreen(data: String) {
    val itemsList = (0..500).toList()
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(items = itemsList) { item ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                text = "$data $item",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun CharactersTabContent(media: MediaDetailsQuery.Media) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (!media.characters?.edges.isNullOrEmpty()) {
            items(media.characters?.edges ?: emptyList()) { character ->
                character?.node?.let { node ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = node.name?.full ?: "",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        supportingContent = {
                            character.role?.let { role ->
                                Text(
                                    text = role.toRoleString(),
                                    style = MaterialTheme.typography.bodyMedium,

                                    )

                            }
                        },
                        leadingContent = {
                            val placeholder = if (isSystemInDarkTheme()) {
                                painterResource(R.drawable.ic_image_placeholder_dark)

                            } else {
                                painterResource(R.drawable.ic_image_placeholder)
                            }

                            AsyncImage(
                                modifier = Modifier
                                    .size(100.dp, 150.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                model = node.image?.large ?: node.image?.medium,
                                contentScale = ContentScale.Crop,
                                placeholder = placeholder,
                                error = placeholder,
                                contentDescription = "Character image",

                                )

                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StaffTabContent(media: MediaDetailsQuery.Media) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (!media.staff?.nodes.isNullOrEmpty()) {
            items(media.staff?.nodes ?: emptyList()) { staff ->
                if (staff != null) {
                    ListItem(
                        headlineContent = {
                            staff.name?.let { name ->
                                Text(
                                    text = staff.name.full ?: "",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                        },
                        supportingContent = {
                            staff.primaryOccupations?.let { occupation ->
                                Text(
                                    text = occupation.joinToString(separator = ", ") { it.toString() },
                                    style = MaterialTheme.typography.bodyMedium,

                                    )

                            }
                        },
                        leadingContent = {
                            val placeholder = if (isSystemInDarkTheme()) {
                                painterResource(R.drawable.ic_image_placeholder_dark)

                            } else {
                                painterResource(R.drawable.ic_image_placeholder)
                            }

                            AsyncImage(
                                modifier = Modifier
                                    .size(100.dp, 150.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                model = staff.image?.large ?: staff.image?.medium,
                                contentScale = ContentScale.Crop,
                                placeholder = placeholder,
                                error = placeholder,
                                contentDescription = "Character image",

                                )

                        }
                    )
                }


            }
        }
    }
}


@Composable
fun InfoTabContent(media: MediaDetailsQuery.Media) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mean score: ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = media.meanScore?.let { "${it.toFloat() / 10f}/10" } ?: "",
                    style = MaterialTheme.typography.bodyMedium  // Example style
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total episodes: ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = media.episodes?.toString() ?: "Unknown",
                    style = MaterialTheme.typography.bodyMedium  // Example style
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Format: ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = media.format?.toFormatString() ?: "Unknown",
                    style = MaterialTheme.typography.bodyMedium  // Example style
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Source: ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = media.source?.toSourceString() ?: "Unknown",
                    style = MaterialTheme.typography.bodyMedium  // Example style
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Studio: ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (media.studios?.edges?.mapNotNull { it?.isMain }
                        ?.isNotEmpty() == true) {
                    Text(
                        text = media.studios.edges.firstOrNull()?.node?.name ?: "Unknown",
                        style = MaterialTheme.typography.bodySmall // Example style
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Author: ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                val fullName =
                    media.staff?.nodes?.firstOrNull { it?.primaryOccupations?.contains("Mangaka") == true }
                        ?.name?.full ?: "Unknown"
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.bodySmall // Example style
                )

            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Season: ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                val fullSeason =
                    if (media.season != null && media.seasonYear != null) {
                        "${media.season} ${media.seasonYear}"
                    } else {
                        "Unknown"
                    }
                Text(
                    text = fullSeason,
                    style = MaterialTheme.typography.bodySmall // Example style
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Start date: ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                val fullStartDate =
                    if (media.startDate != null && media.startDate.month != null && media.startDate.year != null) {
                        "${media.startDate.day.toString()} ${media.startDate.month.toStringMonth()} ${media.startDate.year}"
                    } else {
                        "Not yet aired"
                    }
                Text(
                    text = fullStartDate,
                    style = MaterialTheme.typography.bodySmall // Example style
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "End date: ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                val fullEndDate =
                    if (media.endDate != null && media.endDate.month != null && media.endDate.year != null) {
                        "${media.endDate.day.toString()} ${media.endDate.month.toStringMonth() ?: ""} ${media.endDate.year}"
                    } else {
                        "Ongoing"
                    }
                Text(
                    text = fullEndDate,
                    style = MaterialTheme.typography.bodySmall // Example style
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Popularity: ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = media.popularity.toString() ?: "",
                    style = MaterialTheme.typography.bodySmall // Example style
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Favourites: ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = media.favourites.toString() ?: "",
                    style = MaterialTheme.typography.bodySmall // Example style
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Name romaji: ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = media.title?.romaji ?: "",
                    style = MaterialTheme.typography.bodySmall // Example style
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Name : ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = media.title?.native ?: "",
                    style = MaterialTheme.typography.bodySmall // Example style
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                Text(
                    text = "Synopsis",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = HtmlCompat.fromHtml(
                        media.description ?: "",
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    ).toString(),
                    style = MaterialTheme.typography.bodyMedium  // Example style
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Synonyms : ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = media.synonyms?.joinToString(separator = ", ") ?: "",
                    style = MaterialTheme.typography.bodySmall // Example style
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                Text(
                    text = "Trailer",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (media.trailer?.id != null) {
                    MediaTrailer(
                        videoId = media.trailer.id,
                        lifeCycleOwner = LocalLifecycleOwner.current
                    )
                    Log.d(
                        "MediaTrailer",
                        "Trailer loaded for ${media.id} ${media.title}"
                    )
                } else {
                    Text(
                        text = "No trailer available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                Text(
                    text = "Genres",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                MediaGenres(media.genres)
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                MediaTags(media.tags)
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                Text(
                    text = "Recommended",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                MediaRecommendation(media.recommendations)
            }
        }
    }
}

@Composable
fun MediaTrailer(
    videoId: String,
    lifeCycleOwner: LifecycleOwner
) {
    val activity = LocalActivity.current
    var isFullscreen by remember { mutableStateOf(false) }
    var fullscreenView: View? by remember { mutableStateOf(null) }
    var exitFullscreenCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Handle fullscreen item
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp)),
        factory = { context ->
            YouTubePlayerView(context = context).apply {
                lifeCycleOwner.lifecycle.addObserver(this)

                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.loadVideo(videoId, 0f)
                    }

                })

                addFullscreenListener(object : FullscreenListener {
                    override fun onEnterFullscreen(
                        view: View,
                        exitFullscreen: () -> Unit
                    ) {
                        isFullscreen = true
                        fullscreenView = view
                        exitFullscreenCallback = exitFullscreen
                    }

                    override fun onExitFullscreen() {
                        isFullscreen = false
                        exitFullscreenCallback = null
                        fullscreenView = null
                    }


                })
            }
        }
    )

    // Display fullscreen view when requested
    val decorView = remember(activity) { activity?.window?.decorView as ViewGroup }

    DisposableEffect(isFullscreen, fullscreenView) {
        if (isFullscreen && fullscreenView != null) {
            decorView.addView(
                fullscreenView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        } else if (fullscreenView != null) {
            decorView.removeView(fullscreenView)
        }
        onDispose {
            fullscreenView?.let {
                decorView.removeView(it)
            }
        }
    }

    // Ensure back button handles fullscreen exit (optional, but recommended)
    BackHandler(enabled = isFullscreen) {
        exitFullscreenCallback?.invoke()
    }
}

@Composable
fun MediaGenres(genres: List<String?>?) {
    if (!genres.isNullOrEmpty()) {
        Column {
            genres.chunked(2).forEach { genrePair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    genrePair.forEach { genre ->
                        OutlinedButton(
                            onClick = { /* TODO: Handle button click */ },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = genre.orEmpty(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    if (genrePair.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

}

@Composable
fun MediaTags(tags: List<MediaDetailsQuery.Tag?>?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            if (tags != null) {
                repeat(tags.size) { index ->
                    AssistChip(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        onClick = { /* do something*/ },
                        label = { Text("${tags[index]?.name ?: ""}: ${tags[index]?.rank ?: ""}%") },

                        )
                }
            }
        }
    }
}

@Composable
fun MediaRecommendation(recommendations: MediaDetailsQuery.Recommendations?) {

    if (recommendations != null) {
        if (recommendations.nodes != null) {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                recommendations.nodes.forEach { node ->
                    if (node?.mediaRecommendation != null) {
                        Column(
                            modifier = Modifier
                                .width(100.dp)
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val placeholder = if (isSystemInDarkTheme()) {
                                painterResource(R.drawable.ic_image_placeholder_dark)

                            } else {
                                painterResource(R.drawable.ic_image_placeholder)
                            }

                            Box(
                                modifier = Modifier
                                    .size(100.dp, 150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = node.mediaRecommendation.coverImage?.extraLarge
                                        ?: node.mediaRecommendation.coverImage?.large ?: "",
                                    contentScale = ContentScale.Crop,
                                    placeholder = placeholder,
                                    error = placeholder,
                                    contentDescription = "Media image",
                                )

                                Text(
                                    text = node.mediaRecommendation.meanScore?.let { "${it.toFloat() / 10f}/10" }
                                        ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surface)
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp) // Adjust padding if needed
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = node.mediaRecommendation.title?.romaji
                                    ?: node.mediaRecommendation.title?.english ?: "Unknown",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 2,
                                modifier = Modifier.width(100.dp)
                            )
                            Text(
                                text = "Episodes: ${node.mediaRecommendation.episodes ?: "Unknown"}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(100.dp)
                            )
                        }
                    }
                }
            }
        }
    }


}


private fun MediaFormat.toFormatString(): String = when (this) {
    MediaFormat.TV -> "TV"
    MediaFormat.TV_SHORT -> "TV Short"
    MediaFormat.MOVIE -> "Movie"
    MediaFormat.SPECIAL -> "Special"
    MediaFormat.OVA -> "OVA"
    MediaFormat.ONA -> "ONA"
    MediaFormat.MUSIC -> "Music"
    MediaFormat.MANGA -> "Manga"
    MediaFormat.NOVEL -> "Novel"
    MediaFormat.ONE_SHOT -> "One Shot"
    else -> "Unknown"
}

private fun MediaSource.toSourceString(): String = when (this) {
    MediaSource.ORIGINAL -> "Original"
    MediaSource.MANGA -> "Manga"
    MediaSource.LIGHT_NOVEL -> "Light Novel"
    MediaSource.VISUAL_NOVEL -> "Visual Novel"
    MediaSource.VIDEO_GAME -> "Video Game"
    MediaSource.OTHER -> "Other"
    MediaSource.NOVEL -> "Novel"
    MediaSource.DOUJINSHI -> "Doujinshi"
    MediaSource.ANIME -> "Anime"
    MediaSource.WEB_NOVEL -> "Web Novel"
    MediaSource.LIVE_ACTION -> "Live Action"
    MediaSource.GAME -> "Game"
    MediaSource.COMIC -> "Comic"
    MediaSource.MULTIMEDIA_PROJECT -> "Multimedia Project"
    MediaSource.PICTURE_BOOK -> "Picture Book"
    MediaSource.UNKNOWN__ -> "Unknown"
}

private fun Int.toStringMonth(): String {
    return when (this) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> {
            " "
        }
    }
}

private fun CharacterRole.toRoleString(): String = when (this) {
    CharacterRole.MAIN -> "Main"
    CharacterRole.SUPPORTING -> "Supporting"
    CharacterRole.BACKGROUND -> "Background"
    else -> "Unknown"
}