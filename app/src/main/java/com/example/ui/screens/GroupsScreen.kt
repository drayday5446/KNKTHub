package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
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
import com.example.data.repository.MessagingRepository
import com.example.ui.components.PinnedChatsGrid
import com.example.ui.components.SwipeableConversationCard
import com.example.ui.theme.LocalThemePreferences
import com.example.ui.theme.RobotoMonoFontFamily
import kotlinx.coroutines.launch

@Composable
fun GroupsScreen(
    repository: MessagingRepository,
    searchQuery: String,
    onConversationClick: (Long) -> Unit,
    showCreateGroupDialog: Boolean = false,
    onDismissCreateGroupDialog: () -> Unit = {},
    onSwipeLeftPeek: (Long, Float) -> Unit = { _, _ -> },
    onNavigateToMedia: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = LocalThemePreferences.current

    val groups by repository.getGroupThreads().collectAsState(initial = emptyList())
    val pinnedGroupList by repository.getPinnedGroupConversations().collectAsState(initial = emptyList())

    // Selection mode state
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    val isSelectionMode = selectedIds.isNotEmpty()

    BackHandler(enabled = isSelectionMode) {
        selectedIds = emptySet()
    }

    val pinnedGroupIds = remember(pinnedGroupList) { pinnedGroupList.map { it.id }.toSet() }

    val filteredGroups = remember(groups, searchQuery) {
        if (searchQuery.isBlank()) groups
        else groups.filter {
            it.recipientName.contains(searchQuery, ignoreCase = true) ||
            it.groupParticipants.contains(searchQuery, ignoreCase = true) ||
            it.lastMessage.contains(searchQuery, ignoreCase = true)
        }
    }

    // Deduplicate: pinned groups only appear in the pinned group section
    val unpinnedGroups = remember(filteredGroups, pinnedGroupIds) {
        filteredGroups.filter { it.id !in pinnedGroupIds }
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
                // Terminal CMD styled brand header
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
                        modifier = Modifier.testTag("knkt_top_left_groups_brand")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF111116),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                    ) {
                        Text(
                            text = "groups",
                            fontFamily = RobotoMonoFontFamily,
                            fontSize = 9.sp,
                            color = Color(0xFF06B6D4),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = if (isSelectionMode) 180.dp else 140.dp)
                ) {
                    // One UI Reachability Viewing Spacer
                    item {
                        Spacer(modifier = Modifier.height(36.dp))
                    }

                    item {
                        Text(
                            text = "GROUP CHATS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFF71717A),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }

                    if (unpinnedGroups.isEmpty() && pinnedGroupList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        tint = Color(0xFF3F3F46),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (searchQuery.isBlank()) "No group chats yet" else "No matching groups",
                                        color = Color(0xFFA1A1AA),
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Open navigation dock to create a group (3+ participants)",
                                        color = Color(0xFF52525B),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(
                            items = unpinnedGroups,
                            key = { it.id }
                        ) { group ->
                            val isSelected = group.id in selectedIds
                            SwipeableConversationCard(
                                conversation = group,
                                swipeRightAction = prefs.swipeRightAction,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onClick = {
                                    if (isSelectionMode) {
                                        selectedIds = if (isSelected) selectedIds - group.id else selectedIds + group.id
                                    } else {
                                        onConversationClick(group.id)
                                    }
                                },
                                onLongClick = {
                                    selectedIds = if (isSelected) selectedIds - group.id else selectedIds + group.id
                                },
                                onSwipeLeftPeek = { progress ->
                                    onSwipeLeftPeek(group.id, progress)
                                },
                                onSwipeLeftComplete = {
                                    onConversationClick(group.id)
                                },
                                onSwipeRight = {
                                    scope.launch {
                                        val feedback = repository.performRightSwipeAction(group.id, prefs.swipeRightAction)
                                        Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Pinned Group Chats Section towards the bottom for ergonomic thumb reach
                    if (searchQuery.isBlank() && pinnedGroupList.isNotEmpty()) {
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
                                    text = "PINNED GROUPS",
                                    fontSize = 11.sp,
                                    fontFamily = RobotoMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Color(0xFF71717A)
                                )
                                Text(
                                    text = "${pinnedGroupList.size}/4",
                                    fontSize = 11.sp,
                                    fontFamily = RobotoMonoFontFamily,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        item {
                            PinnedChatsGrid(
                                pinnedList = pinnedGroupList,
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

            // Selection Floating Toolbar
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
                    modifier = Modifier.fillMaxWidth()
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
                                        selectedIds.forEach { id -> repository.togglePin(id) }
                                        Toast.makeText(context, "Updated pins", Toast.LENGTH_SHORT).show()
                                        selectedIds = emptySet()
                                    }
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Default.PushPin, contentDescription = "Pin", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            }

                            // Archive
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        selectedIds.forEach { id -> repository.toggleArchive(id) }
                                        Toast.makeText(context, "Archived ${selectedIds.size} groups", Toast.LENGTH_SHORT).show()
                                        selectedIds = emptySet()
                                    }
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Default.Archive, contentDescription = "Archive", tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                            }

                            // Trash
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        selectedIds.forEach { id -> repository.moveToTrash(id) }
                                        Toast.makeText(context, "Moved to Trash", Toast.LENGTH_SHORT).show()
                                        selectedIds = emptySet()
                                    }
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Trash", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }

                            // Media
                            IconButton(
                                onClick = {
                                    selectedIds = emptySet()
                                    onNavigateToMedia()
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Default.PermMedia, contentDescription = "Media", tint = Color(0xFF06B6D4), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Group Dialog
    if (showCreateGroupDialog) {
        CreateGroupDialog(
            onDismiss = onDismissCreateGroupDialog,
            onCreateGroup = { groupName, memberList ->
                onDismissCreateGroupDialog()
                scope.launch {
                    val id = repository.createGroupConversation(groupName, memberList)
                    onConversationClick(id)
                }
            }
        )
    }
}

@Composable
fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onCreateGroup: (name: String, members: List<String>) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var memberInput by remember { mutableStateOf("") }
    val members = remember { mutableStateOf(listOf("Alex Rivera", "Taylor Morgan")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A0A0C),
        shape = RoundedCornerShape(26.dp),
        title = { Text("New Group Chat (3+)", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("group_name_input")
                )

                Text("Participants (${members.value.size + 1} including you):", fontSize = 12.sp, color = Color(0xFFA1A1AA))
                members.value.forEach { m ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF141418),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(m, color = Color.White, fontSize = 13.sp)
                            TextButton(onClick = { members.value = members.value - m }) {
                                Text("Remove", color = Color(0xFFEF4444), fontSize = 11.sp)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = memberInput,
                        onValueChange = { memberInput = it },
                        placeholder = { Text("Add participant name") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("group_member_input")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (memberInput.isNotBlank()) {
                                members.value = members.value + memberInput.trim()
                                memberInput = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (groupName.isNotBlank() && members.value.size >= 2) {
                        onCreateGroup(groupName.trim(), members.value)
                    }
                },
                enabled = groupName.isNotBlank() && members.value.size >= 2,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("confirm_create_group_btn")
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
