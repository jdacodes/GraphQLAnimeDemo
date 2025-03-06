package com.jdacodes.graphqlanimedemo

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.apollographql.apollo.api.Optional
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch


@Composable
fun MediaList(onMediaClick: (id: Int) -> Unit) {
    var page by remember { mutableIntStateOf(1) }
    val perPage by remember { mutableIntStateOf(10) }
    var hasNextPage by remember { mutableStateOf(true) }
    var mediaList by remember { mutableStateOf(emptyList<MediaQuery.Medium>()) }
    // State to track the scroll position
    val listState = rememberLazyListState()
    // Coroutine scope for handling background operations like loading data
    val coroutineScope = rememberCoroutineScope()
    // State to track if more items are being loaded
    var isLoading by remember { mutableStateOf(false) }

    // Function to simulate loading more items (with a delay)
    fun loadMoreItems() {
        coroutineScope.launch {
            if (!isLoading && hasNextPage) {
                isLoading = true
                delay(1000)
                val response = apolloClient.query(
                    MediaQuery(Optional.present(page), Optional.present(perPage))
                ).execute()

                val newMediaItems = response.data?.Page?.media?.filterNotNull().orEmpty()
                val currentPageInfo = response.data?.Page?.pageInfo

                // Append new items, avoiding duplicates
                mediaList = (mediaList + newMediaItems).distinctBy { it.id }

                // Update paging info
                hasNextPage = currentPageInfo?.hasNextPage ?: false
                page = currentPageInfo?.currentPage?.plus(1) ?: page

                Log.d("MediaList", "Fetched page: $page, hasNextPage: $hasNextPage")
                isLoading = false
            }
        }
    }

    PaginatedLazyColumn(
        items = mediaList.toPersistentList(),
        loadMoreItems = ::loadMoreItems,
        onClick = onMediaClick,
        listState = listState,
        isLoading = isLoading
    )

}

@Composable
fun PaginatedLazyColumn(
    items: PersistentList<MediaQuery.Medium>,  // Using PersistentList for efficient state management
    loadMoreItems: () -> Unit,  // Function to load more items
    listState: LazyListState,  // Track the scroll state of the LazyColumn
    buffer: Int = 2,  // Buffer to load more items when we get near the end
    isLoading: Boolean,  // Track if items are being loaded
    modifier: Modifier = Modifier,
    onClick: (id: Int) -> Unit
) {
    // Derived state to determine when to load more items
    val shouldLoadMore = remember {
        derivedStateOf {
            // Get the total number of items in the list
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            // Get the index of the last visible item
            val lastVisibleItemIndex =
                listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            // Check if we have scrolled near the end of the list and more items should be loaded
            lastVisibleItemIndex >= (totalItemsCount - buffer) && !isLoading
        }
    }

// Launch a coroutine to load more items when shouldLoadMore becomes true
    LaunchedEffect(listState) {
        snapshotFlow { shouldLoadMore.value }
            .distinctUntilChanged()
            .filter { it }  // Ensure that we load more items only when needed
            .collect {
                loadMoreItems()
            }
    }
    // LazyColumn to display the list of items
    LazyColumn(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(16.dp),  // Add padding for better visual spacing
        state = listState  // Pass the scroll state
    ) {
        // Render each item in the list using a unique key
        itemsIndexed(items, key = { _, item -> item.id }) { _, media ->
            MediaItem(
                media = media,
                onClick = { onClick(media.id) }  // Simplified click handler
            )
        }

//            // Check if we've reached the end of the list
//            if (index == items.lastIndex && !isLoading) {
//                loadMoreItems()
//            }

        // Show a loading indicator at the bottom when items are being loaded
        if (isLoading) {
            item {
                LoadingItem()
            }
        }
    }
}

@Composable
fun MediaItem(
    media: MediaQuery.Medium,
    onClick: (id: Int) -> Unit
) {
    ListItem(
        modifier = Modifier.clickable {
            Log.d(
                "MEDIA_CLICK",
                "ID: ${media.id}, Title: ${media.title?.english ?: media.title?.romaji}"
            )
            onClick(media.id)
        },
        headlineContent = {
            if (media.title != null) {
                Column {
                    // Style the first Text with Material 3 typography settings
                    Text(
                        text = media.title.english ?: media.title.romaji ?: "",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium  // Example style
                    )
                    if (media.title.english != media.title.romaji) {
                        Text(
                            text = media.title.romaji ?: "",
                            style = MaterialTheme.typography.bodyMedium  // Example style
                        )
                    }
                }
            }

        },
        supportingContent = {

            Column {
                Spacer(modifier = Modifier.height(8.dp))

                if (media.studios?.edges?.mapNotNull { it?.isMain }?.isNotEmpty() == true) {
                    Text(
                        text = "Studio: ${media.studios.edges.firstOrNull()?.node?.name}",
                        style = MaterialTheme.typography.bodySmall // Example style
                    )
                }
            }
        },
        leadingContent = {
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
                    modifier = Modifier.size(100.dp, 150.dp),
                    model = media.coverImage?.large,
                    contentScale = ContentScale.Crop,
                    placeholder = placeholder,
                    error = placeholder,
                    contentDescription = "Media image",

                    )
                Text(
                    text = media.averageScore?.let { "${it.toFloat() / 10f}/10" } ?: "",
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .align(Alignment.BottomEnd)
                        .padding(4.dp), // Adjust padding if needed
                    style = MaterialTheme.typography.bodySmall // Example style
                )
            }
        }
    )

}

@Composable
private fun LoadingItem() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        CircularProgressIndicator()
    }
}


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MediaListDetailPaneScaffold() {
    val navigator = rememberListDetailPaneScaffoldNavigator<Int>()
    BackHandler(navigator.canNavigateBack()) {
        navigator.navigateBack()
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                MediaList(
                    onMediaClick = { id ->
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                    }
                )
            }

        },
        detailPane = {
            AnimatedPane {
                navigator.currentDestination?.content?.let {
                    MediaDetails(id = it, onBack = {
                        navigator.navigateBack()
                    })
                }
            }

        },
    )
}


