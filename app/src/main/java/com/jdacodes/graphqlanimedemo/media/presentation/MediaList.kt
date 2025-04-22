package com.jdacodes.graphqlanimedemo.media.presentation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jdacodes.graphqlanimedemo.R
import com.jdacodes.graphqlanimedemo.core.util.TestTags
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
                title = {
                    OutlinedTextField(
                        value = listState.searchText,
                        onValueChange = { onAction(MediaAction.SearchTextChanged(it)) },
                        placeholder = { Text("Search anime...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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
                                keyboardController?.hide()
                                onAction(MediaAction.SearchSubmitted(listState.searchText))
                            }
                        )
                    )
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
    ) { scaffoldPaddingValues -> 
        Column(
            modifier = Modifier
                .padding(scaffoldPaddingValues)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End 
            ) {
                Checkbox(
                    checked = listState.isAdultChecked, 
                    onCheckedChange = { isChecked ->
                        onAction(MediaAction.AdultCheckboxToggled(isChecked))
                    }
                )
                Spacer(modifier = Modifier.width(4.dp)) 
                Text(
                    text = "Adult",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            PaginatedLazyColumn(
                modifier = Modifier.weight(1f),
                items = listState.items,
                loadMoreItems = { onAction(MediaAction.LoadMoreItems) },
                listState = listStateLazy,
                isLoading = listState.isLoading,
                onAction = onAction
            )
        }
    }
}

@Composable
fun PaginatedLazyColumn(
    modifier: Modifier = Modifier,
    items: PersistentList<MediaListItem>,
    loadMoreItems: () -> Unit,
    listState: LazyListState,
    buffer: Int = 2,
    isLoading: Boolean,
    onAction: (MediaAction) -> Unit
) {
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex =
                listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= (totalItemsCount - buffer) && !isLoading
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { shouldLoadMore.value }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                loadMoreItems()
            }
    }

    LazyColumn(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth() 
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(TestTags.PaginatedList),
        state = listState
    ) {
        itemsIndexed(items, key = { _, item -> item.id }) { _, media ->
            MediaItem(
                media = media,
                onAction = onAction,
                modifier = Modifier.testTag("${TestTags.MediaListItem}_${media.id}")
            )
        }

        if (isLoading && items.isNotEmpty()) {
            item {
                LoadingItem()
            }
        }
        if (isLoading && items.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxSize()
                        .testTag(TestTags.LoadingIndicator),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun MediaItem(
    media: MediaListItem,
    onAction: (MediaAction) -> Unit,
    modifier: Modifier = Modifier  // Accept Modifier as parameter
) {
    ListItem(
        modifier = modifier.clickable {
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
private fun LoadingItem(
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth()
            .padding(16.dp)
            .testTag(TestTags.LoadingIndicator)
            // Add when accessibility is needed
            .semantics {
                contentDescription = "Loading indicator"
            }

    ) {
        CircularProgressIndicator()
    }
}


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MediaListDetailRoot(
    viewModel: MediaViewModel = hiltViewModel(),
    navigator: ThreePaneScaffoldNavigator<Int>
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    BackHandler(navigator.canNavigateBack()) {
        scope.launch {
            navigator.navigateBack()
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


