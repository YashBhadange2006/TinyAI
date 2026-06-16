package com.yashbhadange.tinyai.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.yashbhadange.tinyai.screens.chat.Message
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MessageBubble(message: Message) {
    val context = LocalContext.current
    var showImagePreview by remember(message.imagePath) { mutableStateOf(false) }
    var showThinking by rememberSaveable { mutableStateOf(false) }

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
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    val contentColor = if (message.isUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 1.dp),
        contentAlignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = if (message.isUser) 290.dp else 330.dp)
                .padding(bottom = if (!message.isUser && message.thinkingText.isNotBlank()) 6.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!message.isUser && message.thinkingText.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showThinking = !showThinking }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Thinking",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = if (showThinking) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (showThinking) "Hide thinking" else "Show thinking",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (showThinking) {
                            Text(
                                text = message.thinkingText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }
            }
            Surface(
                shape = elegantShape,
                color = backgroundColor
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (message.messageType == "image" && message.imagePath != null) {
                        val bitmap = remember(message.imagePath) {
                            BitmapFactory.decodeFile(message.imagePath)
                        }
                        bitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = message.imageName ?: "Attached image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { showImagePreview = true },
                                contentScale = ContentScale.Crop
                            )
                        }
                        message.imageName?.let { imageName ->
                            Text(
                                text = imageName,
                                style = MaterialTheme.typography.labelSmall.copy(color = contentColor)
                            )
                        }
                        if (message.text.isNotBlank()) {
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = contentColor,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                )
                            )
                        }
                    } else if (message.isUser) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = contentColor,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        )
                    } else if (message.isStreaming) {
                        Text(
                            text = if (message.text.isBlank()) {
                                buildLightweightStreamingMarkdown("...")
                            } else {
                                buildLightweightStreamingMarkdown(input = message.text)
                            },
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
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        val scope = rememberCoroutineScope()
        var isClicked by remember {mutableStateOf(false)}
        val icon = if(isClicked) Icons.Default.Check else Icons.Default.ContentCopy
        IconButton(
            onClick = {
                        copyMessageToClipboard(context, message.text)
                        if (!isClicked){
                            isClicked = true
                            scope.launch{
                                delay(1500)
                                isClicked = false
                            }
                        }
                      },
            modifier = Modifier.padding(top = 1.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Copy message",
                tint = contentColor.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }

    if (showImagePreview && message.imagePath != null) {
        val previewBitmap = remember(message.imagePath) {
            BitmapFactory.decodeFile(message.imagePath)
        }
        var scale by remember(message.imagePath) { mutableStateOf(1f) }
        var offsetX by remember(message.imagePath) { mutableStateOf(0f) }
        var offsetY by remember(message.imagePath) { mutableStateOf(0f) }
        val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
            scale = (scale * zoomChange).coerceIn(1f, 4f)
            if (scale > 1f) {
                offsetX += panChange.x
                offsetY += panChange.y
            } else {
                offsetX = 0f
                offsetY = 0f
            }
        }

        Dialog(
            onDismissRequest = { showImagePreview = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .fillMaxHeight(0.68f),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    previewBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = message.imageName ?: "Image preview",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth()
                                .aspectRatio(it.width.toFloat() / it.height.toFloat())
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    translationX = offsetX
                                    translationY = offsetY
                                }
                                .transformable(transformableState),
                            contentScale = ContentScale.Fit
                        )
                    }

                    IconButton(
                        onClick = { showImagePreview = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close preview"
                        )
                    }
                }
            }
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
    clipboardManager.setPrimaryClip(ClipData.newPlainText("TinyAI message", text))
}

private fun buildLightweightStreamingMarkdown(input: String): AnnotatedString {
    val normalized = input.replace("\r\n", "\n")
    val builder = AnnotatedString.Builder()
    var index = 0

    while (index < normalized.length) {
        if (normalized.startsWith("**", index)) {
            val close = normalized.indexOf("**", startIndex = index + 2)
            if (close > index + 2) {
                val text = normalized.substring(index + 2, close)
                builder.pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold))
                builder.append(text)
                builder.pop()
                index = close + 2
                continue
            }
        }

        if (normalized[index] == '*') {
            val close = normalized.indexOf('*', startIndex = index + 1)
            if (close > index + 1) {
                val text = normalized.substring(index + 1, close)
                builder.pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                builder.append(text)
                builder.pop()
                index = close + 1
                continue
            }
        }

        if (normalized[index] == '`') {
            val close = normalized.indexOf('`', startIndex = index + 1)
            if (close > index + 1) {
                val text = normalized.substring(index + 1, close)
                builder.pushStyle(SpanStyle(fontFamily = FontFamily.Monospace))
                builder.append(text)
                builder.pop()
                index = close + 1
                continue
            }
        }

        builder.append(normalized[index])
        index++
    }

    return builder.toAnnotatedString()
}
