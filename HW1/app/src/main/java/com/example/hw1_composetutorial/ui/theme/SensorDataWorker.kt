package com.example.hw1_composetutorial.ui.theme

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class SensorDataWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {

    override fun doWork(): Result {

        val prefs = applicationContext.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("NotificationsEnabled", false)
        val sensorData = prefs.getBoolean("SensorDataDetected", false)

        if (sensorData && notificationsEnabled) {
            // ilmoitukset sallittu, laheta ilmoitus
            NotificationUtils.sendNotification(
                applicationContext,
                "Sensor Alert",
                "Sensor detected significant movement."
            )
        }

        // nollaa sensoridatan tila
        prefs.edit().putBoolean("SensorDataDetected", false).apply()

        return Result.success()
    }
}