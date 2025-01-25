package `fun`.coda.app.yoyoplayer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import `fun`.coda.app.yoyoplayer.utils.CookieManager

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    var showCookieDialog by remember { mutableStateOf(false) }
    val cookieManager = CookieManager(LocalContext.current)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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

        // 这里可以添加更多设置项...
    }

    // Cookie 设置对话框
    if (showCookieDialog) {
        CookieSettingDialog(
            cookieManager = cookieManager,
            onDismiss = { showCookieDialog = false }
        )
    }
} 