package `fun`.coda.app.yoyoplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import android.view.KeyEvent
import androidx.compose.ui.focus.onFocusChanged

@Composable
fun VideoControlBar(
    isPlaylist: Boolean,
    hasSubtitles: Boolean,
    onPlaylistClick: () -> Unit,
    onSubtitleClick: () -> Unit,
    onQualityClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onFocusedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentFocusIndex by remember { mutableStateOf(0) }
    val buttonRefs = remember { mutableListOf<FocusRequester>() }
    
    val buttons = buildList {
        if (isPlaylist) add(Pair("选集", onPlaylistClick))
        if (hasSubtitles) add(Pair("字幕", onSubtitleClick))
        add(Pair("清晰度", onQualityClick))
        add(Pair("倍速", onSpeedClick))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        buttons.forEachIndexed { index, (text, onClick) ->
            val buttonFocusRequester = remember { FocusRequester() }
            buttonRefs.add(buttonFocusRequester)
            
            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .focusRequester(buttonFocusRequester)
                    .focusable(true)
                    .onFocusChanged { state ->
                        if (state.isFocused) {
                            currentFocusIndex = index
                            onFocusedChanged(true)
                        }
                    }
                    .onKeyEvent { keyEvent ->
                        when {
                            keyEvent.type == KeyEventType.KeyDown -> {
                                when (keyEvent.nativeKeyEvent?.keyCode) {
                                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                                        if (currentFocusIndex > 0) {
                                            currentFocusIndex--
                                            buttonRefs[currentFocusIndex].requestFocus()
                                            true
                                        } else false
                                    }
                                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                        if (currentFocusIndex < buttons.size - 1) {
                                            currentFocusIndex++
                                            buttonRefs[currentFocusIndex].requestFocus()
                                            true
                                        } else false
                                    }
                                    KeyEvent.KEYCODE_DPAD_UP -> {
                                        onFocusedChanged(false)
                                        true
                                    }
                                    else -> false
                                }
                            }
                            else -> false
                        }
                    }
            ) {
                Icon(
                    imageVector = when(text) {
                        "选集" -> Icons.Default.List
                        "字幕" -> Icons.Default.Subtitles
                        "清晰度" -> Icons.Default.Settings
                        else -> Icons.Default.Speed
                    },
                    contentDescription = text,
                    tint = if (index == currentFocusIndex) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.White
                    }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        if (buttonRefs.isNotEmpty()) {
            buttonRefs[0].requestFocus()
        }
    }
} 