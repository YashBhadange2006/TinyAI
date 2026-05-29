package com.example.localmodelai.screens.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.localmodelai.screens.chat.ChatViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(
    chatViewModel: ChatViewModel,
    onBack: () -> Unit
) {
    var mediaFiles by remember { mutableStateOf<List<File>>(emptyList()) }

    LaunchedEffect(Unit) {
        mediaFiles = chatViewModel.mediaStorage.loadInternalMediaFiles()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Media") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
                    MediaGridItem(file = file)
                }
            }
        }

    }
}

@Composable
fun MediaGridItem(file: File) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // Makes it a perfect square
            .clip(RoundedCornerShape(8.dp))
    ) {
        AsyncImage(
            model = file, // Coil accepts local java.io.File directly
            contentDescription = "Stored media file",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Fills the square nicely
        )
    }
}