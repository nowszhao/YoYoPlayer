package `fun`.coda.app.yoyoplayer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `fun`.coda.app.yoyoplayer.model.Tag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagBar(
    tags: List<Tag>,
    selectedTag: Tag?,
    onTagSelected: (Tag?) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedTag == null,
                    onClick = { onTagSelected(null) },
                    label = { Text("全部") }
                )
            }
            
            items(tags) { tag ->
                FilterChip(
                    selected = tag == selectedTag,
                    onClick = { onTagSelected(tag) },
                    label = {
                        Text("${tag.name} (${tag.count})")
                    }
                )
            }
        }
    }
} 