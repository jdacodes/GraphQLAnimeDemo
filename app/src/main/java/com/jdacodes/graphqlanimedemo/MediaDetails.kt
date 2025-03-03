@file:OptIn(ExperimentalMaterial3Api::class)

package com.jdacodes.graphqlanimedemo

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.exception.ApolloNetworkException

private sealed interface MediaDetailsState {
    object Loading : MediaDetailsState
    data class Error(val message: String) : MediaDetailsState
    data class Success(val data: MediaDetailsQuery.Data) : MediaDetailsState
}

@Composable
fun MediaDetails(
    id: Int,
    onBack: () -> Unit
) {
    Log.d("MEDIA_DETAILS", "Entered with ID: $id")  // BEFORE null check
    if (id == 0) {
        ErrorMessage("Invalid media ID")
        return
    }

    var state by remember { mutableStateOf<MediaDetailsState>(MediaDetailsState.Loading) }
    LaunchedEffect(id) {  // Track ID changes
        Log.d("MediaDetailsDebug", "Received ID: $id (type: ${id::class.java.simpleName})")
        Log.d("QueryDebug", "Executing query with mediaId: $id")
        val response = apolloClient.query(
            MediaDetailsQuery(mediaId = Optional.present(id))
        ).execute()
        Log.d("QueryDebug", "Received response: ${response.data}")
        Log.d("MEDIA_QUERY", "Querying with ID: $id")
        state = when {
            response.errors.orEmpty().isNotEmpty() -> {
                MediaDetailsState.Error(response.errors!!.first().message)
            }

            response.exception is ApolloNetworkException -> {
                MediaDetailsState.Error("Please check your network connectivity.")
            }

            response.data != null -> {
                MediaDetailsState.Success(response.data!!)
            }

            else -> {
                MediaDetailsState.Error("Oh no... An error happened.")
            }
        }
        Log.d("Fetch error", response.exception.toString())
        Log.d("Request error", response.errors.toString() + response.data.toString())
        Log.d("Field error", response.errors.toString() + response.data.toString())
    }
    when (val s = state) {
        MediaDetailsState.Loading -> Loading()
        is MediaDetailsState.Error -> ErrorMessage(s.message)
        is MediaDetailsState.Success -> MediaDetailsScreen(s.data, onBack)
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
private fun MediaDetails(
    data: MediaDetailsQuery.Data,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        AsyncImage(
            modifier = Modifier.size(100.dp, 150.dp),
            model = data.Media?.coverImage?.large,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_image_placeholder),
            error = painterResource(R.drawable.ic_image_placeholder),
            contentDescription = "Media image"
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (data.Media?.title != null) {
                Text(
                    text = data.Media.title.english ?: data.Media.title.romaji ?: "",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium  // Example style
                )
                Text(
                    text = data.Media.title.romaji ?: data.Media.title.native ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )

            }


        }
    }

}


@Composable
private fun MediaDetailsScreen(
    data: MediaDetailsQuery.Data,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = data.Media?.title?.english ?: data.Media?.title?.romaji ?: ""
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
                CollapsedHeaderContent(data = data)
            },
            bodyContent = {
                TabContent(data = data)
            }
        )

    }


}

