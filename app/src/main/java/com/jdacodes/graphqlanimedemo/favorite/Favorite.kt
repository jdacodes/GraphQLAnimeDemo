package com.jdacodes.graphqlanimedemo.favorite

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jdacodes.graphqlanimedemo.ui.theme.GraphQLAnimeDemoTheme

@Composable
fun FavoriteRoot(
    viewModel: FavoriteViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    FavoriteScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun FavoriteScreen(
    state: FavoriteState,
    onAction: (FavoriteAction) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Favorites Screen",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Preview
@Composable
private fun Preview() {
    GraphQLAnimeDemoTheme {
        FavoriteScreen(
            state = FavoriteState(),
            onAction = {}
        )
    }
}