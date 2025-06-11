package com.example.canary.fragments


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.navigine.idl.java.Position
import com.navigine.idl.java.PositionListener
import com.example.canary.R
import com.example.canary.MainActivity
import com.example.canary.fragments.utils.Constants
import com.example.canary.NavigineSdkManager
import com.navigine.idl.java.NavigationManager

class NavigationService : Service() {

    companion object {
        var INSTANCE: NavigationService? = null
        const val ACTION_POSITION_UPDATED = "ACTION_POSITION_UPDATED"
        const val ACTION_POSITION_ERROR = "ACTION_POSITION_ERROR"
        const val KEY_LOCATION_ID = "location_id"
        const val KEY_SUBLOCATION_ID = "sublocation_id"
        const val KEY_LOCATION_HEADING = "location_heading"
        const val KEY_POINT_X = "point_x"
        const val KEY_POINT_Y = "point_y"
        const val KEY_ERROR = "error"

        fun startService(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, Intent(context, NavigationService::class.java))
            } else {
                context.startService(Intent(context, NavigationService::class.java))
            }
        }

        fun stopService(context: Context) {
            context.stopService(Intent(context, NavigationService::class.java))
        }
    }
    val NavigationManager = NavigineSdkManager.navigationManager
    val mPositionListener = object : PositionListener() {
        override fun onPositionUpdated(position: Position) {
            val intent = Intent(ACTION_POSITION_UPDATED).apply {
                putExtra(KEY_LOCATION_ID, position.locationPoint.locationId)
                putExtra(KEY_SUBLOCATION_ID, position.locationPoint.sublocationId)
                putExtra(KEY_POINT_X, position.locationPoint.point.x)
                putExtra(KEY_POINT_Y, position.locationPoint.point.y)
                putExtra(KEY_LOCATION_HEADING, position.locationHeading)
            }
            sendBroadcast(intent)
        }

        override fun onPositionError(error: Error) {
            val intent = Intent(ACTION_POSITION_ERROR).apply {
                putExtra(KEY_ERROR, error.message)
            }
            sendBroadcast(intent)
        }
    }

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        wakeLockAcquire()
        addPositionListener()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        val notification = createNotification()
        startForeground(1, notification)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        removePositionListener()
        wakeLockRelease()
        super.onDestroy()
    }

    private fun addPositionListener() {
        NavigineSdkManager.navigationManager?.let {
            it.addPositionListener(mPositionListener)
        }
    }

    private fun removePositionListener() {
        NavigineSdkManager.navigationManager?.let {
            it.removePositionListener(mPositionListener)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_MUTABLE
        )

        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.navigation_service_name))
            .setSmallIcon(R.drawable.ic_navigation)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun wakeLockAcquire() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "navigine:wakelock")
        wakeLock?.let { if (!it.isHeld) it.acquire() }
    }

    private fun wakeLockRelease() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
    }
}