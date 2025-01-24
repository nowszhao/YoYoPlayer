package `fun`.coda.app.yoyoplayer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `fun`.coda.app.yoyoplayer.network.SubtitleItem

@Composable
fun SubtitleSelectionDialog(
    subtitles: List<SubtitleItem>,
    selectedSubtitle: SubtitleItem?,
    onSubtitleSelected: (SubtitleItem?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择字幕") },
        text = {
            Column {
                // 添加关闭字幕选项
                TextButton(
                    onClick = { onSubtitleSelected(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "关闭字幕",
                        color = if (selectedSubtitle == null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // 字幕列表
                subtitles.forEach { subtitle ->
                    TextButton(
                        onClick = { onSubtitleSelected(subtitle) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = subtitle.lan_doc,
                            color = if (subtitle == selectedSubtitle) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
} 