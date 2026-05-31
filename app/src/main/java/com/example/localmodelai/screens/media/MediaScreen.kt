package com.example.localmodelai.screens.media

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    var mediaFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }


    fun refreshMedia(){
        mediaFiles = chatViewModel.mediaStorage.loadInternalMediaFiles()
    }
    LaunchedEffect(Unit) {
        refreshMedia()
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
                    MediaGridItem(
                        file = file,
                        onClick = {
                            selectedImageUrl = file.absolutePath },
                        onDelete = {
                            if(file.exists() && file.delete()){
                                Toast.makeText(context,"File deleted",Toast.LENGTH_SHORT).show()
                                refreshMedia()
                            } else {
                                Toast.makeText(context,"Failed to delete file",Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
    selectedImageUrl?.let{ url ->
        Surface(
            color = Color.Black.copy(alpha = 0.9f),
            modifier = Modifier
                .fillMaxSize()
                .clickable{selectedImageUrl = null}
        ){
            Box(contentAlignment = Alignment.Center){
                AsyncImage(
                    model = url,
                    contentDescription = "Full Image Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun MediaGridItem(
    file: File,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember {mutableStateOf(false)}
    Box(
        modifier = Modifier
            .aspectRatio(1f) // Makes it a perfect square
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = {showMenu = true}
            )
    ) {
        AsyncImage(
            model = file, // Coil accepts local java.io.File directly
            contentDescription = "Stored media file",
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop, // Fills the square nicely
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ){
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = {showMenu = false}
            ) {
                DropdownMenuItem(
                    text = {Text("Delete")},
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Icon",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}