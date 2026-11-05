package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ConversationEntity
import com.example.data.model.MessageProtocol
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.util.HapticHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PinnedChatsGrid(
    pinnedList: List<ConversationEntity>,
    onConversationClick: (Long) -> Unit,
    onConversationLongClick: ((Long) -> Unit)? = null,
    selectedIds: Set<Long> = emptySet(),
    modifier: Modifier = Modifier
) {
    if (pinnedList.isEmpty()) return
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("pinned_chats_grid")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "PINNED (${pinnedList.size}/4)",
                fontSize = 11.sp,
                fontFamily = RobotoMonoFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        when (pinnedList.size) {
            1 -> {
                // 1 Pin: Full-width Banner card with larger profile picture and more message content
                val conv = pinnedList[0]
                PinnedBannerCard(
                    conversation = conv,
                    isSelected = conv.id in selectedIds,
                    onClick = { onConversationClick(conv.id) },
                    onLongClick = {
                        HapticHelper.longPress(context)
                        onConversationLongClick?.invoke(conv.id)
                    }
                )
            }
            2 -> {
                // 2 Pins: Side-by-side cards (cut in half)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        val conv = pinnedList[0]
                        PinnedCompactCard(
                            conversation = conv,
                            isSelected = conv.id in selectedIds,
                            onClick = { onConversationClick(conv.id) },
                            onLongClick = {
                                HapticHelper.longPress(context)
                                onConversationLongClick?.invoke(conv.id)
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        val conv = pinnedList[1]
                        PinnedCompactCard(
                            conversation = conv,
                            isSelected = conv.id in selectedIds,
                            onClick = { onConversationClick(conv.id) },
                            onLongClick = {
                                HapticHelper.longPress(context)
                                onConversationLongClick?.invoke(conv.id)
                            }
                        )
                    }
                }
            }
            3 -> {
                // 3 Pins: Top row of 2 side-by-side, bottom row with 3rd card
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            val conv = pinnedList[0]
                            PinnedCompactCard(
                                conversation = conv,
                                isSelected = conv.id in selectedIds,
                                onClick = { onConversationClick(conv.id) },
                                onLongClick = {
                                    HapticHelper.longPress(context)
                                    onConversationLongClick?.invoke(conv.id)
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            val conv = pinnedList[1]
                            PinnedCompactCard(
                                conversation = conv,
                                isSelected = conv.id in selectedIds,
                                onClick = { onConversationClick(conv.id) },
                                onLongClick = {
                                    HapticHelper.longPress(context)
                                    onConversationLongClick?.invoke(conv.id)
                                }
                            )
                        }
                    }
                    val conv = pinnedList[2]
                    PinnedCompactCard(
                        conversation = conv,
                        isSelected = conv.id in selectedIds,
                        onClick = { onConversationClick(conv.id) },
                        onLongClick = {
                            HapticHelper.longPress(context)
                            onConversationLongClick?.invoke(conv.id)
                        }
                    )
                }
            }
            else -> {
                // 4 Pins: 2x2 grid (two rows of 2 cards each)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            val conv = pinnedList[0]
                            PinnedCompactCard(
                                conversation = conv,
                                isSelected = conv.id in selectedIds,
                                onClick = { onConversationClick(conv.id) },
                                onLongClick = {
                                    HapticHelper.longPress(context)
                                    onConversationLongClick?.invoke(conv.id)
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            val conv = pinnedList[1]
                            PinnedCompactCard(
                                conversation = conv,
                                isSelected = conv.id in selectedIds,
                                onClick = { onConversationClick(conv.id) },
                                onLongClick = {
                                    HapticHelper.longPress(context)
                                    onConversationLongClick?.invoke(conv.id)
                                }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            val conv = pinnedList[2]
                            PinnedCompactCard(
                                conversation = conv,
                                isSelected = conv.id in selectedIds,
                                onClick = { onConversationClick(conv.id) },
                                onLongClick = {
                                    HapticHelper.longPress(context)
                                    onConversationLongClick?.invoke(conv.id)
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            val conv = pinnedList[3]
                            PinnedCompactCard(
                                conversation = conv,
                                isSelected = conv.id in selectedIds,
                                onClick = { onConversationClick(conv.id) },
                                onLongClick = {
                                    HapticHelper.longPress(context)
                                    onConversationLongClick?.invoke(conv.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PinnedBannerCard(
    conversation: ConversationEntity,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val protocol = remember(conversation.activeProtocol) {
        try { MessageProtocol.valueOf(conversation.activeProtocol) } catch (_: Exception) { MessageProtocol.RCS }
    }
    val timeFormatted = remember(conversation.lastTimestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(conversation.lastTimestamp))
    }

    val minimalPipColor = when {
        conversation.isE2EE -> Color(0xFF06B6D4)
        protocol == MessageProtocol.MMS -> Color(0xFF0284C7)
        else -> Color(0xFF64748B)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF141418) else Color(0xFF0A0A0C)),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF222226),
                shape = RoundedCornerShape(24.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("pinned_banner_card_${conversation.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Larger profile picture banner avatar (62dp)
            Surface(
                shape = CircleShape,
                color = Color(conversation.avatarColorHex),
                modifier = Modifier.size(62.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val initials = conversation.recipientName.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                        .uppercase()
                    Text(
                        text = if (initials.isNotEmpty()) initials else "💬",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = conversation.recipientName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(5.dp).background(minimalPipColor, CircleShape))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = timeFormatted,
                            fontFamily = RobotoMonoFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Expanded message preview
                Text(
                    text = conversation.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFD4D4D8),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (conversation.unreadCount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = conversation.unreadCount.toString(),
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PinnedCompactCard(
    conversation: ConversationEntity,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val protocol = remember(conversation.activeProtocol) {
        try { MessageProtocol.valueOf(conversation.activeProtocol) } catch (_: Exception) { MessageProtocol.RCS }
    }
    val timeFormatted = remember(conversation.lastTimestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(conversation.lastTimestamp))
    }

    val minimalPipColor = when {
        conversation.isE2EE -> Color(0xFF06B6D4)
        protocol == MessageProtocol.MMS -> Color(0xFF0284C7)
        else -> Color(0xFF64748B)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF141418) else Color(0xFF0A0A0C)),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF222226),
                shape = RoundedCornerShape(20.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("pinned_compact_card_${conversation.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(conversation.avatarColorHex),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val initials = conversation.recipientName.split(" ")
                            .mapNotNull { it.firstOrNull()?.toString() }
                            .take(2)
                            .joinToString("")
                            .uppercase()
                        Text(
                            text = initials,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = conversation.recipientName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(4.dp).background(minimalPipColor, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = timeFormatted,
                            fontFamily = RobotoMonoFontFamily,
                            fontSize = 10.sp,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                }

                if (conversation.unreadCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = conversation.unreadCount.toString(),
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = conversation.lastMessage,
                fontSize = 12.sp,
                color = Color(0xFFD4D4D8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
