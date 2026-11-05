package com.example.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

enum class MessageProtocol(
    val label: String,
    val badgeColor: Long,
    val description: String,
    val maxChars: Int,
    val supportsMedia: Boolean,
    val supportsEncryption: Boolean
) {
    SMS(
        label = "SMS",
        badgeColor = 0xFF64748B, // Slate Gray
        description = "Carrier Cellular Text",
        maxChars = 160,
        supportsMedia = false,
        supportsEncryption = false
    ),
    MMS(
        label = "MMS",
        badgeColor = 0xFF0284C7, // Cyan/Sky
        description = "Multimedia Messaging (Media, Audio)",
        maxChars = 1000,
        supportsMedia = true,
        supportsEncryption = false
    ),
    RCS(
        label = "RCS",
        badgeColor = 0xFF06B6D4, // Neon Cyan
        description = "Rich Communication Services (E2EE)",
        maxChars = 8000,
        supportsMedia = true,
        supportsEncryption = true
    )
}

enum class DeliveryStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}

enum class SwipeRightAction(val displayName: String, val iconDescription: String) {
    MARK_READ_UNREAD("Mark Read / Unread", "Toggle read status"),
    ARCHIVE("Archive Thread", "Move to archive"),
    TRASH("Move to Trash", "Delete conversation"),
    PIN("Pin / Unpin", "Toggle pin status")
}

enum class BubbleShapeType(val displayName: String) {
    ROUNDED("Rounded Squircle"),
    RECTANGULAR("Sharp Rectangular"),
    PILL("Smooth Pill"),
    CHAT_TAIL("Message Tail")
}

data class ContactBubbleStyle(
    val colorLong: Long,
    val shapeType: BubbleShapeType
) {
    fun getTextColor(): Color {
        val c = Color(colorLong)
        return if (c.luminance() > 0.5f) Color(0xFF000000) else Color(0xFFFFFFFF)
    }
}

data class ThemePreferences(
    val isOledBlack: Boolean = true,
    val customAccentHex: String = "#06B6D4", // Neon Cyan
    val swipeRightAction: SwipeRightAction = SwipeRightAction.MARK_READ_UNREAD,
    val defaultBubbleShape: BubbleShapeType = BubbleShapeType.ROUNDED,
    val squircleRadiusDp: Int = 22,
    val fontSizeScale: Float = 1.0f,
    val hapticFeedback: Boolean = true,
    val quickReplyEnabled: Boolean = true
)

data class CryptoPacketInfo(
    val cipher: String = "AES-256-GCM / Signal Protocol",
    val ivHex: String,
    val tagHex: String,
    val cipherTextBase64: String,
    val keyFingerprint: String,
    val ratchetIndex: Int = 1,
    val isVerified: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
