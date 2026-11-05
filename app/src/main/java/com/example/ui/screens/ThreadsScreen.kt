package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MessageProtocol
import com.example.data.repository.MessagingRepository
import com.example.ui.components.PinnedChatsGrid
import com.example.ui.components.SwipeableConversationCard
import com.example.ui.theme.LocalThemePreferences
import com.example.ui.theme.RobotoMonoFontFamily
import kotlinx.coroutines.launch

@Composable
fun ThreadsScreen(
    repository: MessagingRepository,
    searchQuery: String,
    onConversationClick: (Long) -> Unit,
    showNewChatDialog: Boolean = false,
    onDismissNewChatDialog: () -> Unit = {},
    onSwipeLeftPeek: (Long, Float) -> Unit = { _, _ -> },
    onNavigateToMedia: () -> Unit = {},
    onOpenWebSync: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = LocalThemePreferences.current

    val allDirectThreads by repository.getDirectThreads().collectAsState(initial = emptyList())
    val pinnedList by repository.getPinnedDirectConversations().collectAsState(initial = emptyList())

    // Selection mode state
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    val isSelectionMode = selectedIds.isNotEmpty()

    BackHandler(enabled = isSelectionMode) {
        selectedIds = emptySet()
    }

    val pinnedIds = remember(pinnedList) { pinnedList.map { it.id }.toSet() }

    val filteredThreads = remember(allDirectThreads, searchQuery) {
        if (searchQuery.isBlank()) allDirectThreads
        else allDirectThreads.filter {
            it.recipientName.contains(searchQuery, ignoreCase = true) ||
            it.lastMessage.contains(searchQuery, ignoreCase = true) ||
            it.recipientPhone.contains(searchQuery)
        }
    }

    // Deduplicate: pinned threads should only appear in the pinned section at the top
    val unpinnedThreads = remember(filteredThreads, pinnedIds) {
        filteredThreads.filter { it.id !in pinnedIds }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF000000) // Pure OLED Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF000000))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Terminal CMD Styled Brand Header at Top Left
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ">_ ",
                        fontFamily = RobotoMonoFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "KNKT",
                        fontFamily = RobotoMonoFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color.White,
                        modifier = Modifier.testTag("knkt_top_left_brand")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF111116),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                    ) {
                        Text(
                            text = "terminal",
                            fontFamily = RobotoMonoFontFamily,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = if (isSelectionMode) 180.dp else 140.dp)
                ) {
                    // One UI Reachability Viewing Spacer (shifts content down within easy thumb reach)
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Google Messages Web Sync Banner
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0C0C10),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clickable { onOpenWebSync() }
                                .testTag("threads_google_web_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CloudSync,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Google Messages Web",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "SMS & RCS",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Pair your phone to sync real SMS & RCS texts here",
                                        fontSize = 11.sp,
                                        color = Color(0xFFA1A1AA)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Open Web Sync",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Direct Messages Header Title
                    item {
                        Text(
                            text = "DIRECT MESSAGES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFF71717A),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }

                    if (unpinnedThreads.isEmpty() && pinnedList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Forum,
                                        contentDescription = null,
                                        tint = Color(0xFF3F3F46),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (searchQuery.isBlank()) "No direct conversations yet" else "No matching conversations",
                                        color = Color(0xFFA1A1AA),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(
                            items = unpinnedThreads,
                            key = { it.id }
                        ) { conv ->
                            val isSelected = conv.id in selectedIds
                            SwipeableConversationCard(
                                conversation = conv,
                                swipeRightAction = prefs.swipeRightAction,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onClick = {
                                    if (isSelectionMode) {
                                        selectedIds = if (isSelected) selectedIds - conv.id else selectedIds + conv.id
                                    } else {
                                        onConversationClick(conv.id)
                                    }
                                },
                                onLongClick = {
                                    selectedIds = if (isSelected) selectedIds - conv.id else selectedIds + conv.id
                                },
                                onSwipeLeftPeek = { progress ->
                                    onSwipeLeftPeek(conv.id, progress)
                                },
                                onSwipeLeftComplete = {
                                    onConversationClick(conv.id)
                                },
                                onSwipeRight = {
                                    scope.launch {
                                        val feedback = repository.performRightSwipeAction(conv.id, prefs.swipeRightAction)
                                        Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Pinned Contacts Section towards the bottom for ergonomic thumb reach
                    if (searchQuery.isBlank() && pinnedList.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(18.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "PINNED CONTACTS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Color(0xFF71717A)
                                )
                                Text(
                                    text = "${pinnedList.size}/4",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        item {
                            PinnedChatsGrid(
                                pinnedList = pinnedList,
                                selectedIds = selectedIds,
                                onConversationClick = { id ->
                                    if (isSelectionMode) {
                                        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
                                    } else {
                                        onConversationClick(id)
                                    }
                                },
                                onConversationLongClick = { id ->
                                    selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }

            // Selection Floating Toolbar (Archive, Trash, Media, Pin, Cancel)
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF101014),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A)),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selection_toolbar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { selectedIds = emptySet() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${selectedIds.size} selected",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Pin toggle
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        var pinCount = 0
                                        selectedIds.forEach { id ->
                                            if (repository.togglePin(id)) pinCount++
                                        }
                                        Toast.makeText(context, "Updated pins", Toast.LENGTH_SHORT).show()
                                        selectedIds = emptySet()
                                    }
                                },
                                modifier = Modifier.size(38.dp).testTag("selection_pin_btn")
                            ) {
                                Icon(Icons.Default.PushPin, contentDescription = "Pin", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            }

                            // Archive
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        selectedIds.forEach { id -> repository.toggleArchive(id) }
                                        Toast.makeText(context, "Archived ${selectedIds.size} threads", Toast.LENGTH_SHORT).show()
                                        selectedIds = emptySet()
                                    }
                                },
                                modifier = Modifier.size(38.dp).testTag("selection_archive_btn")
                            ) {
                                Icon(Icons.Default.Archive, contentDescription = "Archive", tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                            }

                            // Trash
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        selectedIds.forEach { id -> repository.moveToTrash(id) }
                                        Toast.makeText(context, "Moved ${selectedIds.size} threads to Trash", Toast.LENGTH_SHORT).show()
                                        selectedIds = emptySet()
                                    }
                                },
                                modifier = Modifier.size(38.dp).testTag("selection_trash_btn")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Trash", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }

                            // Media
                            IconButton(
                                onClick = {
                                    selectedIds = emptySet()
                                    onNavigateToMedia()
                                },
                                modifier = Modifier.size(38.dp).testTag("selection_media_btn")
                            ) {
                                Icon(Icons.Default.PermMedia, contentDescription = "Media", tint = Color(0xFF06B6D4), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // New Direct Chat Modal (triggered from navigation bubble)
    if (showNewChatDialog) {
        NewDirectChatDialog(
            onDismiss = onDismissNewChatDialog,
            onStartChat = { name, phone, protocol, isEncrypted ->
                onDismissNewChatDialog()
                scope.launch {
                    val id = repository.createOrGetDirectConversation(name, phone, protocol, isEncrypted)
                    onConversationClick(id)
                }
            }
        )
    }
}

@Composable
fun NewDirectChatDialog(
    onDismiss: () -> Unit,
    onStartChat: (name: String, phone: String, protocol: MessageProtocol, isEncrypted: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var protocol by remember { mutableStateOf(MessageProtocol.RCS) }
    var isEncrypted by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A0A0C),
        shape = RoundedCornerShape(26.dp),
        title = { Text("New Direct Message", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contact Name") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("direct_name_input")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("+1 (555) 000-0000") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("direct_phone_input")
                )

                Text("Transmission Protocol", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MessageProtocol.values().forEach { p ->
                        val isSelected = protocol == p
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color(0xFF18181B),
                            modifier = Modifier
                                .clickable {
                                    protocol = p
                                    if (p != MessageProtocol.RCS) isEncrypted = false
                                }
                                .padding(2.dp)
                        ) {
                            Text(
                                text = p.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFA1A1AA),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (phone.isNotBlank()) onStartChat(name, phone, protocol, isEncrypted)
                },
                enabled = phone.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("start_direct_chat_btn")
            ) {
                Text("Start")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
