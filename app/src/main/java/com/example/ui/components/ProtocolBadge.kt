package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MessageProtocol

@Composable
fun ProtocolBadge(
    protocol: MessageProtocol,
    modifier: Modifier = Modifier,
    isSmall: Boolean = false
) {
    val (bg, icon, label) = when (protocol) {
        MessageProtocol.SMS -> Triple(Color(0xFF64748B), Icons.Default.ChatBubbleOutline, "SMS")
        MessageProtocol.MMS -> Triple(Color(0xFF0284C7), Icons.Default.Attachment, "MMS")
        MessageProtocol.RCS -> Triple(Color(0xFF2563EB), Icons.Default.Bolt, "RCS")
    }

    Row(
        modifier = modifier
            .testTag("protocol_badge_${protocol.name.lowercase()}")
            .background(
                color = bg.copy(alpha = 0.18f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(
                horizontal = if (isSmall) 6.dp else 8.dp,
                vertical = if (isSmall) 2.dp else 4.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = bg,
            modifier = Modifier.size(if (isSmall) 10.dp else 13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = bg,
            fontSize = if (isSmall) 10.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun E2EELockBadge(
    modifier: Modifier = Modifier,
    isVerified: Boolean = true,
    isSmall: Boolean = false
) {
    val color = if (isVerified) Color(0xFF10B981) else Color(0xFFF59E0B)
    Row(
        modifier = modifier
            .testTag("e2ee_badge")
            .background(
                color = color.copy(alpha = 0.16f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(
                horizontal = if (isSmall) 6.dp else 8.dp,
                vertical = if (isSmall) 2.dp else 4.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isVerified) Icons.Default.Security else Icons.Default.Lock,
            contentDescription = "End-to-End Encrypted",
            tint = color,
            modifier = Modifier.size(if (isSmall) 10.dp else 12.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = if (isVerified) "E2EE" else "ENCRYPTED",
            color = color,
            fontSize = if (isSmall) 9.sp else 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
