package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ConversationEntity
import com.example.data.model.MessageProtocol
import com.example.data.model.SwipeRightAction
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.util.HapticHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SwipeableConversationCard(
    conversation: ConversationEntity,
    swipeRightAction: SwipeRightAction,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSwipeLeftPeek: (dragProgress: Float) -> Unit = {},
    onSwipeLeftComplete: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    val protocol = remember(conversation.activeProtocol) {
        try { MessageProtocol.valueOf(conversation.activeProtocol) } catch (e: Exception) { MessageProtocol.RCS }
    }
    val timeFormatted = remember(conversation.lastTimestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(conversation.lastTimestamp))
    }

    // Minimal, unintrusive protocol indicator color pip
    val minimalProtocolPipColor = remember(protocol, conversation.isE2EE) {
        when {
            conversation.isE2EE -> Color(0xFF06B6D4) // Glowing Neon Cyan
            protocol == MessageProtocol.MMS -> Color(0xFF0284C7) // Sky
            else -> Color(0xFF64748B) // Slate Gray
        }
    }

    val (rightActionIcon, rightActionColor, rightActionLabel) = when (swipeRightAction) {
        SwipeRightAction.MARK_READ_UNREAD -> Triple(
            if (conversation.unreadCount > 0) Icons.Default.MarkEmailRead else Icons.Default.MarkEmailUnread,
            Color(0xFF38BDF8),
            if (conversation.unreadCount > 0) "Mark Read" else "Mark Unread"
        )
        SwipeRightAction.ARCHIVE -> Triple(Icons.Default.Archive, Color(0xFFF59E0B), "Archive")
        SwipeRightAction.TRASH -> Triple(Icons.Default.Delete, Color(0xFFEF4444), "Trash")
        SwipeRightAction.PIN -> Triple(Icons.Default.PushPin, Color(0xFF10B981), if (conversation.isPinned) "Unpin" else "Pin")
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("swipeable_conv_${conversation.id}")
    ) {
        // Background swipe action reveals
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF000000),
            modifier = Modifier.matchParentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Right swipe action icon (revealed on dragging right)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = rightActionIcon,
                        contentDescription = rightActionLabel,
                        tint = rightActionColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = rightActionLabel,
                        color = rightActionColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Left swipe action icon (revealed on dragging left -> Peek / Enter)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Peek",
                        color = Color(0xFF06B6D4),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Peek / Enter Chat",
                        tint = Color(0xFF06B6D4),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Foreground Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) Color(0xFF131722) else Color(0xFF0A0A0C)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1E1E22),
                    shape = RoundedCornerShape(22.dp)
                )
                .draggable(
                    enabled = !isSelectionMode,
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            val newX = offsetX.value + delta
                            offsetX.snapTo(newX)
                            if (newX < 0) {
                                onSwipeLeftPeek(-newX / 600f)
                            }
                        }
                    },
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        val current = offsetX.value
                        scope.launch {
                            if (current < -150f) {
                                // Swept left far enough to trigger peek / enter!
                                HapticHelper.gestureSwipe(context)
                                offsetX.animateTo(0f, spring())
                                onSwipeLeftComplete()
                            } else if (current > 130f) {
                                // Swept right to trigger custom action
                                HapticHelper.gestureSwipe(context)
                                offsetX.animateTo(0f, spring())
                                onSwipeRight()
                            } else {
                                offsetX.animateTo(0f, spring())
                            }
                        }
                    }
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        HapticHelper.longPress(context)
                        onLongClick()
                    }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar with Selection Checkbox overlay
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        shape = CircleShape,
                        color = Color(conversation.avatarColorHex),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val initials = if (conversation.isGroup) {
                                "👥"
                            } else {
                                conversation.recipientName.split(" ")
                                    .mapNotNull { it.firstOrNull()?.toString() }
                                    .take(2)
                                    .joinToString("")
                                    .uppercase()
                            }
                            Text(
                                text = if (initials.isNotEmpty()) initials else "💬",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (conversation.isGroup) 16.sp else 15.sp
                            )
                        }
                    }

                    if (isSelected) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.BottomEnd)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = conversation.recipientName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Time and minimal unintrusive colored protocol dot pip
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Minimal unintrusive dot indicator! No bulky text/badges
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(minimalProtocolPipColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = timeFormatted,
                                fontFamily = RobotoMonoFontFamily,
                                fontSize = 11.sp,
                                color = if (conversation.unreadCount > 0) MaterialTheme.colorScheme.primary else Color(0xFFA1A1AA)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = conversation.lastMessage,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (conversation.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (conversation.unreadCount > 0) Color(0xFFFFFFFF) else Color(0xFFA1A1AA),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (conversation.unreadCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
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
    }
}
