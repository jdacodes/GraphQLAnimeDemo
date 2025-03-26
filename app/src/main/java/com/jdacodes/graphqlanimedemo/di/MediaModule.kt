package com.jdacodes.graphqlanimedemo.di

import com.apollographql.apollo.ApolloClient
import com.jdacodes.graphqlanimedemo.media.data.repository.MediaRepositoryImpl
import com.jdacodes.graphqlanimedemo.media.domain.repository.MediaRepository
import com.jdacodes.graphqlanimedemo.media.domain.usecase.GetMediaDetailsUseCase
import com.jdacodes.graphqlanimedemo.media.domain.usecase.GetMediaListUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object MediaModule {
    @Provides
    @Singleton
    fun provideMediaRepository(apolloClient: ApolloClient): MediaRepository {
        return MediaRepositoryImpl(apolloClient)
    }

    @Provides
    fun provideGetMediaDetailsUseCase(repository: MediaRepository): GetMediaDetailsUseCase {
        return GetMediaDetailsUseCase(repository)
    }

    @Provides
    fun provideGetMediaListUseCase(repository: MediaRepository): GetMediaListUseCase {
        return GetMediaListUseCase(repository)
    }
}