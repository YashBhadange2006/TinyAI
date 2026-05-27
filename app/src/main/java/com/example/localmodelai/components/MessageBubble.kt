package com.example.localmodelai.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.localmodelai.chatui.Message
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin

@Composable
fun MessageBubble(message: Message) {
    val context = LocalContext.current

    val elegantShape = if (message.isUser) {
        RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomEnd = 2.dp, // Very subtle, clean tail for the sender
            bottomStart = 20.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomEnd = 20.dp,
            bottomStart = 2.dp // Very subtle, clean tail for the receiver
        )
    }

    val backgroundColor = if (message.isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (message.isUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(elegantShape)
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (message.isUser) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = contentColor,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                )
            } else {
                MarkdownMessage(
                    markdown = normalizeMarkdownForMarkwon(message.text),
                    textColor = contentColor.toArgb()
                )
            }
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        IconButton(
            onClick = { copyMessageToClipboard(context, message.text) },
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Copy message",
                tint = contentColor.copy(alpha = 0.5f)
            )
        }
    }

}

@Composable
private fun MarkdownMessage(
    markdown: String,
    textColor: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val linkColor = MaterialTheme.colorScheme.primary.toArgb()
    val latexTextSize = with(density) { 15.sp.toPx() }

    val markwon = remember(context, latexTextSize) {
        Markwon.builder(context)
            .usePlugin(MarkwonInlineParserPlugin.create())
            .usePlugin(
                JLatexMathPlugin.create(latexTextSize) { builder ->
                    builder.inlinesEnabled(true)
                }
            )
            .usePlugin(TablePlugin.create(context))
            .build()
    }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            TextView(viewContext).apply {
                setTextIsSelectable(false)
                movementMethod = LinkMovementMethod.getInstance()
                textSize = 15f
                setLineSpacing(6f, 1.1f) // Smooth breathing space between text lines
            }
        },
        update = { textView ->
            textView.setTextColor(textColor)
            textView.setLinkTextColor(linkColor)
            markwon.setMarkdown(textView, markdown)
        }
    )
}

private fun normalizeMarkdownForMarkwon(text: String): String {
    val inlineMathRegex = Regex("""(?<!\$)\$(?!\$)(.+?)(?<!\$)\$(?!\$)""")
    return text.replace(inlineMathRegex) { matchResult ->
        val expression = matchResult.groupValues[1].trim()
        if (expression.isEmpty()) "" else "\$\$$expression\$\$"
    }
}

private fun copyMessageToClipboard(
    context: Context,
    text: String
) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText("PocketAI message", text))
}
