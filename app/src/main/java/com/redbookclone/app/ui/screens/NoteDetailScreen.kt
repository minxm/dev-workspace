package com.redbookclone.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.redbookclone.app.data.model.Comment
import com.redbookclone.app.ui.theme.Gray600
import com.redbookclone.app.ui.theme.Gray100
import com.redbookclone.app.ui.theme.RedBookRed
import com.redbookclone.app.ui.viewmodel.CommentViewModel
import com.redbookclone.app.ui.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    onBackClick: () -> Unit,
    noteViewModel: NoteViewModel = hiltViewModel(),
    commentViewModel: CommentViewModel = hiltViewModel()
) {
    val note by noteViewModel.selectedNote.collectAsState()
    val comments by commentViewModel.comments.collectAsState()
    var commentText by remember { mutableStateOf("") }
    var showCommentSheet by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) {
        noteViewModel.loadNoteById(noteId)
        commentViewModel.loadComments(noteId)
    }

    if (note == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = RedBookRed)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Filled.Share, contentDescription = "分享")
                    }
                    IconButton(onClick = { /* More */ }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "更多")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            NoteDetailBottomBar(
                note = note!!,
                onLikeClick = { noteViewModel.likeNote(noteId) },
                onCollectClick = { noteViewModel.collectNote(noteId) },
                onCommentClick = { showCommentSheet = true }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            // Image pager
            item {
                val pagerState = rememberPagerState()
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.75f)
                ) {
                    HorizontalPager(
                        count = note!!.images.size,
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        AsyncImage(
                            model = note!!.images[page],
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    if (note!!.images.size > 1) {
                        HorizontalPagerIndicator(
                            pagerState = pagerState,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            activeColor = Color.White,
                            inactiveColor = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // User info
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = note!!.userAvatar,
                        contentDescription = note!!.username,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = note!!.username,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = { /* Follow */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RedBookRed
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Text("关注", fontSize = 13.sp)
                    }
                }
            }

            // Title and content
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = note!!.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = note!!.content,
                        fontSize = 14.sp,
                        color = Color.Black,
                        lineHeight = 20.sp
                    )

                    if (note!!.location.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Place,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Gray600
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = note!!.location,
                                fontSize = 13.sp,
                                color = Gray600
                            )
                        }
                    }
                }
            }

            // Comments header
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "评论 ${comments.size}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Comments list
            items(comments) { comment ->
                CommentItem(
                    comment = comment,
                    onLikeClick = {
                        commentViewModel.likeComment(comment.id, noteId)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showCommentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCommentSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "写评论...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("说点什么...") },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (commentText.isNotEmpty()) {
                            commentViewModel.addComment(noteId, commentText)
                            commentText = ""
                            showCommentSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedBookRed
                    ),
                    enabled = commentText.isNotEmpty()
                ) {
                    Text("发布评论")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun NoteDetailBottomBar(
    note: com.redbookclone.app.data.model.Note,
    onLikeClick: () -> Unit,
    onCollectClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLikeClick) {
                    Icon(
                        if (note.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "点赞",
                        tint = if (note.isLiked) RedBookRed else Color.Black
                    )
                }
                Text(text = formatCount(note.likesCount), fontSize = 14.sp)

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onCollectClick) {
                    Icon(
                        if (note.isCollected) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "收藏",
                        tint = if (note.isCollected) Color(0xFFFFA500) else Color.Black
                    )
                }
                Text(text = formatCount(note.collectsCount), fontSize = 14.sp)

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onCommentClick) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "评论",
                        tint = Color.Black
                    )
                }
                Text(text = formatCount(note.commentsCount), fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    onLikeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        AsyncImage(
            model = comment.userAvatar,
            contentDescription = comment.username,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = comment.username,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = comment.content,
                fontSize = 14.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getTimeAgo(comment.createdAt),
                    fontSize = 12.sp,
                    color = Gray600
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    if (comment.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "点赞",
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(onClick = onLikeClick),
                    tint = if (comment.isLiked) RedBookRed else Gray600
                )

                if (comment.likesCount > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatCount(comment.likesCount),
                        fontSize = 12.sp,
                        color = Gray600
                    )
                }
            }
        }
    }

    Divider(
        modifier = Modifier.padding(start = 64.dp),
        color = Gray100
    )
}

fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff / 60000}分钟前"
        diff < 86400000 -> "${diff / 3600000}小时前"
        diff < 604800000 -> "${diff / 86400000}天前"
        else -> "${diff / 604800000}周前"
    }
}
