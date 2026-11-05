package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MessageEntity
import com.example.data.repository.MessagingRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MediaFilterType(val label: String) {
    ALL("All Media"),
    PHOTOS("Photos 📷"),
    AUDIO("Voice 🎤"),
    LOCATIONS("Locations 📍"),
    DOCUMENTS("Documents 📄")
}

@Composable
fun MediaBrowserScreen(
    repository: MessagingRepository,
    onShowInChat: (conversationId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(MediaFilterType.ALL) }
    val allMedia by repository.getAllMediaMessages().collectAsState(initial = emptyList())

    val filteredMedia = remember(allMedia, selectedFilter) {
        when (selectedFilter) {
            MediaFilterType.ALL -> allMedia
            MediaFilterType.PHOTOS -> allMedia.filter { it.attachmentType == "IMAGE" }
            MediaFilterType.AUDIO -> allMedia.filter { it.attachmentType == "AUDIO" }
            MediaFilterType.LOCATIONS -> allMedia.filter { it.attachmentType == "LOCATION" }
            MediaFilterType.DOCUMENTS -> allMedia.filter { it.attachmentType == "FILE" || it.attachmentType == "LINK" }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF000000)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF000000))
        ) {
            // Top left brand: KNKT • MEDIA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "KNKT",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• MEDIA HUB",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFA1A1AA)
                    )
                }

                Text(
                    text = "${allMedia.size} items",
                    fontSize = 11.sp,
                    color = Color(0xFF71717A)
                )
            }

            // Filter Pills
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(MediaFilterType.values()) { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color(0xFF0A0A0C),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF222226)
                        ),
                        modifier = Modifier
                            .clickable { selectedFilter = filter }
                            .testTag("filter_${filter.name.lowercase()}")
                    ) {
                        Text(
                            text = filter.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFA1A1AA),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Media items list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredMedia.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.PermMedia,
                                    contentDescription = null,
                                    tint = Color(0xFF3F3F46),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No shared media found in this category",
                                    color = Color(0xFFA1A1AA),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    items(filteredMedia, key = { it.id }) { msg ->
                        MediaCardItem(
                            message = msg,
                            onShowInChat = { onShowInChat(msg.conversationId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaCardItem(
    message: MessageEntity,
    onShowInChat: () -> Unit
) {
    val timeFormatted = remember(message.timestamp) {
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0C)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E1E22), RoundedCornerShape(20.dp))
            .testTag("media_item_${message.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Media Type Icon / Thumbnail
            val (icon, iconColor, typeLabel) = when (message.attachmentType) {
                "IMAGE" -> Triple(Icons.Default.Image, Color(0xFF06B6D4), "Photo")
                "AUDIO" -> Triple(Icons.Default.Mic, Color(0xFFEF4444), "Voice Memo")
                "LOCATION" -> Triple(Icons.Default.LocationOn, Color(0xFFF59E0B), "Location")
                else -> Triple(Icons.Default.Description, Color(0xFFA855F7), "Document")
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = typeLabel,
                        tint = iconColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message.attachmentTitle ?: message.text,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "From: ${message.sender} • $timeFormatted",
                    fontSize = 11.sp,
                    color = Color(0xFFA1A1AA)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$typeLabel • ${message.protocol} ${if (message.isEncrypted) "E2EE 🔒" else ""}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = iconColor
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Show in chat action button
            OutlinedButton(
                onClick = onShowInChat,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("show_in_chat_btn_${message.id}")
            ) {
                Text("Show in Chat", fontSize = 11.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
