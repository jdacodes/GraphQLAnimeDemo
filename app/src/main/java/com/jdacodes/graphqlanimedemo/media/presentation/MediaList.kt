package com.jdacodes.graphqlanimedemo.media.presentation

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jdacodes.graphqlanimedemo.R
import com.jdacodes.graphqlanimedemo.media.domain.model.MediaListItem
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaList(
    listState: MediaListState,
    onAction: (MediaAction) -> Unit,
) {

    // State to track the scroll position
    val listStateLazy = rememberLazyListState()

    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(horizontal = 8.dp),
                title = {
                    OutlinedTextField(
                        value = listState.searchText,
                        onValueChange = { onAction(MediaAction.SearchTextChanged(it)) },
                        placeholder = { Text("Search anime...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search icon"
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                // Handle search here
                                keyboardController?.hide()
                                onAction(MediaAction.SearchSubmitted(listState.searchText))
                            }
                        )
                    )
                }
            )
        },
    ) { paddingValues ->
        PaginatedLazyColumn(
            modifier = Modifier.padding(paddingValues),
            items = listState.items,
            loadMoreItems = { onAction(MediaAction.LoadMoreItems) },
            listState = listStateLazy,
            isLoading = listState.isLoading,
            onAction = onAction
        )
    }
}


@Composable
fun PaginatedLazyColumn(
    modifier: Modifier = Modifier,
    items: PersistentList<MediaListItem>,  // Using PersistentList for efficient state management
    loadMoreItems: () -> Unit,  // Function to load more items
    listState: LazyListState,  // Track the scroll state of the LazyColumn
    buffer: Int = 2,  // Buffer to load more items when we get near the end
    isLoading: Boolean, // Track if items are being loaded
    onAction: (MediaAction) -> Unit

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
                onAction = onAction  // Simplified click handler
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
    media: MediaListItem,
    onAction: (MediaAction) -> Unit

) {
    ListItem(
        modifier = Modifier.clickable {
            Log.d(
                "MEDIA_CLICK",
                "ID: ${media.id}, Title: ${media.titleEnglish ?: media.titleRomaji}"
            )
            onAction(MediaAction.MediaClicked(media.id))
        },
        headlineContent = {
                Column {
                    // Style the first Text with Material 3 typography settings
                    Text(
                        text = media.titleEnglish ?: media.titleRomaji ?: "",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium  // Example style
                    )
                    if (media.titleEnglish != media.titleRomaji) {
                        Text(
                            text = media.titleRomaji ?: "",
                            style = MaterialTheme.typography.bodyMedium  // Example style
                        )
                    }
                }

        },
        supportingContent = {

            Column {
                Spacer(modifier = Modifier.height(8.dp))

                if (media.studios.map { it.isMain }.isNotEmpty()) {
                    Text(
                        text = "Studio: ${media.studios.firstOrNull()?.name}",
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
                    model = media.coverImageLarge,
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
fun MediaListDetailRoot(viewModel: MediaViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navigator = rememberListDetailPaneScaffoldNavigator<Int>()
    val scope = rememberCoroutineScope()

    BackHandler(navigator.canNavigateBack()) {
        scope.launch {
            navigator.navigateBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationChannel.collect { mediaId ->
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, mediaId)
        }
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                MediaList(
                    listState = state.listState,
                    onAction = viewModel::onAction
                )
            }

        },
        detailPane = {
            AnimatedPane {
                navigator.currentDestination?.contentKey?.let {
                    MediaDetails(
                        id = it,
                        detailState = state.detailState,
                        onAction = viewModel::onAction,
                        onBack = {
                            scope.launch {
                                navigator.navigateBack()
                            }
                        })
                }
            }

        },
    )
}


