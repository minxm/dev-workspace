package com.redbookclone.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.redbookclone.app.R
import com.redbookclone.app.ui.navigation.Screen
import com.redbookclone.app.ui.theme.RedBookRed

sealed class BottomNavItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: Int
) {
    object Home : BottomNavItem(
        Screen.Home.route,
        Icons.Filled.Home,
        Icons.Outlined.Home,
        R.string.tab_home
    )
    object Discover : BottomNavItem(
        Screen.Discover.route,
        Icons.Filled.Explore,
        Icons.Outlined.Explore,
        R.string.tab_discover
    )
    object Publish : BottomNavItem(
        Screen.Publish.route,
        Icons.Filled.AddCircle,
        Icons.Outlined.AddCircle,
        R.string.tab_publish
    )
    object Messages : BottomNavItem(
        Screen.Messages.route,
        Icons.Filled.ChatBubble,
        Icons.Outlined.ChatBubbleOutline,
        R.string.tab_messages
    )
    object Profile : BottomNavItem(
        Screen.Profile.route,
        Icons.Filled.Person,
        Icons.Outlined.PersonOutline,
        R.string.tab_profile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Discover,
        BottomNavItem.Publish,
        BottomNavItem.Messages,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = null
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(item.label),
                                fontSize = 11.sp
                            )
                        },
                        selected = selected,
                        onClick = {
                            if (item.route == Screen.Publish.route) {
                                // Navigate to publish screen
                                navController.navigate(Screen.Publish.route) {
                                    launchSingleTop = true
                                }
                            } else {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RedBookRed,
                            selectedTextColor = RedBookRed,
                            indicatorColor = Color.White,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNoteClick = { noteId ->
                        navController.navigate(Screen.NoteDetail.createRoute(noteId))
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    }
                )
            }

            composable(Screen.Discover.route) {
                DiscoverScreen(
                    onNoteClick = { noteId ->
                        navController.navigate(Screen.NoteDetail.createRoute(noteId))
                    }
                )
            }

            composable(Screen.Publish.route) {
                PublishScreen(
                    onBackClick = { navController.navigateUp() },
                    onPublishSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Messages.route) {
                MessagesScreen()
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNoteClick = { noteId ->
                        navController.navigate(Screen.NoteDetail.createRoute(noteId))
                    },
                    onLogout = onLogout
                )
            }

            composable(Screen.NoteDetail.route) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
                NoteDetailScreen(
                    noteId = noteId,
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onBackClick = { navController.navigateUp() },
                    onNoteClick = { noteId ->
                        navController.navigate(Screen.NoteDetail.createRoute(noteId))
                    }
                )
            }
        }
    }
}

@Composable
fun DiscoverScreen(
    onNoteClick: (String) -> Unit
) {
    // Simplified discover screen - reuses HomeScreen
    HomeScreen(
        onNoteClick = onNoteClick,
        onSearchClick = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tab_messages)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "暂无消息",
                    color = Color.Gray
                )
            }
        }
    }
}
