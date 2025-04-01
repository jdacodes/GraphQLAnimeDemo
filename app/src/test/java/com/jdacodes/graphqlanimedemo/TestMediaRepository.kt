package com.jdacodes.graphqlanimedemo

import com.jdacodes.graphqlanimedemo.core.util.Result
import com.jdacodes.graphqlanimedemo.media.domain.model.MediaDetails
import com.jdacodes.graphqlanimedemo.media.domain.model.MediaListItem
import com.jdacodes.graphqlanimedemo.media.domain.model.MediaListResult
import com.jdacodes.graphqlanimedemo.media.domain.model.PageInfo
import com.jdacodes.graphqlanimedemo.media.domain.repository.MediaRepository

class TestMediaRepository : MediaRepository {
    private var mediaListResult: Result<MediaListResult> = Result.Success(
        MediaListResult(
            emptyList(),
            PageInfo(
                hasNextPage = false,
                currentPage = 1,
                lastPage = 1,
                perPage = 10
            )
        )
    )

    private var mediaDetailsResult: Result<MediaDetails> =
        Result.Error(Exception("No media details set"))


    // Default state methods
    fun resetToDefaults() {
        mediaListResult = Result.Success(
            MediaListResult(
                emptyList(), PageInfo(
                    hasNextPage = false,
                    currentPage = 1,
                    lastPage = 1,
                    perPage = 10
                )
            )
        )
        mediaDetailsResult = Result.Error(Exception("No media details set"))
    }

    fun setMediaListResult(result: Result<MediaListResult>) {
        mediaListResult = result
    }

    fun setMediaDetailsResult(result: Result<MediaDetails>) {
        mediaDetailsResult = result
    }

    // Mock default success responses
    fun setSuccessMediaList(
        items: List<MediaListItem>,
        pageInfo: PageInfo = PageInfo(
            hasNextPage = false,
            currentPage = 1,
            lastPage = 1,
            perPage = 10
        )
    ) {
        mediaListResult = Result.Success(MediaListResult(items, pageInfo))
    }

    fun setSuccessMediaDetails(mediaDetails: MediaDetails) {
        mediaDetailsResult = Result.Success(mediaDetails)
    }

    // Convenience methods for loading state
    fun setLoadingMediaList() {
        mediaListResult = Result.Loading
    }

    fun setLoadingMediaDetails() {
        mediaDetailsResult = Result.Loading
    }

    // Mock error responses
    fun setErrorMediaList(exception: Throwable = Exception("Test error")) {
        mediaListResult = Result.Error(exception)
    }

    fun setErrorMediaDetails(exception: Throwable = Exception("Test error")) {
        mediaDetailsResult = Result.Error(exception)
    }

    override suspend fun getMediaList(
        page: Int,
        perPage: Int,
        search: String?
    ): Result<MediaListResult> {
        return mediaListResult
    }

    override suspend fun getMediaDetails(mediaId: Int): Result<MediaDetails> {
        return mediaDetailsResult
    }
}