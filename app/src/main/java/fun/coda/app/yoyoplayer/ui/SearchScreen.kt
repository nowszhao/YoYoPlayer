package `fun`.coda.app.yoyoplayer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onPlayVideo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("输入视频ID，比如BV1vm411Z7ZN") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Button(
            onClick = { 
                if (searchQuery.isNotEmpty()) {
                    onPlayVideo(searchQuery)
                }
            },
            enabled = searchQuery.isNotEmpty(),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("播放")
        }
    }
} 