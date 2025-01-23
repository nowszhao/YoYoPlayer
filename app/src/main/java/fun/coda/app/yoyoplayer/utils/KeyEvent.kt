package `fun`.coda.app.yoyoplayer.utils

import android.view.KeyEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object KeyEvent {
    private val keyEventChannel = Channel<KeyEventWrapper>(Channel.UNLIMITED)
    val keyEventFlow = keyEventChannel.receiveAsFlow()

    fun onKeyEvent(event: android.view.KeyEvent): Boolean {
        if (event.action == android.view.KeyEvent.ACTION_DOWN) {
            keyEventChannel.trySend(KeyEventWrapper(event.keyCode))
            return true
        }
        return false
    }

    const val KEYCODE_DPAD_DOWN = android.view.KeyEvent.KEYCODE_DPAD_DOWN
    const val KEYCODE_BACK = android.view.KeyEvent.KEYCODE_BACK
}

data class KeyEventWrapper(
    val keyCode: Int,
    var handled: Boolean = false
) 