@file:OptIn(ExperimentalMaterial3Api::class)

package com.jdacodes.graphqlanimedemo.media.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jdacodes.graphqlanimedemo.media.presentation.composable.CollapsedHeaderContent
import com.jdacodes.graphqlanimedemo.media.presentation.composable.CollapsingLayout
import com.jdacodes.graphqlanimedemo.MediaDetailsQuery
import com.jdacodes.graphqlanimedemo.media.presentation.composable.TabContent

@Composable
fun MediaDetails(
    id: Int,
    onBack: () -> Unit,
    detailState: MediaDetailState,
    onAction: (MediaAction) -> Unit,
) {
    Log.d("MEDIA_DETAILS", "Entered with ID: $id")  // BEFORE null check
    if (id == 0) {
        ErrorMessage("Invalid media ID")
        return
    }

    LaunchedEffect(id) {  // Track ID changes
        Log.d("MediaDetailsDebug", "Received ID: $id (type: ${id::class.java.simpleName})")
        Log.d("QueryDebug", "Executing query with mediaId: $id")
        Log.d("MEDIA_QUERY", "Querying with ID: $id")
    }

    when (val s = detailState.uiState) {
        MediaDetailsUiState.Loading -> Loading()
        is MediaDetailsUiState.Error -> ErrorMessage(s.message)
        is MediaDetailsUiState.Success -> MediaDetailsScreen(s.media, onBack)
    }
}

@Composable
private fun ErrorMessage(text: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text)
    }
}

@Composable
private fun Loading() {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MediaDetailsScreen(
    media: MediaDetailsQuery.Media,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = media.title?.english ?: media.title?.romaji ?: ""
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
                // Will fill in the details of toolbar
            )
        },
        bottomBar = { }
    ) { paddingValues ->
        CollapsingLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
            collapsingTop = {
                CollapsedHeaderContent(media = media)
            },
            bodyContent = {
                TabContent(media = media)
            }
        )

    }


}

