package `fun`.coda.app.yoyoplayer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import `fun`.coda.app.yoyoplayer.utils.CookieManager
import `fun`.coda.app.yoyoplayer.utils.SettingsManager
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    var showCookieDialog by remember { mutableStateOf(false) }
    var showBaseUrlDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val cookieManager = remember { CookieManager(context) }
    val settingsManager = remember { SettingsManager(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 视频源设置项
        ListItem(
            headlineContent = { Text("视频源地址") },
            supportingContent = { Text("设置远程视频列表的获取地址") },
            trailingContent = {
                TextButton(onClick = { showBaseUrlDialog = true }) {
                    Text("设置")
                }
            }
        )

        Divider()

        // B站 Cookie 设置项
        ListItem(
            headlineContent = { Text("B站 Cookie") },
            supportingContent = { Text("设置B站账号Cookie以支持更高清晰度") },
            trailingContent = {
                TextButton(onClick = { showCookieDialog = true }) {
                    Text("设置")
                }
            }
        )

        Divider()
    }

    // Cookie 设置对话框
    if (showCookieDialog) {
        CookieSettingDialog(
            cookieManager = cookieManager,
            onDismiss = { showCookieDialog = false }
        )
    }

    // BaseUrl 设置对话框
    if (showBaseUrlDialog) {
        BaseUrlSettingDialog(
            settingsManager = settingsManager,
            onDismiss = { showBaseUrlDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BaseUrlSettingDialog(
    settingsManager: SettingsManager,
    onDismiss: () -> Unit
) {
    var baseUrlText by remember { mutableStateOf(settingsManager.baseUrl) }
    var showSuccess by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置视频源地址") },
        text = {
            Column {
                if (showSuccess) {
                    Text(
                        "设置成功！",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                if (showError) {
                    Text(
                        "请输入有效的URL地址",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                OutlinedTextField(
                    value = baseUrlText,
                    onValueChange = { 
                        baseUrlText = it
                        showError = false
                    },
                    label = { Text("视频源地址") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Text(
                    "当前默认地址：${SettingsManager.DEFAULT_BASE_URL}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = {
                        settingsManager.resetBaseUrl()
                        baseUrlText = settingsManager.baseUrl
                        showSuccess = true
                        showError = false
                    }
                ) {
                    Text("重置")
                }
                
                Button(
                    onClick = {
                        try {
                            URL(baseUrlText) // 验证URL格式
                            settingsManager.baseUrl = baseUrlText
                            showSuccess = true
                            showError = false
                        } catch (e: Exception) {
                            showError = true
                        }
                    }
                ) {
                    Text("保存")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
} 