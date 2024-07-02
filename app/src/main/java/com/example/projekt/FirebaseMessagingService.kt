package com.example.projekt
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log


class FirebaseMessagingService : FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Sprawdź, czy otrzymano dane powiadomienia
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        }

        // Sprawdź, czy otrzymano treść powiadomienia
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // Tutaj możesz zaimplementować logikę związaną ze zmianą tokena,
        // na przykład zaktualizować go w bazie danych lub wysłać na serwer.
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
