package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.model.BubbleShapeType
import com.example.data.model.MessageProtocol
import com.example.data.repository.MessagingRepository
import com.example.ui.components.AttachmentPickerSheet
import com.example.ui.components.CryptoInspectorDialog
import com.example.ui.components.ReactionSelector
import com.example.ui.components.SafetyNumberDialog
import com.example.ui.theme.LocalThemePreferences
import com.example.ui.theme.OneUIThemeEngine
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.util.HapticHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatThreadScreen(
    conversationId: Long,
    repository: MessagingRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = LocalThemePreferences.current

    val conversation by repository.getConversationFlow(conversationId).collectAsState(initial = null)
    val messages by repository.getMessages(conversationId).collectAsState(initial = emptyList())
    val typingStatusMap by repository.typingStatus.collectAsState()
    val typingName = typingStatusMap[conversationId]

    var inputText by remember { mutableStateOf("") }
    var activeProtocol by remember { mutableStateOf(MessageProtocol.RCS) }
    var isE2EEActive by remember { mutableStateOf(true) }

    // Bottom toolbar bubble expandable state
    var isToolbarBubbleExpanded by remember { mutableStateOf(false) }

    var showCustomizerSheet by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showSafetyNumberDialog by remember { mutableStateOf(false) }
    var inspectingMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var reactingToMessageId by remember { mutableStateOf<Long?>(null) }

    val listState = rememberLazyListState()

    // Sliding window progress: 0f = chat visible, 1f = media window covers screen
    val mediaSlideProgress = remember { Animatable(0f) }
    // Thread peek back offset: dragging right peeks back to thread list
    val threadBackOffsetX = remember { Animatable(0f) }

    LaunchedEffect(conversation) {
        conversation?.let { conv ->
            repository.markConversationRead(conv.id)
            activeProtocol = try {
                MessageProtocol.valueOf(conv.activeProtocol)
            } catch (e: Exception) {
                MessageProtocol.RCS
            }
            isE2EEActive = conv.isE2EE
        }
    }

    LaunchedEffect(messages.size, typingName) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // Parse group bubble config (stores "Alex Rivera|0xFF06B6D4|ROUNDED;...")
    val groupBubbleMap = remember(conversation?.groupBubbleConfigJson) {
        val map = mutableMapOf<String, Pair<Long, BubbleShapeType>>()
        conversation?.groupBubbleConfigJson?.split(";")?.forEach { entry ->
            val parts = entry.split("|")
            if (parts.size >= 3) {
                val memberName = parts[0]
                val colorHex = parts[1].removePrefix("0x").removePrefix("#").toLongOrNull(16) ?: 0xFF06B6D4
                val shape = try { BubbleShapeType.valueOf(parts[2]) } catch (e: Exception) { BubbleShapeType.ROUNDED }
                map[memberName] = Pair(colorHex, shape)
            }
        }
        map
    }

    // CMD Terminal Blinking Cursor animation (blinks every 500ms)
    val infiniteTransition = rememberInfiniteTransition(label = "cmd_cursor")
    val cursorVisible by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_blink"
    )

    // Minimal status pip color for the active conversation
    val minimalProtocolPipColor = remember(activeProtocol, isE2EEActive) {
        when {
            isE2EEActive -> Color(0xFF06B6D4) // Cyan (E2EE)
            activeProtocol == MessageProtocol.MMS -> Color(0xFF0284C7)
            else -> Color(0xFF64748B) // Slate Gray
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        val screenWidthPx = constraints.maxWidth.toFloat()

        BackHandler(enabled = true) {
            if (mediaSlideProgress.value > 0.05f) {
                scope.launch {
                    HapticHelper.gestureSwipe(context)
                    mediaSlideProgress.animateTo(0f, spring(dampingRatio = 0.85f))
                }
            } else {
                onBackClick()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(threadBackOffsetX.value.roundToInt(), 0) }
                .draggable(
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            if (delta > 0) {
                                // Dragging right -> peeking back to thread list
                                val newX = (threadBackOffsetX.value + delta).coerceAtLeast(0f)
                                threadBackOffsetX.snapTo(newX)
                            } else if (delta < 0) {
                                // Dragging left -> peeking the media page window!
                                val newProgress = (mediaSlideProgress.value + (-delta / screenWidthPx)).coerceIn(0f, 1f)
                                mediaSlideProgress.snapTo(newProgress)
                            }
                        }
                    },
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        val currentBack = threadBackOffsetX.value
                        val currentMedia = mediaSlideProgress.value
                        scope.launch {
                            if (currentBack > 150f) {
                                HapticHelper.gestureSwipe(context)
                                threadBackOffsetX.animateTo(screenWidthPx, spring())
                                onBackClick()
                            } else if (currentBack > 0f) {
                                threadBackOffsetX.animateTo(0f, spring())
                            } else if (currentMedia > 0.25f) {
                                HapticHelper.gestureSwipe(context)
                                mediaSlideProgress.animateTo(1f, spring(dampingRatio = 0.85f))
                            } else {
                                mediaSlideProgress.animateTo(0f, spring(dampingRatio = 0.85f))
                            }
                        }
                    }
                )
        ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color(0xFF000000), // Pure OLED Black
            topBar = {
                // Top bar: clean and minimal, without KNKT at top right
                Surface(
                    color = Color(0xFF000000),
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(conversation?.avatarColorHex ?: 0xFF06B6D4),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (conversation?.isGroup == true) "👥"
                                        else conversation?.recipientName?.firstOrNull()?.toString()?.uppercase() ?: "K",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = conversation?.recipientName ?: "Chat",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isE2EEActive) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = "E2EE Encrypted",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Minimal unintrusive protocol indicator pip
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(minimalProtocolPipColor)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (typingName != null) "$typingName is typing..."
                                        else "${activeProtocol.name} • ${if (conversation?.isGroup == true) "${conversation?.groupParticipants?.split(",")?.size ?: 3} members" else conversation?.recipientPhone ?: ""}",
                                        fontSize = 11.sp,
                                        color = if (typingName != null) MaterialTheme.colorScheme.primary else Color(0xFFA1A1AA)
                                    )
                                }
                            }
                        }

                        // Right actions: quick shared media access button
                        IconButton(
                            onClick = {
                                scope.launch {
                                    HapticHelper.gestureSwipe(context)
                                    mediaSlideProgress.animateTo(1f, spring(dampingRatio = 0.85f))
                                }
                            },
                            modifier = Modifier.size(32.dp).testTag("chat_top_media_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PermMedia,
                                contentDescription = "Shared Media",
                                tint = Color(0xFF06B6D4),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            },
            bottomBar = {
                // Bottom Section: Expandable Toolbar Bubble Just Above Text Box, followed ONLY by Text Field!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .background(Color(0xFF000000))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    // Toolbar Bubble Just Above the Text Box
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF0D0D10),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .padding(bottom = 8.dp)
                            .testTag("chat_bottom_toolbar_bubble")
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp)) {
                            // Header of toolbar bubble: shows minimal protocol pip + tap to expand/collapse
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isToolbarBubbleExpanded = !isToolbarBubbleExpanded }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(minimalProtocolPipColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${activeProtocol.name}${if (isE2EEActive) " • E2EE" else ""}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = minimalProtocolPipColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isToolbarBubbleExpanded) "Tap to collapse" else "Tap for options",
                                        fontSize = 10.sp,
                                        color = Color(0xFF71717A)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isToolbarBubbleExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle Toolbar",
                                        tint = Color(0xFFA1A1AA),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Expanded options: Customize, Add Media +, Voice Mic, Protocol switchers, E2EE
                            AnimatedVisibility(
                                visible = isToolbarBubbleExpanded,
                                enter = fadeIn() + slideInVertically { -it / 3 },
                                exit = fadeOut() + slideOutVertically { -it / 3 }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 4.dp)
                                ) {
                                    // Minimal protocol switcher chips
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            MessageProtocol.values().forEach { proto ->
                                                val isSelected = activeProtocol == proto
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = if (isSelected) Color(proto.badgeColor).copy(alpha = 0.25f) else Color(0xFF141418),
                                                    modifier = Modifier
                                                        .clickable {
                                                            activeProtocol = proto
                                                            if (proto != MessageProtocol.RCS) isE2EEActive = false
                                                            conversation?.let {
                                                                scope.launch {
                                                                    repository.updateProtocol(it.id, proto)
                                                                    if (proto != MessageProtocol.RCS) repository.updateE2EE(it.id, false)
                                                                }
                                                            }
                                                        }
                                                        .testTag("proto_chip_${proto.name.lowercase()}")
                                                ) {
                                                    Text(
                                                        text = proto.name,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) Color(proto.badgeColor) else Color(0xFFA1A1AA),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // E2EE Lock Toggle
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isE2EEActive) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF141418),
                                            modifier = Modifier.clickable {
                                                if (activeProtocol == MessageProtocol.RCS) {
                                                    val next = !isE2EEActive
                                                    isE2EEActive = next
                                                    conversation?.let { scope.launch { repository.updateE2EE(it.id, next) } }
                                                } else {
                                                    Toast.makeText(context, "E2EE requires RCS", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isE2EEActive) Icons.Default.Lock else Icons.Default.LockOpen,
                                                    contentDescription = "Toggle E2EE",
                                                    tint = if (isE2EEActive) Color(0xFF10B981) else Color.Gray,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isE2EEActive) "E2EE" else "Unencrypted",
                                                    fontSize = 10.sp,
                                                    color = if (isE2EEActive) Color(0xFF10B981) else Color.Gray,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Action buttons: Customize Theme, Add Media +, Voice Mic, Carrier SMS
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Customize Button
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier
                                                .clickable { showCustomizerSheet = true }
                                                .testTag("toolbar_customize_btn")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Palette,
                                                    contentDescription = "Customize",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Customize",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            // Plus Button to add media
                                            IconButton(
                                                onClick = { showAttachmentSheet = true },
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .background(Color(0xFF18181B), CircleShape)
                                                    .border(1.dp, Color(0xFF27272A), CircleShape)
                                                    .testTag("toolbar_attach_media_btn")
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Add Media", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            }

                                            // Microphone for audio messages
                                            IconButton(
                                                onClick = {
                                                    conversation?.let { conv ->
                                                        scope.launch {
                                                            repository.sendMessage(
                                                                conversationId = conv.id,
                                                                text = "Voice Memo (0:14)",
                                                                protocol = activeProtocol,
                                                                isEncrypted = isE2EEActive,
                                                                attachmentType = "AUDIO",
                                                                attachmentTitle = "Voice Memo (0:14)"
                                                            )
                                                            HapticHelper.messageSend(context)
                                                            Toast.makeText(context, "Voice memo sent", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .background(Color(0xFF18181B), CircleShape)
                                                    .border(1.dp, Color(0xFF27272A), CircleShape)
                                                    .testTag("toolbar_mic_voice_btn")
                                            ) {
                                                Icon(Icons.Default.Mic, contentDescription = "Voice Memo", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            }

                                            // Shared Media in this chat button
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        HapticHelper.gestureSwipe(context)
                                                        mediaSlideProgress.animateTo(1f, spring(dampingRatio = 0.85f))
                                                    }
                                                },
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .background(Color(0xFF18181B), CircleShape)
                                                    .border(1.dp, Color(0xFF27272A), CircleShape)
                                            ) {
                                                Icon(Icons.Default.PermMedia, contentDescription = "Shared Media", tint = Color(0xFF06B6D4), modifier = Modifier.size(16.dp))
                                            }

                                            // System SMS App Hand-off
                                            IconButton(
                                                onClick = {
                                                    val phone = conversation?.recipientPhone ?: ""
                                                    val sendIntent = Intent(Intent.ACTION_SENDTO).apply {
                                                        data = Uri.parse("smsto:$phone")
                                                        putExtra("sms_body", inputText)
                                                    }
                                                    try { context.startActivity(sendIntent) } catch (e: Exception) {
                                                        Toast.makeText(context, "No system SMS app found", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .background(Color(0xFF18181B), CircleShape)
                                                    .border(1.dp, Color(0xFF27272A), CircleShape)
                                            ) {
                                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open Carrier SMS", tint = Color(0xFFA1A1AA), modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Just below it: ONLY the text field with blinking CMD/terminal cursor
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xFF0A0A0D),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else Color(0xFF27272A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Terminal Prompt Symbol `>`
                            Text(
                                text = "> ",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Terminal style input text area with blinking CMD block cursor `▌`
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_input_text_field"),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    cursorBrush = SolidColor(Color.Transparent), // Hide default cursor, use CMD cursor
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(
                                        onSend = {
                                            if (inputText.isNotBlank()) {
                                                val toSend = inputText.trim()
                                                inputText = ""
                                                HapticHelper.messageSend(context)
                                                conversation?.let { conv ->
                                                    scope.launch {
                                                        repository.sendMessage(
                                                            conversationId = conv.id,
                                                            text = toSend,
                                                            protocol = activeProtocol,
                                                            isEncrypted = isE2EEActive
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (inputText.isEmpty()) {
                                                Text(
                                                    text = when (activeProtocol) {
                                                        MessageProtocol.RCS -> if (isE2EEActive) "Encrypted message..." else "RCS message..."
                                                        MessageProtocol.SMS -> "Text message (SMS)..."
                                                        MessageProtocol.MMS -> "Multimedia message..."
                                                    },
                                                    fontSize = 13.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color(0xFF52525B)
                                                )
                                            } else {
                                                innerTextField()
                                            }

                                            // CMD Terminal Blinking Cursor `▌`
                                            Text(
                                                text = if (cursorVisible > 0.5f) "▌" else " ",
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.testTag("cmd_blinking_cursor")
                                            )
                                        }
                                    }
                                )
                            }

                            // Send Button
                            if (inputText.isNotBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        val toSend = inputText.trim()
                                        inputText = ""
                                        HapticHelper.messageSend(context)
                                        conversation?.let { conv ->
                                            scope.launch {
                                                repository.sendMessage(
                                                    conversationId = conv.id,
                                                    text = toSend,
                                                    protocol = activeProtocol,
                                                    isEncrypted = isE2EEActive
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        .testTag("chat_send_action_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFF000000))
            ) {
                LazyColumn(
                    state = listState,
                    reverseLayout = true,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (typingName != null) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF121216),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A)),
                                modifier = Modifier.testTag("typing_indicator_bubble")
                            ) {
                                Text(
                                    text = "$typingName is typing...",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    items(messages.reversed(), key = { it.id }) { msg ->
                        // Determine bubble color and shape per message
                        val (bubbleColor, bubbleShape) = if (msg.isFromUser) {
                            val color = conversation?.customUserBubbleColor?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
                            val shapeType = conversation?.customUserBubbleShape?.let {
                                try { BubbleShapeType.valueOf(it) } catch (e: Exception) { BubbleShapeType.ROUNDED }
                            } ?: BubbleShapeType.ROUNDED
                            Pair(color, OneUIThemeEngine.getBubbleShape(shapeType, true, prefs.squircleRadiusDp))
                        } else {
                            if (conversation?.isGroup == true && groupBubbleMap.containsKey(msg.sender)) {
                                // In group chat: customize for each participant!
                                val (cHex, sType) = groupBubbleMap[msg.sender]!!
                                Pair(Color(cHex), OneUIThemeEngine.getBubbleShape(sType, false, prefs.squircleRadiusDp))
                            } else {
                                val color = conversation?.customPeerBubbleColor?.let { Color(it) } ?: Color(0xFF18181B)
                                val shapeType = conversation?.customPeerBubbleShape?.let {
                                    try { BubbleShapeType.valueOf(it) } catch (e: Exception) { BubbleShapeType.ROUNDED }
                                } ?: BubbleShapeType.ROUNDED
                                Pair(color, OneUIThemeEngine.getBubbleShape(shapeType, false, prefs.squircleRadiusDp))
                            }
                        }

                        // Best accessibility text color based on bubble luminance
                        val textColor = if (bubbleColor.luminance() > 0.45f) Color(0xFF000000) else Color(0xFFFFFFFF)

                        KNKTMessageBubble(
                            message = msg,
                            isFromUser = msg.isFromUser,
                            isGroup = conversation?.isGroup == true,
                            bubbleColor = bubbleColor,
                            bubbleShape = bubbleShape,
                            textColor = textColor,
                            onInspectCrypto = { inspectingMessage = msg },
                            onReact = { reactingToMessageId = msg.id }
                        )
                    }
                }
            }
        }
    }

    // Dedicated In-Thread Media Sliding Window
        if (mediaSlideProgress.value > 0.001f && conversation != null) {
            val mediaOffsetX = screenWidthPx * (1f - mediaSlideProgress.value)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(mediaOffsetX.roundToInt(), 0) }
                    .background(Color(0xFF000000))
                    .draggable(
                        state = rememberDraggableState { delta ->
                            scope.launch {
                                if (delta > 0) {
                                    // Dragging right -> slide back / dismiss media window!
                                    val newProgress = (mediaSlideProgress.value - (delta / screenWidthPx)).coerceIn(0f, 1f)
                                    mediaSlideProgress.snapTo(newProgress)
                                }
                            }
                        },
                        orientation = Orientation.Horizontal,
                        onDragStopped = {
                            val currentProgress = mediaSlideProgress.value
                            scope.launch {
                                if (currentProgress < 0.75f) {
                                    HapticHelper.gestureSwipe(context)
                                    mediaSlideProgress.animateTo(0f, spring(dampingRatio = 0.85f))
                                } else {
                                    mediaSlideProgress.animateTo(1f, spring(dampingRatio = 0.85f))
                                }
                            }
                        }
                    )
            ) {
                ThreadMediaWindow(
                    conversation = conversation!!,
                    messages = messages.filter { it.attachmentType != null },
                    onBackToChat = {
                        scope.launch {
                            HapticHelper.gestureSwipe(context)
                            mediaSlideProgress.animateTo(0f, spring(dampingRatio = 0.85f))
                        }
                    }
                )
            }
        }

        // Reaction Selector
    val activeReactingMsgId = reactingToMessageId
    if (activeReactingMsgId != null) {
        AlertDialog(
            onDismissRequest = { reactingToMessageId = null },
            containerColor = Color(0xFF0A0A0C),
            shape = RoundedCornerShape(24.dp),
            title = { Text("React", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                ReactionSelector(
                    onSelectReaction = { reaction ->
                        scope.launch { repository.updateReaction(activeReactingMsgId, reaction) }
                        reactingToMessageId = null
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { reactingToMessageId = null }) { Text("Close") }
            }
        )
    }

    // Attachment Picker
    if (showAttachmentSheet) {
        AttachmentPickerSheet(
            onDismiss = { showAttachmentSheet = false },
            onAttachPhoto = { uri, title ->
                conversation?.let { conv ->
                    scope.launch {
                        repository.sendMessage(
                            conversationId = conv.id,
                            text = title,
                            protocol = if (activeProtocol == MessageProtocol.SMS) MessageProtocol.MMS else activeProtocol,
                            isEncrypted = isE2EEActive,
                            attachmentType = "IMAGE",
                            attachmentTitle = title,
                            attachmentUri = uri
                        )
                    }
                }
            },
            onAttachAudio = { title ->
                conversation?.let { conv ->
                    scope.launch {
                        repository.sendMessage(
                            conversationId = conv.id,
                            text = title,
                            protocol = activeProtocol,
                            isEncrypted = isE2EEActive,
                            attachmentType = "AUDIO",
                            attachmentTitle = title
                        )
                    }
                }
            },
            onAttachLocation = { title ->
                conversation?.let { conv ->
                    scope.launch {
                        repository.sendMessage(
                            conversationId = conv.id,
                            text = title,
                            protocol = activeProtocol,
                            isEncrypted = isE2EEActive,
                            attachmentType = "LOCATION",
                            attachmentTitle = title
                        )
                    }
                }
            },
            onAttachContact = { name ->
                conversation?.let { conv ->
                    scope.launch {
                        repository.sendMessage(
                            conversationId = conv.id,
                            text = name,
                            protocol = activeProtocol,
                            isEncrypted = isE2EEActive,
                            attachmentType = "CONTACT",
                            attachmentTitle = name
                        )
                    }
                }
            }
        )
    }

    // Thread & Bubble Customizer Sheet
    if (showCustomizerSheet && conversation != null) {
        ThreadCustomizerSheet(
            conversation = conversation!!,
            onDismiss = { showCustomizerSheet = false },
            onSaveTheming = { userColor, peerColor, userShape, peerShape, bgUri, groupConfig ->
                scope.launch {
                    repository.updateCustomTheming(
                        conversationId = conversation!!.id,
                        userColor = userColor,
                        peerColor = peerColor,
                        userShape = userShape,
                        peerShape = peerShape,
                        bgUri = bgUri,
                        groupConfigJson = groupConfig
                    )
                }
                showCustomizerSheet = false
                Toast.makeText(context, "Thread theme updated", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Crypto Packet Inspector
    inspectingMessage?.let { msg ->
        CryptoInspectorDialog(
            message = msg,
            contactName = conversation?.recipientName ?: "Contact",
            onDismiss = { inspectingMessage = null },
            onVerifyContactClick = { showSafetyNumberDialog = true }
        )
    }

    // Safety Numbers
    if (showSafetyNumberDialog && conversation != null) {
        SafetyNumberDialog(
            contactName = conversation!!.recipientName,
            contactPhone = conversation!!.recipientPhone,
            isCurrentlyVerified = true,
            onDismiss = { showSafetyNumberDialog = false },
            onVerificationChanged = { verified ->
                scope.launch { repository.verifyContact(conversation!!.recipientPhone, verified) }
            }
        )
    }
    }
}

@Composable
private fun KNKTMessageBubble(
    message: MessageEntity,
    isFromUser: Boolean,
    isGroup: Boolean,
    bubbleColor: Color,
    bubbleShape: androidx.compose.ui.graphics.Shape,
    textColor: Color,
    onInspectCrypto: () -> Unit,
    onReact: () -> Unit
) {
    val timeFormatted = remember(message.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bubble_${message.id}"),
        horizontalAlignment = if (isFromUser) Alignment.End else Alignment.Start
    ) {
        // In group chats: sender name
        if (isGroup && !isFromUser) {
            Text(
                text = message.sender,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA1A1AA),
                modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .clickable { onReact() }
                .clip(bubbleShape)
                .background(bubbleColor)
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Column {
                if (message.attachmentType != null) {
                    when (message.attachmentType) {
                        "IMAGE" -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black,
                                modifier = Modifier.size(width = 200.dp, height = 110.dp).padding(bottom = 6.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.background(Color(0xFF18181B))) {
                                    Text("📷 ${message.attachmentTitle ?: "Photo"}", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                        "AUDIO" -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = textColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🎤 Voice Memo", color = textColor, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        "LOCATION" -> {
                            Text("📍 ${message.text}", color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (message.isEncrypted) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Tap to inspect crypto",
                            tint = textColor.copy(alpha = 0.75f),
                            modifier = Modifier
                                .size(10.dp)
                                .clickable { onInspectCrypto() }
                                .testTag("bubble_crypto_${message.id}")
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }

                    Text(
                        text = timeFormatted,
                        fontSize = 10.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )

                    if (isFromUser) {
                        Spacer(modifier = Modifier.width(5.dp))
                        OledReadStatusIndicator(
                            status = message.status,
                            textColor = textColor,
                            modifier = Modifier.testTag("read_status_${message.id}")
                        )
                    }
                }
            }
        }

        if (!message.reaction.isNullOrBlank()) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF18181B),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A)),
                modifier = Modifier
                    .padding(top = 2.dp)
                    .clickable { onReact() }
            ) {
                Text(
                    text = message.reaction,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun OledReadStatusIndicator(
    status: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    when (status) {
        "SENDING" -> {
            val infiniteTransition = rememberInfiniteTransition(label = "sending_pip_transition")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.95f,
                animationSpec = infiniteRepeatable(
                    animation = tween(450),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "sending_pip_alpha"
            )
            Box(
                modifier = modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(textColor.copy(alpha = alpha))
            )
        }
        "SENT" -> {
            // Single subtle 3.5dp dot in muted tone
            Box(
                modifier = modifier
                    .size(3.5.dp)
                    .clip(CircleShape)
                    .background(textColor.copy(alpha = 0.45f))
            )
        }
        "DELIVERED" -> {
            // Dual subtle 3.5dp dots in clean neutral tone
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(3.5.dp)
                        .clip(CircleShape)
                        .background(textColor.copy(alpha = 0.65f))
                )
                Box(
                    modifier = Modifier
                        .size(3.5.dp)
                        .clip(CircleShape)
                        .background(textColor.copy(alpha = 0.65f))
                )
            }
        }
        "READ" -> {
            // Dual glowing cyan micro-dots matching pure OLED black
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(3.5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF06B6D4))
                )
                Box(
                    modifier = Modifier
                        .size(3.5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF06B6D4))
                )
            }
        }
        else -> {
            Box(
                modifier = modifier
                    .size(3.5.dp)
                    .clip(CircleShape)
                    .background(textColor.copy(alpha = 0.45f))
            )
        }
    }
}

@Composable
fun ThreadMediaWindow(
    conversation: ConversationEntity,
    messages: List<MessageEntity>,
    onBackToChat: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ALL") }
    var inspectingMediaMsg by remember { mutableStateOf<MessageEntity?>(null) }

    val categories = listOf("ALL", "PHOTOS", "AUDIO", "LOCATION", "DOCS")

    val filteredMessages = remember(messages, selectedCategory) {
        if (selectedCategory == "ALL") {
            messages
        } else {
            messages.filter { msg ->
                when (selectedCategory) {
                    "PHOTOS" -> msg.attachmentType == "IMAGE"
                    "AUDIO" -> msg.attachmentType == "AUDIO"
                    "LOCATION" -> msg.attachmentType == "LOCATION"
                    "DOCS" -> msg.attachmentType != "IMAGE" && msg.attachmentType != "AUDIO" && msg.attachmentType != "LOCATION"
                    else -> true
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("thread_media_window"),
        containerColor = Color(0xFF000000), // Pure OLED Black
        topBar = {
            Surface(
                color = Color(0xFF000000),
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBackToChat,
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("media_window_back_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Chat",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = ">_ ",
                                        fontFamily = RobotoMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF06B6D4)
                                    )
                                    Text(
                                        text = "SHARED MEDIA",
                                        fontFamily = RobotoMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = "${conversation.recipientName} • ${filteredMessages.size} items",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFA1A1AA)
                                )
                            }
                        }

                        // Swipe hint badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF141418),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A)),
                            modifier = Modifier.clickable { onBackToChat() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "👉 Swipe right to chat",
                                    fontSize = 11.sp,
                                    fontFamily = RobotoMonoFontFamily,
                                    color = Color(0xFF06B6D4)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Pills
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            val isSelected = selectedCategory == category
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF06B6D4) else Color(0xFF141418),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF06B6D4) else Color(0xFF27272A)
                                ),
                                modifier = Modifier
                                    .clickable { selectedCategory = category }
                                    .testTag("media_filter_${category.lowercase()}")
                            ) {
                                Text(
                                    text = category,
                                    fontSize = 11.sp,
                                    fontFamily = RobotoMonoFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.Black else Color(0xFFA1A1AA),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF000000))
        ) {
            if (filteredMessages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PermMedia,
                            contentDescription = null,
                            tint = Color(0xFF27272A),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "[ NO MEDIA FOUND ]",
                            fontFamily = RobotoMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF71717A),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No shared attachments in $selectedCategory for this thread.",
                            color = Color(0xFFA1A1AA),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    items(filteredMessages, key = { it.id }) { msg ->
                        MediaItemCard(
                            message = msg,
                            onClick = { inspectingMediaMsg = msg }
                        )
                    }
                }
            }
        }
    }

    // Media inspector dialog
    inspectingMediaMsg?.let { msg ->
        AlertDialog(
            onDismissRequest = { inspectingMediaMsg = null },
            containerColor = Color(0xFF0A0A0C),
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ">_ ",
                        fontFamily = RobotoMonoFontFamily,
                        color = Color(0xFF06B6D4),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = msg.attachmentTitle ?: (msg.attachmentType ?: "Attachment"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF141418))
                            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when (msg.attachmentType) {
                                    "IMAGE" -> "📷"
                                    "AUDIO" -> "🎤"
                                    "LOCATION" -> "📍"
                                    else -> "📎"
                                },
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = msg.attachmentTitle ?: msg.text,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val timeFormatted = remember(msg.timestamp) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        sdf.format(Date(msg.timestamp))
                    }

                    Text(
                        text = "Sender: ${msg.sender}",
                        color = Color(0xFFA1A1AA),
                        fontSize = 11.sp,
                        fontFamily = RobotoMonoFontFamily
                    )
                    Text(
                        text = "Timestamp: $timeFormatted",
                        color = Color(0xFFA1A1AA),
                        fontSize = 11.sp,
                        fontFamily = RobotoMonoFontFamily
                    )
                    Text(
                        text = "Protocol: ${msg.protocol} • ${if (msg.isEncrypted) "E2EE ENCRYPTED" else "CLEARTEXT"}",
                        color = if (msg.isEncrypted) Color(0xFF10B981) else Color(0xFFF59E0B),
                        fontSize = 11.sp,
                        fontFamily = RobotoMonoFontFamily
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { inspectingMediaMsg = null }) {
                    Text("Close", color = Color(0xFF06B6D4))
                }
            }
        )
    }
}

@Composable
private fun MediaItemCard(
    message: MessageEntity,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121216)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A)),
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp)
            .clickable { onClick() }
            .testTag("media_item_${message.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = when (message.attachmentType) {
                        "IMAGE" -> "📷"
                        "AUDIO" -> "🎤"
                        "LOCATION" -> "📍"
                        else -> "📎"
                    },
                    fontSize = 26.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.attachmentTitle ?: message.text.take(14),
                    fontSize = 10.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = message.attachmentType ?: "FILE",
                    fontSize = 9.sp,
                    fontFamily = RobotoMonoFontFamily,
                    color = Color(0xFF06B6D4)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThreadCustomizerSheet(
    conversation: ConversationEntity,
    onDismiss: () -> Unit,
    onSaveTheming: (
        userColor: Long?,
        peerColor: Long?,
        userShape: String?,
        peerShape: String?,
        bgUri: String?,
        groupConfig: String?
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedUserColor by remember { mutableStateOf(conversation.customUserBubbleColor ?: 0xFF06B6D4) }
    var selectedPeerColor by remember { mutableStateOf(conversation.customPeerBubbleColor ?: 0xFF18181B) }
    var selectedUserShape by remember { mutableStateOf(conversation.customUserBubbleShape ?: "ROUNDED") }
    var selectedPeerShape by remember { mutableStateOf(conversation.customPeerBubbleShape ?: "ROUNDED") }
    var selectedBg by remember { mutableStateOf(conversation.customBackgroundUri ?: "OLED_BLACK") }

    val paletteOptions = listOf(
        0xFF06B6D4, // Cyan
        0xFF3B82F6, // Royal Blue
        0xFF10B981, // Emerald
        0xFFA855F7, // Purple
        0xFFF97316, // Orange
        0xFFEF4444, // Red
        0xFF18181B  // Pitch Dark
    )

    // For groups: customize each participant
    val members = remember(conversation.groupParticipants) {
        if (conversation.isGroup) conversation.groupParticipants.split(",").map { it.trim() } else emptyList()
    }
    var memberConfigs by remember {
        val initial = mutableMapOf<String, Pair<Long, String>>()
        conversation.groupBubbleConfigJson?.split(";")?.forEach { entry ->
            val parts = entry.split("|")
            if (parts.size >= 3) {
                val name = parts[0]
                val color = parts[1].removePrefix("0x").removePrefix("#").toLongOrNull(16) ?: 0xFF06B6D4
                val shape = parts[2]
                initial[name] = Pair(color, shape)
            }
        }
        mutableStateOf(initial)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF000000), // Pure OLED Black
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Customize ${if (conversation.isGroup) "Group" else "Thread"} Theming",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = "Personalize colors, shapes, accessibility text contrast, and wallpapers",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFA1A1AA)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // User Bubble Color
            Text("YOUR MESSAGE BUBBLE COLOR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(paletteOptions) { colorLong ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(colorLong))
                            .border(
                                width = if (selectedUserColor == colorLong) 3.dp else 1.dp,
                                color = if (selectedUserColor == colorLong) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedUserColor = colorLong }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // User Bubble Shape
            Text("YOUR BUBBLE STYLE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ROUNDED", "RECTANGULAR", "PILL", "CHAT_TAIL").forEach { shapeName ->
                    val isSelected = selectedUserShape == shapeName
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color(0xFF141418),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedUserShape = shapeName }
                    ) {
                        Text(
                            text = shapeName.take(5),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFA1A1AA),
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            if (!conversation.isGroup) {
                // Direct chat peer bubble styling
                Spacer(modifier = Modifier.height(14.dp))
                Text("${conversation.recipientName.uppercase()}'S BUBBLE COLOR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(paletteOptions) { colorLong ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorLong))
                                .border(
                                    width = if (selectedPeerColor == colorLong) 3.dp else 1.dp,
                                    color = if (selectedPeerColor == colorLong) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedPeerColor = colorLong }
                        )
                    }
                }
            } else {
                // Group chat: customize for each participant!
                Spacer(modifier = Modifier.height(16.dp))
                Text("GROUP PARTICIPANTS BUBBLES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))

                members.forEach { memberName ->
                    val current = memberConfigs[memberName] ?: Pair(0xFF06B6D4, "ROUNDED")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(memberName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Quick color cycle
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(current.first))
                                    .clickable {
                                        val nextColor = paletteOptions[(paletteOptions.indexOf(current.first) + 1) % paletteOptions.size]
                                        memberConfigs = memberConfigs.toMutableMap().apply { put(memberName, Pair(nextColor, current.second)) }
                                    }
                            )
                            // Quick shape toggle (Rounded vs Rectangular vs Pill)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF141418),
                                modifier = Modifier.clickable {
                                    val nextShape = when (current.second) {
                                        "ROUNDED" -> "RECTANGULAR"
                                        "RECTANGULAR" -> "PILL"
                                        else -> "ROUNDED"
                                    }
                                    memberConfigs = memberConfigs.toMutableMap().apply { put(memberName, Pair(current.first, nextShape)) }
                                }
                            ) {
                                Text(current.second.take(4), fontSize = 10.sp, color = Color(0xFFA1A1AA), modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Save Button
            Button(
                onClick = {
                    val groupConfigString = if (conversation.isGroup) {
                        memberConfigs.map { "${it.key}|0x${java.lang.Long.toHexString(it.value.first)}|${it.value.second}" }.joinToString(";")
                    } else null

                    onSaveTheming(
                        selectedUserColor,
                        selectedPeerColor,
                        selectedUserShape,
                        selectedPeerShape,
                        selectedBg,
                        groupConfigString
                    )
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("save_theming_btn")
            ) {
                Text("Apply to Chat")
            }
        }
    }
}
