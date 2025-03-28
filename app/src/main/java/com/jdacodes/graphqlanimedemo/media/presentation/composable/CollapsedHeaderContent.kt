package com.jdacodes.graphqlanimedemo.media.presentation.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jdacodes.graphqlanimedemo.R
import com.jdacodes.graphqlanimedemo.media.domain.model.MediaDetails

@Composable
internal fun CollapsedHeaderContent(
    media: MediaDetails,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            model = media.bannerImage,
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(
                Color.Black.copy(alpha = 0.6f),
                BlendMode.SrcAtop
            ),
            placeholder = painterResource(R.drawable.ic_image_placeholder),
            error = painterResource(R.drawable.ic_image_placeholder),
            contentDescription = "Media image"
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(
                    Alignment.Center
                )
        ) {
            Spacer(
                modifier = Modifier.height(16.dp)
            )
            AsyncImage(
                modifier = Modifier
                    .size(100.dp, 150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .align(Alignment.CenterHorizontally),
                model = media.coverImageLarge,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_image_placeholder),
                error = painterResource(R.drawable.ic_image_placeholder),
                contentDescription = "Media image"
            )
            Spacer(
                modifier = Modifier.height(8.dp)
            )
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = media.titleEnglish ?: media.titleRomaji ?: "",
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                softWrap = true,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(
                modifier = Modifier.height(4.dp)
            )
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = media.titleRomaji ?: media.titleNative ?: "",
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                softWrap = true,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )


        }
    }

}

@Preview(showBackground = true, device = Devices.PIXEL)
@Composable
fun HeaderContentPreview() {
    CollapsedHeaderContent(
        media =
            MediaDetails(
                bannerImage = null,
                averageScore = null,
                titleEnglish = null,
                titleNative = null,
                titleRomaji = null,
                description = null,
                id = 8525,
                studios = listOf(),
                coverImageLarge = null,
                meanScore = null,
                status = null,
                episodes = null,
                trends = listOf(),
                format = null,
                source = null,
                season = null,
                seasonYear = null,
                startDate = null,
                endDate = null,
                popularity = null,
                favourites = null,
                synonyms = listOf(),
                trailer = null,
                genres = listOf(),
                tags = listOf(),
                characters = listOf(),
                staff = listOf(),
                recommendations = listOf()

            ),
        modifier = Modifier.fillMaxWidth()
    )
}