package com.example.hw1_composetutorial.ui.theme

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.hw1_composetutorial.MainActivity

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // tarkistus ennen kuin MainActivity kaynnistetaan
        if (intent?.action == "com.example.hw1_composetutorial.ACTION_NOTIFICATION_CLICK") {
            val toMainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context?.startActivity(toMainIntent)
        }
    }
}