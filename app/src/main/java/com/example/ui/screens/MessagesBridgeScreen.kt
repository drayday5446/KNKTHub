package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.data.repository.MessagingRepository
import kotlinx.coroutines.launch

private fun isListenerEnabled(context: Context): Boolean =
    NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

@Composable
fun MessagesBridgeScreen(
    repository: MessagingRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var enabled by remember { mutableStateOf(isListenerEnabled(context)) }
    var cleared by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val owner = context as? LifecycleOwner
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) enabled = isListenerEnabled(context)
        }
        owner?.lifecycle?.addObserver(observer)
        onDispose { owner?.lifecycle?.removeObserver(observer) }
    }

    BackHandler { onNavigateBack() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onNavigateBack) {
            Text("Back", color = Color.White)
        }

        Text(
            text = "Google Messages bridge",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "KNKT reads new messages from your Google Messages notifications and shows them in KNKT's interface. Replies you send in KNKT are sent through Google Messages.",
            fontSize = 13.sp,
            color = Color(0xFFA1A1AA)
        )

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (enabled) Color(0xFF052E1B) else Color(0xFF2A0F10)
        ) {
            Text(
                text = if (enabled) "Connected: KNKT has notification access" else "Not connected: notification access is off",
                modifier = Modifier.padding(14.dp),
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) Color(0xFF10B981) else Color(0xFFEF4444)
            )
        }

        Button(
            onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("1. Grant notification access")
        }

        OutlinedButton(
            onClick = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + context.packageName)
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("If the switch is greyed out: open App info")
        }
        Text(
            text = "On Android 13+, apps installed from an APK file are restricted. In App info, tap the three dots at the top right and choose \"Allow restricted settings\", then go back and turn on notification access.",
            fontSize = 12.sp,
            color = Color(0xFF71717A)
        )

        OutlinedButton(
            onClick = {
                scope.launch {
                    repository.clearSampleChats()
                    cleared = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (cleared) "Sample chats cleared" else "2. Clear sample chats")
        }

        Text(
            text = "Good to know",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "- Only messages that arrive after you connect will show up. There is no history.\n" +
                "- Keep Google Messages notifications ON. Setting them to Silent is fine, but turning them off stops KNKT from seeing messages.\n" +
                "- To reply from KNKT, that chat needs an active notification from Google Messages. If you dismissed it, replying works again after the next message arrives.\n" +
                "- Photos and other media show as a short label only.\n" +
                "- Protocol badges (SMS/RCS) are not real in bridged chats.",
            fontSize = 12.sp,
            color = Color(0xFFA1A1AA),
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(120.dp))
    }
}
