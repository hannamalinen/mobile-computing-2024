package com.example.hw1_composetutorial.ui.theme

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.hw1_composetutorial.MainActivity


interface PermissionRequester {
    fun requestPostNotificationPermission()
    fun onPermissionGranted()
}

class SensorHandler(private val context: Context, private val permissionRequester: PermissionRequester) {
    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    private var gyroscopeSensor: Sensor? = null

    init {
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if (gyroscopeSensor == null) {
            Log.e("SensorHandler", "Gyroskooppia ei löydy laitteesta.")
        } else {
            registerSensorListener()
        }
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(sensorEvent: SensorEvent?) {
            sensorEvent?.let { event ->
                // tarkista liike x, y, ja z-akseleilla
                val xRotationRate = event.values[0] // x
                val yRotationRate = event.values[1] // y
                val zRotationRate = event.values[2] // z

                // laheta ilmoitus, jos jokin akseleista liikkuu tarpeeksi
                if (Math.abs(xRotationRate) > GYRO_THRESHOLD ||
                    Math.abs(yRotationRate) > GYRO_THRESHOLD ||
                    Math.abs(zRotationRate) > GYRO_THRESHOLD) {

                    val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("SensorDataDetected", true).apply()

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        // oikeudet on, laheta ilmoitus
                        sendNotificationAboutMovement()
                    } else {
                        // ei oikeuksia, pyyda kayttajalta oikeudet
                        permissionRequester.requestPostNotificationPermission()
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // not used
        }
    }

    fun registerSensorListener() {
        gyroscopeSensor?.also {
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    // not used in the app
    fun unregisterSensorListener() {
        sensorManager.unregisterListener(sensorEventListener)
    }

    // sends a notification to the user about the movement
    private fun sendNotificationAboutMovement() {
        val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("NotificationsEnabled", false)

        if (notificationsEnabled) {
            // sends a notification when notifications are enabled and significant movement is detected
            NotificationUtils.sendNotification(
                context,
                "Puhelinta Käännetään",
                "Havaittiin merkittävää liikehdintää gyroskoopilla."
            )
        }
    }

    // sends a notification to the user about enabled notifications
    fun onPermissionGranted() {
        NotificationUtils.sendNotification(
            context,
            "Ilmoitusoikeudet Myönnetty",
            "Voit nyt vastaanottaa ilmoituksia sovelluksesta."
        )
        registerSensorListener()
    }

    companion object {
        private const val GYRO_THRESHOLD = 1.0f // gyro threshold for movement
    }
}
