package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentPickerSheet(
    onDismiss: () -> Unit,
    onAttachPhoto: (uri: String, title: String) -> Unit,
    onAttachAudio: (title: String) -> Unit,
    onAttachLocation: (title: String) -> Unit,
    onAttachContact: (name: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var mmsSubject by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("attachment_picker_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Share Content & Media",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Select media to attach over RCS or MMS protocol",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4 main attachment tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AttachmentOption(
                    title = "Gallery",
                    icon = Icons.Default.PhotoLibrary,
                    color = Color(0xFF3B82F6),
                    testTag = "attach_gallery_btn",
                    onClick = {
                        onAttachPhoto(
                            "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600",
                            "sunset_gallery.jpg"
                        )
                        onDismiss()
                    }
                )

                AttachmentOption(
                    title = "Camera",
                    icon = Icons.Default.CameraAlt,
                    color = Color(0xFF10B981),
                    testTag = "attach_camera_btn",
                    onClick = {
                        onAttachPhoto(
                            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600",
                            "camera_capture.jpg"
                        )
                        onDismiss()
                    }
                )

                AttachmentOption(
                    title = "Voice Memo",
                    icon = Icons.Default.Mic,
                    color = Color(0xFFEF4444),
                    testTag = "attach_voice_btn",
                    onClick = {
                        onAttachAudio("Voice Memo (0:18)")
                        onDismiss()
                    }
                )

                AttachmentOption(
                    title = "Location",
                    icon = Icons.Default.LocationOn,
                    color = Color(0xFFF59E0B),
                    testTag = "attach_location_btn",
                    onClick = {
                        onAttachLocation("Namsan Seoul Tower • 37.5512° N, 126.9882° E")
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AttachmentOption(
                    title = "Contact",
                    icon = Icons.Default.ContactPhone,
                    color = Color(0xFF8B5CF6),
                    testTag = "attach_contact_card_btn",
                    onClick = {
                        onAttachContact("Min-Jun Kim (+82 10-1234-5678)")
                        onDismiss()
                    }
                )

                AttachmentOption(
                    title = "Document",
                    icon = Icons.Default.Description,
                    color = Color(0xFF06B6D4),
                    testTag = "attach_doc_btn",
                    onClick = {
                        onAttachPhoto("", "oneui_spec_doc.pdf")
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.size(64.dp))
            }
        }
    }
}

@Composable
private fun AttachmentOption(
    title: String,
    icon: ImageVector,
    color: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(4.dp)
    ) {
        Surface(
            color = color.copy(alpha = 0.15f),
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
