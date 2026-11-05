package com.example.bridge

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.example.data.local.AppDatabase
import com.example.data.repository.MessagingRepository
import kotlinx.coroutines.launch

class MessagesNotificationListener : NotificationListenerService() {

    private val repository: MessagingRepository by lazy {
        val scope = GoogleMessagesBridge.scope
        MessagingRepository(
            AppDatabase.getDatabase(applicationContext, scope),
            scope,
            applicationContext
        )
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            activeNotifications?.forEach { handle(it) }
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn != null) handle(sbn)
    }

    private fun handle(sbn: StatusBarNotification) {
        if (sbn.packageName != GoogleMessagesBridge.GOOGLE_MESSAGES_PACKAGE) return
        val notification = sbn.notification ?: return
        if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return

        val style = NotificationCompat.MessagingStyle
            .extractMessagingStyleFromNotification(notification) ?: return

        val title = (style.conversationTitle
            ?: notification.extras.getCharSequence(Notification.EXTRA_TITLE))
            ?.toString()?.trim().orEmpty()
        if (title.isBlank()) return

        val key = GoogleMessagesBridge.KEY_PREFIX + title

        notification.actions
            ?.firstOrNull { !it.remoteInputs.isNullOrEmpty() }
            ?.let { GoogleMessagesBridge.rememberReplyAction(key, it) }

        val isGroup = style.isGroupConversation
        val selfName = style.user.name?.toString()

        val incoming = style.messages.mapNotNull { m ->
            val sender = m.person?.name?.toString() ?: return@mapNotNull null
            if (sender == selfName || sender == "You") return@mapNotNull null
            var text = m.text?.toString().orEmpty()
            if (text.isBlank() && m.dataMimeType?.startsWith("image") == true) {
                text = "📷 Photo"
            }
            if (text.isBlank()) return@mapNotNull null
            Triple(sender, text, m.timestamp)
        }
        if (incoming.isEmpty()) return

        GoogleMessagesBridge.scope.launch {
            incoming.forEach { (sender, text, time) ->
                repository.receiveBridgedMessage(key, title, isGroup, sender, text, time)
            }
        }
    }
}
