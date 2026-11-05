package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipientName: String,
    val recipientPhone: String,
    val avatarColorHex: Long = 0xFF06B6D4,
    val activeProtocol: String = "RCS",
    val isE2EE: Boolean = true,
    val isGroup: Boolean = false,
    val groupParticipants: String = "", // e.g. "Alex Rivera, Sarah Chen, Elena Rostova"
    val lastMessage: String = "",
    val lastTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isTrash: Boolean = false,
    // Customization per thread
    val customUserBubbleColor: Long? = null,
    val customPeerBubbleColor: Long? = null,
    val customUserBubbleShape: String? = null, // "ROUNDED", "RECTANGULAR", "PILL", "CHAT_TAIL"
    val customPeerBubbleShape: String? = null,
    val customBackgroundUri: String? = null,
    val groupBubbleConfigJson: String? = null // stores contactName|colorLong|shapeType;...
)

@Entity(
    tableName = "messages",
    indices = [Index(value = ["conversationId"])]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val sender: String,
    val isFromUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "DELIVERED", // SENDING, SENT, DELIVERED, READ, FAILED
    val protocol: String = "RCS", // SMS, MMS, RCS
    val isEncrypted: Boolean = true,
    val cipherTextBase64: String? = null,
    val ivHex: String? = null,
    val tagHex: String? = null,
    val attachmentUri: String? = null,
    val attachmentType: String? = null, // "IMAGE", "AUDIO", "LOCATION", "LINK", "FILE"
    val attachmentTitle: String? = null,
    val reaction: String? = null,
    val replyToText: String? = null
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val avatarColorHex: Long = 0xFF06B6D4,
    val status: String = "Available on KNKT",
    val isE2EEVerified: Boolean = false,
    val rcsCapable: Boolean = true,
    val keyFingerprint: String = ""
)
