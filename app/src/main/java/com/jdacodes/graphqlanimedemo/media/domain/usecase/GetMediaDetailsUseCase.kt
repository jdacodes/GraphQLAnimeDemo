package com.jdacodes.graphqlanimedemo.media.domain.usecase

import com.jdacodes.graphqlanimedemo.media.domain.repository.MediaRepository

class GetMediaDetailsUseCase(private val repository: MediaRepository) {
    suspend operator fun invoke(mediaId: Int) = repository.getMediaDetails(mediaId)
}
