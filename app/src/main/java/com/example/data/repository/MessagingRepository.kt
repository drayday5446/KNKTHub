package com.example.data.repository

import com.example.data.crypto.CryptoEngine
import com.example.data.local.AppDatabase
import com.example.data.local.ContactEntity
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.model.BubbleShapeType
import com.example.data.model.MessageProtocol
import com.example.data.model.SwipeRightAction
import com.example.data.model.ThemePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import android.content.Context
import com.example.bridge.GoogleMessagesBridge
import com.example.util.HapticHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessagingRepository(
    private val database: AppDatabase,
    private val scope: CoroutineScope,
    private val context: Context? = null
) {
    private val convDao = database.conversationDao()
    private val msgDao = database.messageDao()
    private val contactDao = database.contactDao()

    private val _typingStatus = MutableStateFlow<Map<Long, String>>(emptyMap())
    val typingStatus: StateFlow<Map<Long, String>> = _typingStatus.asStateFlow()

    private val _themePreferences = MutableStateFlow(
        ThemePreferences(
            isOledBlack = true,
            customAccentHex = "#06B6D4",
            swipeRightAction = SwipeRightAction.MARK_READ_UNREAD,
            defaultBubbleShape = BubbleShapeType.ROUNDED,
            squircleRadiusDp = 22,
            fontSizeScale = 1.0f,
            hapticFeedback = true,
            quickReplyEnabled = true
        )
    )
    val themePreferences: StateFlow<ThemePreferences> = _themePreferences.asStateFlow()

    fun updateThemePreferences(update: (ThemePreferences) -> ThemePreferences) {
        _themePreferences.value = update(_themePreferences.value)
    }

    // Direct 1-on-1 Threads
    fun getDirectThreads(): Flow<List<ConversationEntity>> = convDao.getAllDirectThreads()

    // Group Threads
    fun getGroupThreads(): Flow<List<ConversationEntity>> = convDao.getAllGroupThreads()

    // Pinned Conversations
    fun getPinnedConversations(): Flow<List<ConversationEntity>> = convDao.getPinnedConversations()
    fun getPinnedDirectConversations(): Flow<List<ConversationEntity>> = convDao.getPinnedDirectConversations()
    fun getPinnedGroupConversations(): Flow<List<ConversationEntity>> = convDao.getPinnedGroupConversations()

    // Archive & Trash
    fun getArchivedConversations(): Flow<List<ConversationEntity>> = convDao.getArchivedConversations()
    fun getTrashConversations(): Flow<List<ConversationEntity>> = convDao.getTrashConversations()

    // Media
    fun getAllMediaMessages(): Flow<List<MessageEntity>> = msgDao.getAllMediaMessages()
    fun getMediaMessagesByType(type: String): Flow<List<MessageEntity>> = msgDao.getMediaMessagesByType(type)

    fun getConversationFlow(id: Long): Flow<ConversationEntity?> = convDao.getConversationFlow(id)
    fun getMessages(conversationId: Long): Flow<List<MessageEntity>> = msgDao.getMessagesForConversation(conversationId)
    fun getAllContacts(): Flow<List<ContactEntity>> = contactDao.getAllContacts()

    suspend fun markConversationRead(conversationId: Long) {
        convDao.markAsRead(conversationId)
    }

    suspend fun toggleReadStatus(conversationId: Long) {
        convDao.toggleReadStatus(conversationId)
    }

    suspend fun togglePin(conversationId: Long): Boolean {
        val conv = convDao.getConversationById(conversationId) ?: return false
        if (conv.isPinned) {
            convDao.setPinned(conversationId, false)
            return true
        } else {
            val count = if (conv.isGroup) convDao.getPinnedGroupCount() else convDao.getPinnedDirectCount()
            if (count >= 4) {
                return false // Maximum 4 pins allowed per section!
            }
            convDao.setPinned(conversationId, true)
            return true
        }
    }

    suspend fun toggleArchive(conversationId: Long) {
        convDao.toggleArchive(conversationId)
    }

    suspend fun moveToTrash(conversationId: Long) {
        convDao.moveToTrash(conversationId)
    }

    suspend fun restoreFromTrash(conversationId: Long) {
        convDao.restoreFromTrash(conversationId)
    }

    suspend fun emptyTrash() {
        convDao.emptyTrash()
    }

    suspend fun performRightSwipeAction(conversationId: Long, action: SwipeRightAction): String {
        return when (action) {
            SwipeRightAction.MARK_READ_UNREAD -> {
                convDao.toggleReadStatus(conversationId)
                "Toggled read status"
            }
            SwipeRightAction.ARCHIVE -> {
                convDao.toggleArchive(conversationId)
                "Conversation archived"
            }
            SwipeRightAction.TRASH -> {
                convDao.moveToTrash(conversationId)
                "Moved to Trash"
            }
            SwipeRightAction.PIN -> {
                val success = togglePin(conversationId)
                if (success) "Pin status updated" else "Maximum 4 pinned chats reached"
            }
        }
    }

    suspend fun updateCustomTheming(
        conversationId: Long,
        userColor: Long?,
        peerColor: Long?,
        userShape: String?,
        peerShape: String?,
        bgUri: String?,
        groupConfigJson: String?
    ) {
        convDao.updateCustomTheming(
            id = conversationId,
            userColor = userColor,
            peerColor = peerColor,
            userShape = userShape,
            peerShape = peerShape,
            bgUri = bgUri,
            groupConfig = groupConfigJson
        )
    }

    suspend fun createGroupConversation(
        title: String,
        participants: List<String>
    ): Long {
        val participantsStr = participants.joinToString(", ")
        val newGroup = ConversationEntity(
            recipientName = title.ifBlank { "Group Chat" },
            recipientPhone = "group_" + System.currentTimeMillis(),
            avatarColorHex = 0xFF8B5CF6,
            activeProtocol = "RCS",
            isE2EE = true,
            isGroup = true,
            groupParticipants = participantsStr,
            lastMessage = "Created group with ${participants.size} members",
            lastTimestamp = System.currentTimeMillis()
        )
        return convDao.insertConversation(newGroup)
    }

    suspend fun createOrGetDirectConversation(
        name: String,
        phone: String,
        protocol: MessageProtocol = MessageProtocol.RCS,
        isEncrypted: Boolean = true
    ): Long {
        val existing = convDao.getConversationByPhone(phone)
        if (existing != null) {
            if (existing.isTrash) {
                convDao.restoreFromTrash(existing.id)
            }
            return existing.id
        }

        val colors = listOf(0xFF06B6D4, 0xFF10B981, 0xFF3B82F6, 0xFFA855F7, 0xFFF97316)
        val randomColor = colors.random()

        val newConv = ConversationEntity(
            recipientName = name.ifBlank { phone },
            recipientPhone = phone,
            avatarColorHex = randomColor,
            activeProtocol = protocol.name,
            isE2EE = isEncrypted,
            isGroup = false,
            lastMessage = "Started conversation",
            lastTimestamp = System.currentTimeMillis()
        )
        val convId = convDao.insertConversation(newConv)

        val existingContact = contactDao.getContactByPhone(phone)
        if (existingContact == null) {
            contactDao.insertContact(
                ContactEntity(
                    name = name.ifBlank { phone },
                    phone = phone,
                    avatarColorHex = randomColor,
                    status = "Available on KNKT",
                    isE2EEVerified = false,
                    rcsCapable = protocol == MessageProtocol.RCS,
                    keyFingerprint = CryptoEngine.generateKeyFingerprint(phone)
                )
            )
        }
        return convId
    }

    suspend fun updateReaction(messageId: Long, reaction: String?) {
        msgDao.updateReaction(messageId, reaction)
    }

    suspend fun verifyContact(phone: String, isVerified: Boolean) {
        contactDao.updateE2EEVerification(phone, isVerified)
    }

    suspend fun updateProtocol(conversationId: Long, protocol: MessageProtocol) {
        convDao.updateProtocol(conversationId, protocol.name)
    }

    suspend fun updateE2EE(conversationId: Long, isE2ee: Boolean) {
        convDao.updateE2EE(conversationId, isE2ee)
    }

    suspend fun sendMessage(
        conversationId: Long,
        text: String,
        protocol: MessageProtocol,
        isEncrypted: Boolean,
        attachmentType: String? = null,
        attachmentTitle: String? = null,
        attachmentUri: String? = null,
        replyToText: String? = null
    ) {
        val conversation = convDao.getConversationById(conversationId) ?: return

        if (conversation.recipientPhone.startsWith(GoogleMessagesBridge.KEY_PREFIX)) {
            sendViaGoogleMessages(conversation, text)
            return
        }

        var cipherTextBase64: String? = null
        var ivHex: String? = null
        var tagHex: String? = null

        if (isEncrypted) {
            val encResult = CryptoEngine.encryptMessage(text, conversation.recipientPhone)
            cipherTextBase64 = encResult.cipherTextBase64
            ivHex = encResult.ivHex
            tagHex = encResult.tagHex
        }

        val sentMsg = MessageEntity(
            conversationId = conversationId,
            sender = "ME",
            isFromUser = true,
            text = text,
            timestamp = System.currentTimeMillis(),
            status = "SENDING",
            protocol = protocol.name,
            isEncrypted = isEncrypted,
            cipherTextBase64 = cipherTextBase64,
            ivHex = ivHex,
            tagHex = tagHex,
            attachmentType = attachmentType,
            attachmentTitle = attachmentTitle,
            attachmentUri = attachmentUri,
            replyToText = replyToText
        )

        val messageId = msgDao.insertMessage(sentMsg)
        context?.let { HapticHelper.messageSend(it) }

        val summaryText = when {
            attachmentType == "IMAGE" -> "📷 Photo attached"
            attachmentType == "AUDIO" -> "🎤 Voice memo"
            attachmentType == "LOCATION" -> "📍 Location shared"
            attachmentType == "FILE" -> "📄 Document"
            else -> text
        }

        convDao.updateLastMessage(
            id = conversationId,
            lastMsg = summaryText,
            timestamp = sentMsg.timestamp,
            protocol = protocol.name,
            isEncrypted = isEncrypted
        )

        scope.launch(Dispatchers.IO) {
            delay(300)
            msgDao.updateStatus(messageId, "SENT")
            delay(400)
            msgDao.updateStatus(messageId, "DELIVERED")

            // Simulate realistic reply if in demo/preview mode or interactive simulation
            if (!conversation.recipientName.contains("Alert", ignoreCase = true)) {
                simulateIncomingReply(conversation, text, protocol, isEncrypted)
            }
        }
    }

    private suspend fun sendViaGoogleMessages(conversation: ConversationEntity, text: String) {
        val now = System.currentTimeMillis()
        val messageId = msgDao.insertMessage(
            MessageEntity(
                conversationId = conversation.id,
                sender = "ME",
                isFromUser = true,
                text = text,
                timestamp = now,
                status = "SENDING",
                protocol = conversation.activeProtocol,
                isEncrypted = false
            )
        )
        convDao.updateLastMessage(conversation.id, text, now, conversation.activeProtocol, false)
        val ok = context?.let {
            GoogleMessagesBridge.sendReply(it, conversation.recipientPhone, text)
        } ?: false
        msgDao.updateStatus(messageId, if (ok) "SENT" else "FAILED")
    }

    suspend fun receiveBridgedMessage(
        key: String,
        title: String,
        isGroup: Boolean,
        sender: String,
        text: String,
        timestamp: Long
    ) {
        val existing = convDao.getConversationByPhone(key)
        val convId: Long
        if (existing == null) {
            val colors = listOf(0xFF06B6D4, 0xFF10B981, 0xFF3B82F6, 0xFFA855F7, 0xFFF97316)
            convId = convDao.insertConversation(
                ConversationEntity(
                    recipientName = title,
                    recipientPhone = key,
                    avatarColorHex = colors[(title.hashCode() and 0x7fffffff) % colors.size],
                    activeProtocol = "RCS",
                    isE2EE = false,
                    isGroup = isGroup,
                    groupParticipants = if (isGroup) sender else "",
                    lastMessage = text,
                    lastTimestamp = timestamp
                )
            )
        } else {
            if (existing.isTrash) convDao.restoreFromTrash(existing.id)
            convId = existing.id
        }

        // The same notification is re-posted with older messages; skip ones we already stored.
        if (msgDao.countMatching(convId, text, timestamp) > 0) return

        msgDao.insertMessage(
            MessageEntity(
                conversationId = convId,
                sender = sender,
                isFromUser = false,
                text = text,
                timestamp = timestamp,
                status = "DELIVERED",
                protocol = "RCS",
                isEncrypted = false
            )
        )

        val latest = convDao.getConversationById(convId)
        if (latest != null) {
            if (isGroup && !latest.groupParticipants.split(",").map { it.trim() }.contains(sender)) {
                val updated = if (latest.groupParticipants.isBlank()) sender else latest.groupParticipants + ", " + sender
                convDao.updateConversation(latest.copy(groupParticipants = updated))
            }
            if (timestamp >= latest.lastTimestamp) {
                convDao.updateLastMessage(
                    convId,
                    if (isGroup) "$sender: $text" else text,
                    timestamp,
                    "RCS",
                    false
                )
            }
        }
        convDao.incrementUnread(convId)
    }

    suspend fun clearSampleChats() {
        convDao.deleteNonBridgedConversations()
        msgDao.deleteOrphanMessages()
    }

    private suspend fun simulateIncomingReply(
        conversation: ConversationEntity,
        userMessage: String,
        protocol: MessageProtocol,
        isEncrypted: Boolean
    ) {
        val responderName = if (conversation.isGroup) {
            val members = conversation.groupParticipants.split(",").map { it.trim() }
            members.randomOrNull() ?: "Alex Rivera"
        } else {
            conversation.recipientName
        }

        delay(1000)
        _typingStatus.value = _typingStatus.value + (conversation.id to responderName)
        delay(1800)
        _typingStatus.value = _typingStatus.value - conversation.id

        val replyText = generateReplyText(responderName, userMessage, conversation.isGroup, isEncrypted)

        var replyCipher: String? = null
        var replyIv: String? = null
        var replyTag: String? = null

        if (isEncrypted) {
            val enc = CryptoEngine.encryptMessage(replyText, conversation.recipientPhone)
            replyCipher = enc.cipherTextBase64
            replyIv = enc.ivHex
            replyTag = enc.tagHex
        }

        val replyMsg = MessageEntity(
            conversationId = conversation.id,
            sender = responderName,
            isFromUser = false,
            text = replyText,
            timestamp = System.currentTimeMillis(),
            status = "READ",
            protocol = protocol.name,
            isEncrypted = isEncrypted,
            cipherTextBase64 = replyCipher,
            ivHex = replyIv,
            tagHex = replyTag
        )

        msgDao.insertMessage(replyMsg)
        convDao.updateLastMessage(
            id = conversation.id,
            lastMsg = if (conversation.isGroup) "$responderName: $replyText" else replyText,
            timestamp = replyMsg.timestamp,
            protocol = protocol.name,
            isEncrypted = isEncrypted
        )
        context?.let { HapticHelper.incomingNotification(it) }
    }

    private fun generateReplyText(name: String, query: String, isGroup: Boolean, isEncrypted: Boolean): String {
        val q = query.lowercase()
        return when {
            q.contains("hello") || q.contains("hey") || q.contains("hi") ->
                if (isGroup) "Hey everyone! Checking in from KNKT." else "Hey! The pure OLED black look on KNKT is incredible."
            q.contains("pin") || q.contains("card") || q.contains("grid") ->
                "The dynamic 1-4 pinned cards grid reorganizes super cleanly."
            q.contains("color") || q.contains("bubble") || q.contains("theme") ->
                "Love that each member in our group can have custom bubble colors and shapes!"
            q.contains("photo") || q.contains("media") || q.contains("picture") ->
                "Awesome shot! You can find it indexed under the Media tab anytime."
            q.contains("encrypt") || q.contains("security") || q.contains("lock") ->
                if (isEncrypted) "Verified! End-to-end encrypted with AES-256-GCM 🔒" else "Transmitting unencrypted standard packet."
            else ->
                "Got your message via KNKT ${if (isEncrypted) "E2EE" else "carrier"} connection."
        }
    }
}
