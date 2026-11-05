package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.model.MessageProtocol
import com.example.data.repository.MessagingRepository
import com.example.ui.components.KNKTBottomDock
import com.example.ui.components.KNKTNavTab
import com.example.ui.screens.ChatThreadScreen
import com.example.ui.screens.CreateGroupDialog
import com.example.ui.screens.GoogleMessagesWebScreen
import com.example.ui.screens.GroupsScreen
import com.example.ui.screens.MessagesBridgeScreen
import com.example.ui.screens.MediaBrowserScreen
import com.example.ui.screens.NewDirectChatDialog
import com.example.ui.screens.SecurityVaultScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ThreadsScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var repository: MessagingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = AppDatabase.getDatabase(this, lifecycleScope)
        repository = MessagingRepository(database, lifecycleScope, this)

        handleIncomingIntent(intent)

        setContent {
            val themePreferences by repository.themePreferences.collectAsState()

            MyApplicationTheme(preferences = themePreferences) {
                KNKTAppScaffold(
                    repository = repository
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        if (Intent.ACTION_SENDTO == action || Intent.ACTION_SEND == action) {
            val data: Uri? = intent.data
            val schemeSpecific = data?.schemeSpecificPart ?: ""
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""

            if (schemeSpecific.isNotBlank() || sharedText.isNotBlank()) {
                val phone = if (schemeSpecific.isNotBlank()) schemeSpecific else "+1 (555) 000-0000"
                lifecycleScope.launch {
                    val convId = repository.createOrGetDirectConversation(
                        name = phone,
                        phone = phone,
                        protocol = MessageProtocol.RCS,
                        isEncrypted = false
                    )
                    if (sharedText.isNotBlank()) {
                        repository.sendMessage(
                            conversationId = convId,
                            text = sharedText,
                            protocol = MessageProtocol.RCS,
                            isEncrypted = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KNKTAppScaffold(
    repository: MessagingRepository
) {
    val scope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(KNKTNavTab.THREADS) }
    var activeConversationId by remember { mutableStateOf<Long?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isDockExpanded by remember { mutableStateOf(false) }
    var isVaultOpen by remember { mutableStateOf(false) }

    // Dialog states triggered from the Navigation Dock
    var showNewDirectChatDialog by remember { mutableStateOf(false) }
    var showNewGroupChatDialog by remember { mutableStateOf(false) }

    // Swipe-to-peek interactive state: dragging left brings the chat thread in proportionally over the list
    var peekConversationId by remember { mutableStateOf<Long?>(null) }
    var peekProgress by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)) // Pure OLED Black
    ) {
        val screenWidthPx = constraints.maxWidth.toFloat()

        if (isVaultOpen) {
            SecurityVaultScreen(
                repository = repository,
                onBackClick = { isVaultOpen = false },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Main Top-level Views (Threads, Groups, Media, Settings)
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentTab) {
                    KNKTNavTab.THREADS -> {
                        ThreadsScreen(
                            repository = repository,
                            searchQuery = searchQuery,
                            onConversationClick = { id ->
                                peekConversationId = null
                                activeConversationId = id
                            },
                            showNewChatDialog = showNewDirectChatDialog,
                            onDismissNewChatDialog = { showNewDirectChatDialog = false },
                            onSwipeLeftPeek = { id, progress ->
                                peekConversationId = id
                                peekProgress = progress.coerceIn(0f, 1f)
                            },
                            onNavigateToMedia = { currentTab = KNKTNavTab.MEDIA },
                            onOpenWebSync = { currentTab = KNKTNavTab.WEB_MESSAGES }
                        )
                    }
                    KNKTNavTab.WEB_MESSAGES -> {
                        MessagesBridgeScreen(
                            repository = repository,
                            onNavigateBack = { currentTab = KNKTNavTab.THREADS }
                        )
                    }
                    KNKTNavTab.GROUPS -> {
                        GroupsScreen(
                            repository = repository,
                            searchQuery = searchQuery,
                            onConversationClick = { id ->
                                peekConversationId = null
                                activeConversationId = id
                            },
                            showCreateGroupDialog = showNewGroupChatDialog,
                            onDismissCreateGroupDialog = { showNewGroupChatDialog = false },
                            onSwipeLeftPeek = { id, progress ->
                                peekConversationId = id
                                peekProgress = progress.coerceIn(0f, 1f)
                            },
                            onNavigateToMedia = { currentTab = KNKTNavTab.MEDIA }
                        )
                    }
                    KNKTNavTab.MEDIA -> {
                        MediaBrowserScreen(
                            repository = repository,
                            onShowInChat = { id ->
                                peekConversationId = null
                                activeConversationId = id
                            }
                        )
                    }
                    KNKTNavTab.SETTINGS -> {
                        SettingsScreen(
                            repository = repository,
                            onOpenSecurityVault = { isVaultOpen = true },
                            onOpenWebSync = { currentTab = KNKTNavTab.WEB_MESSAGES }
                        )
                    }
                }

                // Condensed Bottom Navigation Bubble (only visible when not in active chat)
                if (activeConversationId == null && peekConversationId == null) {
                    KNKTBottomDock(
                        currentTab = currentTab,
                        onTabSelected = { tab ->
                            currentTab = tab
                            isVaultOpen = false
                        },
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        isExpanded = isDockExpanded,
                        onToggleExpanded = { isDockExpanded = !isDockExpanded },
                        onNewMessageClick = {
                            isDockExpanded = false
                            if (currentTab == KNKTNavTab.GROUPS) {
                                showNewGroupChatDialog = true
                            } else {
                                showNewDirectChatDialog = true
                            }
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }

            // Interactive Peek Overlay (slides in proportionally over the list when dragging left on a card)
            if (peekConversationId != null && activeConversationId == null) {
                val offsetX = (screenWidthPx * (1f - peekProgress)).roundToInt()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(offsetX, 0) }
                ) {
                    ChatThreadScreen(
                        conversationId = peekConversationId!!,
                        repository = repository,
                        onBackClick = {
                            peekConversationId = null
                            peekProgress = 0f
                        }
                    )
                }
            }

            // Active Conversation View
            if (activeConversationId != null) {
                ChatThreadScreen(
                    conversationId = activeConversationId!!,
                    repository = repository,
                    onBackClick = { activeConversationId = null }
                )
            }
        }
    }

    // Direct Message Dialog from Dock
    if (showNewDirectChatDialog && currentTab != KNKTNavTab.THREADS) {
        NewDirectChatDialog(
            onDismiss = { showNewDirectChatDialog = false },
            onStartChat = { name, phone, protocol, isEncrypted ->
                showNewDirectChatDialog = false
                scope.launch {
                    val id = repository.createOrGetDirectConversation(name, phone, protocol, isEncrypted)
                    activeConversationId = id
                }
            }
        )
    }

    // Create Group Dialog from Dock
    if (showNewGroupChatDialog && currentTab != KNKTNavTab.GROUPS) {
        CreateGroupDialog(
            onDismiss = { showNewGroupChatDialog = false },
            onCreateGroup = { groupName, memberList ->
                showNewGroupChatDialog = false
                scope.launch {
                    val id = repository.createGroupConversation(groupName, memberList)
                    activeConversationId = id
                }
            }
        )
    }
}
