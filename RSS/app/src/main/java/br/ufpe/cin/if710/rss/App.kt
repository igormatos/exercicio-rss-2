package br.ufpe.cin.if710.rss

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.SystemClock

const val BROADCAST_UPDATE_FEED = "feedUpdate"
const val BROADCAST_SHOW_NOTIFICATION = "showNotification"

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val intentService = Intent(this, DownloadIntentService::class.java).apply {
            action = ACTION_FETCH_NEW_ITEMS
        }

        val alarmIntent = intentService.let { intent ->
            PendingIntent.getService(this@App, 0, intent, 0)
        }

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                5000,
                alarmIntent
        )
    }

}