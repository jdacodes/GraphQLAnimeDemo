package com.jdacodes.graphqlanimedemo.media.domain.usecase

import com.jdacodes.graphqlanimedemo.media.domain.repository.MediaRepository

class GetMediaListUseCase(private val repository: MediaRepository) {
    suspend operator fun invoke(page: Int, perPage: Int, search: String?, isAdult: Boolean) =
        repository.getMediaList(page, perPage, search, isAdult)
}