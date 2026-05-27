package com.example.localmodelai.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChatInput(
    selectedAttachmentName: String?,
    onAttachClick: () -> Unit,
    onClearAttachment: () -> Unit,
    onSend: (String) -> Unit
) {
    var text by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 50.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (selectedAttachmentName != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Attached: $selectedAttachmentName",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    IconButton(onClick = onClearAttachment) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove attachment"
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onAttachClick,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Attach image"
                )
            }
            TextField(
                value = text,
                onValueChange = {
                    text = it
                },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Type a message...")
                }
            )

            IconButton(
                onClick = {
                    if (text.isNotBlank() || selectedAttachmentName != null) {
                        onSend(text)
                        text = ""
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send"
                )
            }
        }
    }
}