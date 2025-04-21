package com.jdacodes.graphqlanimedemo.media.domain.repository

import com.jdacodes.graphqlanimedemo.core.util.Result
import com.jdacodes.graphqlanimedemo.media.domain.model.MediaDetails
import com.jdacodes.graphqlanimedemo.media.domain.model.MediaListResult

interface MediaRepository {
    suspend fun getMediaList(page: Int, perPage: Int, search: String?, isAdult: Boolean): Result<MediaListResult>
    suspend fun getMediaDetails(mediaId: Int): Result<MediaDetails>
}
