package io.github.batmancpu.watchcompanion

import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue

data class NoticeEvent(val timestampMs: Long, val source: String, val kind: String)

/** Explicitly enabled, temporary phone metadata observation. No notification contents. */
object PhoneNoticeLab {
    var enabled by mutableStateOf(false)
        private set
    var serviceConnected by mutableStateOf(false)
        private set
    val events = mutableStateListOf<NoticeEvent>()
    private val main by lazy { Handler(Looper.getMainLooper()) }

    fun setEnabled(allow: Boolean) {
        enabled = allow
        if (!allow) serviceConnected = false
    }
    fun clear() = events.clear()
    fun setConnection(connected: Boolean) {
        main.post { serviceConnected = connected }
    }
    fun labelFor(packageName: String): String? = when (packageName.lowercase()) {
        "com.whatsapp", "com.whatsapp.w4b" -> "WhatsApp"
        "com.google.android.apps.messaging", "com.android.mms", "com.oplus.mms" -> "SMS"
        "org.telegram.messenger", "org.telegram.messenger.web" -> "Telegram"
        "com.google.android.dialer", "com.android.dialer", "com.oplus.dialer",
        "com.coloros.dialer", "com.android.phone", "com.samsung.android.dialer" -> "Phone"
        else -> null
    }
    fun onNotification(packageName: String, category: String?, isOngoing: Boolean) {
        val label = labelFor(packageName) ?: return
        val kind = if (category == android.app.Notification.CATEGORY_CALL) "call"
            else if (label == "Phone") "phone notification"
            else if (isOngoing) "ongoing notification" else "message/notification"
        main.post {
            if (!enabled) return@post
            if (events.size >= 100) events.removeAt(0)
            events.add(NoticeEvent(System.currentTimeMillis(), label, kind))
        }
    }
}

class PhoneNoticeListenerService : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        PhoneNoticeLab.setConnection(true)
    }
    override fun onListenerDisconnected() {
        PhoneNoticeLab.setConnection(false)
        super.onListenerDisconnected()
    }
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null || !PhoneNoticeLab.enabled) return
        if (sbn.packageName == packageName) return
        val notification = sbn.notification ?: return
        PhoneNoticeLab.onNotification(
            sbn.packageName, notification.category,
            notification.flags and android.app.Notification.FLAG_ONGOING_EVENT != 0
        )
    }
}
