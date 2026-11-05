package com.example.bridge

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.ConcurrentHashMap

object GoogleMessagesBridge {
    const val GOOGLE_MESSAGES_PACKAGE = "com.google.android.apps.messaging"
    const val KEY_PREFIX = "gm:"

    // Lives as long as the app process so database setup is never cancelled with a service.
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val replyActions = ConcurrentHashMap<String, Notification.Action>()

    fun rememberReplyAction(key: String, action: Notification.Action) {
        replyActions[key] = action
    }

    fun sendReply(context: Context, key: String, text: String): Boolean {
        val action = replyActions[key] ?: return false
        val inputs = action.remoteInputs ?: return false
        if (inputs.isEmpty()) return false
        return try {
            val fillIn = Intent()
            val results = Bundle()
            results.putCharSequence(inputs[0].resultKey, text)
            RemoteInput.addResultsToIntent(inputs, fillIn, results)
            action.actionIntent.send(context, 0, fillIn)
            true
        } catch (e: PendingIntent.CanceledException) {
            false
        } catch (e: Exception) {
            false
        }
    }
}
