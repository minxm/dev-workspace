package com.redbookclone.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.redbookclone.app.R
import com.redbookclone.app.ui.theme.Gray200
import com.redbookclone.app.ui.theme.RedBookRed
import com.redbookclone.app.ui.viewmodel.NoteViewModel
import com.redbookclone.app.ui.viewmodel.PublishState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen(
    onBackClick: () -> Unit,
    onPublishSuccess: () -> Unit,
    viewModel: NoteViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var showLocationDialog by remember { mutableStateOf(false) }
    
    val publishState by viewModel.publishState.collectAsState()

    LaunchedEffect(publishState) {
        if (publishState is PublishState.Success) {
            onPublishSuccess()
            viewModel.resetPublishState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.publish_note),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isNotEmpty() && content.isNotEmpty() && selectedImages.isNotEmpty()) {
                                viewModel.createNote(
                                    title = title,
                                    content = content,
                                    images = selectedImages,
                                    location = location
                                )
                            }
                        },
                        enabled = publishState !is PublishState.Loading &&
                                title.isNotEmpty() && 
                                content.isNotEmpty() && 
                                selectedImages.isNotEmpty()
                    ) {
                        Text(
                            text = stringResource(R.string.publish),
                            color = if (title.isNotEmpty() && content.isNotEmpty() && selectedImages.isNotEmpty())
                                RedBookRed else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Image selection
            Text(
                text = "添加图片 (${selectedImages.size}/9)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add image button
                if (selectedImages.size < 9) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Gray200, RoundedCornerShape(8.dp))
                                .clickable {
                                    // 模拟添加图片
                                    val newImageUrl = "https://picsum.photos/400/600?random=${System.currentTimeMillis()}"
                                    selectedImages = selectedImages + newImageUrl
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "添加图片",
                                    modifier = Modifier.size(32.dp),
                                    tint = Gray200
                                )
                                Text(
                                    text = "添加图片",
                                    fontSize = 12.sp,
                                    color = Gray200
                                )
                            }
                        }
                    }
                }

                // Selected images
                items(selectedImages) { imageUrl ->
                    Box(
                        modifier = Modifier.size(100.dp)
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        IconButton(
                            onClick = {
                                selectedImages = selectedImages.filter { it != imageUrl }
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .padding(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "删除",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 50) title = it },
                label = { Text("标题") },
                placeholder = { Text("填写标题会有更多赞哦~") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                supportingText = {
                    Text(
                        text = "${title.length}/50",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            OutlinedTextField(
                value = content,
                onValueChange = { if (it.length <= 1000) content = it },
                label = { Text("内容") },
                placeholder = { Text("添加描述...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 10,
                supportingText = {
                    Text(
                        text = "${content.length}/1000",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Location
            OutlinedButton(
                onClick = { showLocationDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (location.isEmpty()) "添加地点" else location,
                    modifier = Modifier.weight(1f)
                )
                if (location.isNotEmpty()) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "清除",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { location = "" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error message
            if (publishState is PublishState.Error) {
                Text(
                    text = (publishState as PublishState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            // Loading indicator
            if (publishState is PublishState.Loading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RedBookRed)
                }
            }
        }
    }

    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("添加地点") },
            text = {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = { Text("输入地点") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showLocationDialog = false }
                ) {
                    Text("确认", color = RedBookRed)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        location = ""
                        showLocationDialog = false
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
}
