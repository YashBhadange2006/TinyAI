package com.example.localmodelai.components

import android.widget.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localmodelai.screens.chat.ChatViewModel
import com.example.localmodelai.screens.chat.reloadActiveModel
import com.example.localmodelai.screens.chat.updateCurrentSystemPrompt

@Composable
fun ModelSettingsSheetContent(
    chatviewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    var prompt by remember(chatviewModel.currentSystemPrompt) {
        mutableStateOf(chatviewModel.currentSystemPrompt)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 32.dp, top = 8.dp)
    ) {
        Text(
            text = "Configuration",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            )
        )


        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SYSTEM PROMPT",
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 0.8.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(top = 12.dp),
            placeholder = { Text("Enter system prompt...") },
            enabled = !chatviewModel.isModelLoading,
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Text(
            text = "This prompt guides the fundamental behavior and personality of the model.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp, start = 4.dp, end = 4.dp),
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                chatviewModel.updateCurrentSystemPrompt(prompt)
                chatviewModel.reloadActiveModel()
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled = !chatviewModel.isModelLoading,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )

        ) {
            Text(if (chatviewModel.isModelLoading) "Loading..." else "Update Context",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            )
        }
    }
}
