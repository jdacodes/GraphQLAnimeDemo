package com.jdacodes.graphqlanimedemo.media.data.repository

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.exception.ApolloNetworkException
import com.jdacodes.graphqlanimedemo.MediaDetailsQuery
import com.jdacodes.graphqlanimedemo.MediaQuery
import com.jdacodes.graphqlanimedemo.core.util.Result
import com.jdacodes.graphqlanimedemo.media.domain.model.MediaDetails
import com.jdacodes.graphqlanimedemo.media.domain.model.MediaListResult
import com.jdacodes.graphqlanimedemo.media.domain.repository.MediaRepository
import com.jdacodes.graphqlanimedemo.media.domain.util.toMediaDetails
import com.jdacodes.graphqlanimedemo.media.domain.util.toMediaListResult
import com.jdacodes.graphqlanimedemo.media.presentation.MediaDetailsUiState
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(
    private val apolloClient: ApolloClient
) : MediaRepository {

    override suspend fun getMediaList(
        page: Int,
        perPage: Int,
        search: String?
    ): Result<MediaListResult> {
        return try {
            val response = apolloClient.query(
                MediaQuery(
                    Optional.present(page),
                    Optional.present(perPage),
                    Optional.present(search)
                )
            ).execute()

            val pageData = response.data?.Page

            if (pageData != null) {
                val resultData = pageData.toMediaListResult()
                Result.Success(resultData)
            } else {
                Result.Error(Exception("Page data is null"))
            }

        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getMediaDetails(mediaId: Int): Result<MediaDetails> {
        return try {
            val response = apolloClient.query(
                MediaDetailsQuery(mediaId = Optional.present(mediaId))
            ).execute()

            Log.d("Fetch error", response.exception.toString())
            Log.d("Request error", "${response.errors} ${response.data}")
            Log.d("Field error", "${response.errors} ${response.data}")

            val newUiState = when {
                response.errors.orEmpty().isNotEmpty() -> {
                    MediaDetailsUiState.Error(response.errors!!.first().message)
                }

                response.exception is ApolloNetworkException -> {
                    MediaDetailsUiState.Error("Please check your network connectivity.")
                }

                response.data?.Media != null -> {
                    MediaDetailsUiState.Success(response.data!!.Media!!.toMediaDetails())
                }

                else -> {
                    MediaDetailsUiState.Error("Oh no... An error happened.")
                }
            }

            // Return a successful result if we have success UI state, otherwise an error.
            when (newUiState) {
                is MediaDetailsUiState.Success -> Result.Success(newUiState.media)
                is MediaDetailsUiState.Error -> Result.Error(Exception(newUiState.message))
                else -> Result.Error(Exception("Unexpected UI state"))
            }

        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

