package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartScreen
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SwipeRightAction
import com.example.data.repository.MessagingRepository
import com.example.ui.theme.LocalThemePreferences
import com.example.ui.theme.OneUIThemeEngine
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: MessagingRepository,
    onOpenSecurityVault: () -> Unit,
    onOpenWebSync: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs by repository.themePreferences.collectAsState()

    val archivedList by repository.getArchivedConversations().collectAsState(initial = emptyList())
    val trashList by repository.getTrashConversations().collectAsState(initial = emptyList())

    var showArchiveModal by remember { mutableStateOf(false) }
    var showTrashModal by remember { mutableStateOf(false) }
    var showEmptyTrashConfirm by remember { mutableStateOf(false) }

    val accentSwatches = listOf(
        "#06B6D4", // Neon Cyan
        "#3B82F6", // Royal Blue
        "#10B981", // Emerald
        "#A855F7", // Neon Purple
        "#F97316", // Vibrant Coral
        "#EC4899"  // Hot Pink
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF000000)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF000000)),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Top left brand: KNKT • SETTINGS
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "KNKT",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• SETTINGS & MANAGEMENT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFA1A1AA)
                    )
                }
            }

            // Google Messages Web Sync & Device Pairing
            item {
                SettingsSectionTitle("GOOGLE MESSAGES WEB SYNC")
            }

            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0C)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Messages for Web",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "PLAY STORE COMPLIANT",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Access all your real SMS and RCS messages safely via official Google Messages web pairing without restricted telephony permissions.",
                                    fontSize = 12.sp,
                                    color = Color(0xFFA1A1AA),
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    launchGoogleMessagesInSystemBrowser(context)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("open_system_browser_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInBrowser,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Launch In Browser",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            OutlinedButton(
                                onClick = onOpenWebSync,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_google_web_sync_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "In-App Sync",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Features Badges row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Zero Restricted Permissions", "Live RCS & SMS", "Device QR Pairing").forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF141418),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF27272A))
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 9.sp,
                                        color = Color(0xFF71717A),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Message Management (Archive & Trash sections)
            item {
                SettingsSectionTitle("MESSAGE MANAGEMENT")
            }

            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0C)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(1.dp, Color(0xFF1E1E22), RoundedCornerShape(22.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Archive row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showArchiveModal = true }
                                .padding(vertical = 10.dp)
                                .testTag("settings_archive_entry"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Archive, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text("Archived Chats", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                                    Text("${archivedList.size} conversations hidden from main lists", fontSize = 11.sp, color = Color(0xFFA1A1AA))
                                }
                            }
                            Text("Manage", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Trash row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTrashModal = true }
                                .padding(vertical = 10.dp)
                                .testTag("settings_trash_entry"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text("Trash Bin", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                                    Text("${trashList.size} soft-deleted chats", fontSize = 11.sp, color = Color(0xFFA1A1AA))
                                }
                            }
                            Text("Manage", fontSize = 12.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Customizable Right Swipe Gesture
            item {
                Spacer(modifier = Modifier.height(18.dp))
                SettingsSectionTitle("SWIPE GESTURES")
            }

            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0C)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(1.dp, Color(0xFF1E1E22), RoundedCornerShape(22.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Gesture, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Right Swipe Action", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("Left swipe always peeks/enters the chat", fontSize = 11.sp, color = Color(0xFFA1A1AA))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        SwipeRightAction.values().forEach { action ->
                            val isSelected = prefs.swipeRightAction == action
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        repository.updateThemePreferences { it.copy(swipeRightAction = action) }
                                    }
                                    .padding(vertical = 6.dp)
                                    .testTag("swipe_action_${action.name.lowercase()}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        repository.updateThemePreferences { it.copy(swipeRightAction = action) }
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(action.displayName, color = Color.White, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    Text(action.iconDescription, color = Color(0xFF71717A), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Visual OLED Theming
            item {
                Spacer(modifier = Modifier.height(18.dp))
                SettingsSectionTitle("OLED THEME & ACCENTS")
            }

            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0C)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(1.dp, Color(0xFF1E1E22), RoundedCornerShape(22.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Material & System Accent Colors", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(accentSwatches) { hex ->
                                val color = OneUIThemeEngine.parseColor(hex, Color.Cyan)
                                val isSelected = prefs.customAccentHex.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            repository.updateThemePreferences { it.copy(customAccentHex = hex) }
                                        }
                                        .testTag("settings_accent_$hex"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // OLED Black toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pure Black OLED Backgrounds", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 13.sp)
                                Text("True #000000 black with zero gray bleeding", fontSize = 11.sp, color = Color(0xFFA1A1AA))
                            }
                            Switch(
                                checked = prefs.isOledBlack,
                                onCheckedChange = { checked ->
                                    repository.updateThemePreferences { it.copy(isOledBlack = checked) }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("oled_black_switch")
                            )
                        }
                    }
                }
            }

            // Notifications & Multi-window
            item {
                Spacer(modifier = Modifier.height(18.dp))
                SettingsSectionTitle("NOTIFICATIONS & MULTI-WINDOW")
            }

            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0C)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(1.dp, Color(0xFF1E1E22), RoundedCornerShape(22.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Quick Reply in Notifications", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 13.sp)
                                Text("Direct reply actions with inline text input", fontSize = 11.sp, color = Color(0xFFA1A1AA))
                            }
                            Switch(
                                checked = prefs.quickReplyEnabled,
                                onCheckedChange = { checked ->
                                    repository.updateThemePreferences { it.copy(quickReplyEnabled = checked) }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("quick_reply_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF06B6D4).copy(alpha = 0.15f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.SmartScreen, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("One UI Multi-Window & Pop-Up View", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 13.sp)
                                Text("Optimized for split screen, floating pop-up cards and DeX", fontSize = 11.sp, color = Color(0xFFA1A1AA))
                            }
                        }
                    }
                }
            }

            // Security Vault Entry
            item {
                Spacer(modifier = Modifier.height(18.dp))
                SettingsSectionTitle("SECURITY & ENCRYPTION")
            }

            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0C)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(1.dp, Color(0xFF1E1E22), RoundedCornerShape(22.dp))
                        .clickable(onClick = onOpenSecurityVault)
                        .testTag("open_vault_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(22.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("E2EE Cryptographic Vault", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("Curve25519 & AES-256-GCM session keys", fontSize = 11.sp, color = Color(0xFFA1A1AA))
                            }
                        }
                        Text("View", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Archive Management Modal
    if (showArchiveModal) {
        AlertDialog(
            onDismissRequest = { showArchiveModal = false },
            containerColor = Color(0xFF0A0A0C),
            shape = RoundedCornerShape(26.dp),
            title = {
                Text("Archived Conversations (${archivedList.size})", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (archivedList.isEmpty()) {
                        Text("No conversations currently archived.", color = Color(0xFFA1A1AA), fontSize = 13.sp)
                    } else {
                        archivedList.forEach { conv ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF141418), RoundedCornerShape(14.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(conv.recipientName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(conv.lastMessage, color = Color(0xFFA1A1AA), fontSize = 11.sp, maxLines = 1)
                                }
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            repository.toggleArchive(conv.id)
                                            Toast.makeText(context, "Unarchived ${conv.recipientName}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.testTag("unarchive_btn_${conv.id}")
                                ) {
                                    Icon(Icons.Default.Unarchive, contentDescription = "Unarchive", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showArchiveModal = false }) { Text("Close") }
            }
        )
    }

    // Trash Management Modal
    if (showTrashModal) {
        AlertDialog(
            onDismissRequest = { showTrashModal = false },
            containerColor = Color(0xFF0A0A0C),
            shape = RoundedCornerShape(26.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Trash Bin (${trashList.size})", fontWeight = FontWeight.Bold, color = Color.White)
                    if (trashList.isNotEmpty()) {
                        TextButton(onClick = { showEmptyTrashConfirm = true }) {
                            Text("Empty", color = Color(0xFFEF4444), fontSize = 12.sp)
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (trashList.isEmpty()) {
                        Text("Trash is empty.", color = Color(0xFFA1A1AA), fontSize = 13.sp)
                    } else {
                        trashList.forEach { conv ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF141418), RoundedCornerShape(14.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(conv.recipientName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(conv.lastMessage, color = Color(0xFFA1A1AA), fontSize = 11.sp, maxLines = 1)
                                }
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            repository.restoreFromTrash(conv.id)
                                            Toast.makeText(context, "Restored ${conv.recipientName}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.testTag("restore_trash_btn_${conv.id}")
                                ) {
                                    Icon(Icons.Default.Restore, contentDescription = "Restore", tint = Color(0xFF10B981))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTrashModal = false }) { Text("Close") }
            }
        )
    }

    // Empty Trash confirmation
    if (showEmptyTrashConfirm) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashConfirm = false },
            containerColor = Color(0xFF0A0A0C),
            shape = RoundedCornerShape(24.dp),
            title = { Text("Permanently Empty Trash?", fontWeight = FontWeight.Bold, color = Color.White) },
            text = { Text("All conversations in Trash will be permanently deleted and cannot be recovered.", color = Color(0xFFD4D4D8)) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch { repository.emptyTrash() }
                        showEmptyTrashConfirm = false
                        showTrashModal = false
                        Toast.makeText(context, "Trash emptied", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("confirm_empty_trash_btn")
                ) {
                    Text("Empty Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = Color(0xFF71717A),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    )
}
