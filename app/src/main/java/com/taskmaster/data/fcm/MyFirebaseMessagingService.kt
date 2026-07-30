package com.taskmaster.data.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.taskmaster.data.sync.SyncWorker
import timber.log.Timber

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("From: ${message.from}")

        // Check if message contains a data payload
        if (message.data.isNotEmpty()) {
            Timber.d("Message data payload: ${message.data}")
            if (message.data["action"] == "sync") {
                triggerBackgroundSync()
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("Refreshed token: $token")
        // Here you would upload the FCM token to your app server
    }

    private fun triggerBackgroundSync() {
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
        WorkManager.getInstance(applicationContext).enqueue(syncRequest)
    }
}
