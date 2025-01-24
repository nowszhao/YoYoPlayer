package `fun`.coda.app.yoyoplayer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `fun`.coda.app.yoyoplayer.utils.CookieManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookieSettingDialog(
    cookieManager: CookieManager,
    onDismiss: () -> Unit
) {
    var cookieText by remember { mutableStateOf(cookieManager.getCookie()) }
    var showSuccess by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置B站Cookie") },
        text = {
            Column {
                if (showSuccess) {
                    Text(
                        "Cookie 设置成功！",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                OutlinedTextField(
                    value = cookieText,
                    onValueChange = { cookieText = it },
                    label = { Text("输入Cookie") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )
                
                Text(
                    "提示：Cookie可以从浏览器登录B站后获取",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    cookieManager.saveCookie(cookieText)
                    showSuccess = true
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
} 