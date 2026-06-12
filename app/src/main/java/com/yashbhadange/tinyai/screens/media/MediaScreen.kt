package com.yashbhadange.tinyai.screens.media

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yashbhadange.tinyai.screens.chat.ChatViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(
    chatViewModel: ChatViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var mediaFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    val selectedFiles = remember {mutableStateListOf<File>()} //Basically the UI will change for selected files in the list
    val isSelectedMode = selectedFiles.isNotEmpty()

    BackHandler(enabled = true) {
        if(selectedImageUrl !=null){
            // If an image preview is open, close the preview first
            selectedImageUrl = null
        } else if(isSelectedMode){
            // If items are selected, clear selection mode instead of leaving the screen
            selectedFiles.clear()
        } else {
            // go to ChatUI Screen
            onBack()
        }
    }
    fun refreshMedia(){
        mediaFiles = chatViewModel.mediaStorage.loadInternalMediaFiles()
        selectedFiles.clear() // After refresh the UI will clear the selection
    }

    LaunchedEffect(Unit) {
        refreshMedia()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Media") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if(isSelectedMode){
                                // If user selects back then instead of going to ChatUI it will exit selection
                                selectedFiles.clear()
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if(isSelectedMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if(isSelectedMode) "Clear Selection" else "Back"
                        )
                    }
                },
                actions = {
                    if(isSelectedMode){
                        IconButton(
                            onClick = {
                                var deletedCount = 0
                                selectedFiles.forEach { file ->
                                    if(file.exists() && file.delete())
                                        deletedCount++
                                }
                                Toast.makeText(context,"Deleted $deletedCount files", Toast.LENGTH_SHORT).show()
                                refreshMedia()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Selected",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if(mediaFiles.isEmpty())
        {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ){
                Text("No media stored yet",style = MaterialTheme.typography.bodyLarge)
            }
        }else{
            LazyVerticalGrid(
                columns = GridCells.Fixed(3), // 3 columns grid
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(mediaFiles) { file ->
                    val isSelected = selectedFiles.contains(file)
                    MediaGridItem(
                        file = file,
                        onClick = {
                            if(isSelectedMode){
                                if(isSelected) selectedFiles.remove(file) else selectedFiles.add(file)
                            } else {
                                selectedImageUrl = file.absolutePath
                            }
                        },
                        isSelected = isSelected,
                        isSelectedMode = isSelectedMode,
                        onLongClick = {
                            // on click add it to selectedFiles val
                            if(!isSelectedMode){
                                selectedFiles.add(file)
                            }
                        }
                    )
                }
            }
        }
    }
    selectedImageUrl?.let{ url ->
        var scale by remember {mutableStateOf(1f)}
        var offset by remember { mutableStateOf(Offset.Zero) }

        var isDragging by remember { mutableStateOf(false) }
        val animatedOffset by animateOffsetAsState(
            targetValue = if (isDragging) offset else Offset.Zero,
            animationSpec = tween(durationMillis = 200),
            label = "OffsetAnimation"
        )
        // use for raw offset while drag, else use animated offset on release
        val currentOffset = if(isDragging) offset else animatedOffset

        val dismissThreshold = 300f

        val alphaProgress = if (scale <= 1f) {
            (1f - (kotlin.math.abs(currentOffset.y) / (dismissThreshold * 2f))).coerceIn(0f, 1f)
        } else {
            1f
        }

        Surface(
            color = Color.Black.copy(alpha = 0.9f*alphaProgress),
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit){
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale*zoom).coerceIn(1f,5f)
                        isDragging = true
                        offset+=pan
                    }
                }
                .pointerInput(Unit){
                    awaitPointerEventScope {
                        while(true){
                            val event = awaitPointerEvent()
                            if(event.changes.all { !it.pressed }){
                                isDragging = false
                                if (scale <= 1f && kotlin.math.abs(offset.y) > dismissThreshold){
                                   // Preview dismiss
                                    selectedImageUrl = null
                                } else if(scale<=1f){
                                    // user didnt drag enough
                                    offset = Offset.Zero
                                }
                            }

                        }
                    }
                }
                .clickable{selectedImageUrl = null}
        ){
            Box(contentAlignment = Alignment.Center){
                AsyncImage(
                    model = url,
                    contentDescription = "Full Image Preview",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            // scale down slightly to give detaching feel
                            scaleX = if (scale <= 1f) scale * alphaProgress.coerceIn(0.7f, 1f) else scale,
                            scaleY = if (scale <= 1f) scale * alphaProgress.coerceIn(0.7f, 1f) else scale,
                            translationX = currentOffset.x,
                            translationY = currentOffset.y
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ){},
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun MediaGridItem(
    file: File,
    isSelected: Boolean,
    isSelectedMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        AsyncImage(
            model = file,
            contentDescription = "Stored media file",
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // Dim layer on selected grid cell
        if(isSelected){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )
        }

        // Overlay UI for selected Grid cells
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentAlignment = Alignment.TopEnd
        ){
            if(isSelectedMode && !isSelected){
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                )
            }

            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(22.dp)
                        .background(Color.White, CircleShape)
                )
            }

        }
    }
}