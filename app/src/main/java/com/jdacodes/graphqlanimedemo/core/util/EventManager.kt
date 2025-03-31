package com.jdacodes.graphqlanimedemo.core.util

import androidx.annotation.StringRes
import com.jdacodes.graphqlanimedemo.core.util.EventManager.eventChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.receiveAsFlow

object EventManager {
    private val eventChannel = Channel<AppEvent>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    fun triggerEvent(event: AppEvent) {
        CoroutineScope(Dispatchers.Default).launch { eventChannel.send(event) }
    }

    fun triggerEventWithDelay(event: AppEvent, delay: Long = 100) {
        CoroutineScope(Dispatchers.Default).launch {
            delay(delay)
            triggerEvent(event)
        }
    }

    sealed class AppEvent {
        data class ShowSnackbar(@StringRes val message: Int) : AppEvent()
        data class NavigateToDetail(val mediaId: Int): AppEvent()
        data object ExitScreen: AppEvent()
    }
}