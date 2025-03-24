package com.jdacodes.graphqlanimedemo.media.data.remote

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.okHttpClient
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

val apolloClient = ApolloClient.Builder()
    .serverUrl("https://graphql.anilist.co")
    //does not need websocket for now
    .okHttpClient(
        OkHttpClient.Builder()
            .addInterceptor(AuthorizationInterceptor())
            .build()
    )
    .build()

private class AuthorizationInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .apply {
                //No token needed for the api
            }
            .build()
        return chain.proceed(request)
    }
}