package `fun`.coda.app.yoyoplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import `fun`.coda.app.yoyoplayer.VideoPlayerActivity
import `fun`.coda.app.yoyoplayer.model.SubtitleSettings

@Composable
fun SubtitleSettingsDialog(
    currentSettings: SubtitleSettings,
    onSettingsChanged: (SubtitleSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var marginText by remember { mutableStateOf(currentSettings.marginBottom.toInt().toString()) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("字幕位置设置") },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = "请输入字幕距离底部的像素值（0-300）：",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = marginText,
                    onValueChange = { 
                        // 只允许输入数字
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            marginText = it
                            showError = false
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = showError,
                    supportingText = if (showError) {
                        { Text("请输入0-300之间的数值") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val margin = marginText.toIntOrNull() ?: 0
                    if (margin in 0..300) {
                        onSettingsChanged(SubtitleSettings(margin.toFloat()))
                        onDismiss()
                    } else {
                        showError = true
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
} 