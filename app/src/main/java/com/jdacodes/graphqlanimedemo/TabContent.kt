package com.jdacodes.graphqlanimedemo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import com.jdacodes.graphqlanimedemo.type.MediaFormat
import com.jdacodes.graphqlanimedemo.type.MediaSource
import kotlinx.coroutines.launch

@Composable
fun TabContent(
    data: MediaDetailsQuery.Data,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState { 3 }

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Tabs(pagerState = pagerState)
        TabsContent(
            data = data,
            pagerState = pagerState
        )
    }
}

@Composable
fun Tabs(pagerState: PagerState) {
    val list = listOf(
        "Home" to Icons.Default.Home,
        "Shopping" to Icons.Default.ShoppingCart,
        "Settings" to Icons.Default.Settings
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
                        imageVector = pair.second, contentDescription = null,
                        tint = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else
                            MaterialTheme.colorScheme.onSurface
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
    data: MediaDetailsQuery.Data
) {
    HorizontalPager(state = pagerState) { page ->
        when (page) {
            0 -> InfoTabContent(data = data)
            1 -> TabContentScreen(data = "Welcome to Shopping Screen")
            2 -> TabContentScreen(data = "Welcome to Settings Screen")
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
fun InfoTabContent(data: MediaDetailsQuery.Data) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (data.Media != null) {
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
                        text = data.Media.meanScore?.let { "${it.toFloat() / 10f}/10" } ?: "",
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
                        text = data.Media.episodes?.toString() ?: "Unknown",
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
                        text = data.Media.format?.toFormatString() ?: "Unknown",
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
                        text = data.Media.source?.toSourceString() ?: "Unknown",
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
                    if (data.Media.studios?.edges?.mapNotNull { it?.isMain }
                            ?.isNotEmpty() == true) {
                        Text(
                            text = data.Media.studios.edges.firstOrNull()?.node?.name ?: "Unknown",
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
                    val fullName = data.Media.staff?.nodes
                        ?.filter { it?.primaryOccupations?.contains("Mangaka") == true }
                        ?.firstOrNull()
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
                        "${data.Media.season ?: ""} ${data.Media.seasonYear.toString() ?: ""}"
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
                        if (data.Media.startDate != null) {
                            "${data.Media.startDate.day.toString()} ${data.Media.startDate.month?.toStringMonth() ?: ""} ${data.Media.startDate.year.toString()}"
                        } else {
                            ""
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
                        if (data.Media.endDate != null) {
                            "${data.Media.endDate.day.toString() ?: ""} ${data.Media.endDate.month?.toStringMonth() ?: ""} ${data.Media.endDate.year.toString() ?: ""}"
                        } else {
                            ""
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
                        text = data.Media.popularity.toString() ?: "",
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
                        text = data.Media.favourites.toString() ?: "",
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
                        text = data.Media.title?.romaji ?: "",
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
                        text = data.Media.title?.native ?: "",
                        style = MaterialTheme.typography.bodySmall // Example style
                    )
                }
            }
            item {
                Column {
                    Text(
                        text = "Synopsis",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = HtmlCompat.fromHtml(
                            data.Media.description ?: "",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        ).toString(),
                        style = MaterialTheme.typography.bodyMedium  // Example style
                    )
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