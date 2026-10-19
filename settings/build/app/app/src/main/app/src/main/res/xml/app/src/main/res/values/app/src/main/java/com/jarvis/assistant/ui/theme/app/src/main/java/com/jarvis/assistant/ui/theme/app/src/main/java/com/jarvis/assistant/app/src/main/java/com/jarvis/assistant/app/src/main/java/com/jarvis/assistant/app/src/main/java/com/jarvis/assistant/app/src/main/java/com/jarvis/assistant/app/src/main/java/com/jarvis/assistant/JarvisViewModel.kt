package com.jarvis.assistant

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class HistoryItem(
    val userText: String,
    val responseText: String,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class JarvisViewModel : ViewModel() {
    var isListening by mutableStateOf(false)
        private set

    var currentRms by mutableFloatStateOf(0f)
        private set

    var pendingCallContact by mutableStateOf<ContactMatch?>(null)
        private set

    val history = mutableStateListOf<HistoryItem>()

    fun setListeningState(listening: Boolean) {
        isListening = listening
        if (!listening) currentRms = 0f
    }

    fun updateRms(rms: Float) {
        currentRms = rms
    }

    fun addHistory(userText: String, responseText: String, isError: Boolean = false) {
        history.add(0, HistoryItem(userText, responseText, isError))
    }

    fun setPendingCall(contact: ContactMatch?) {
        pendingCallContact = contact
    }
}
