package com.example.time_of_war

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Calendar

class HourlyWidgetUpdateReceiver : BroadcastReceiver() {

    companion object {

        private const val REQUEST_CODE = 2022

        private const val ACTION_HOURLY_UPDATE =
            "com.example.time_of_war.HOURLY_WIDGET_UPDATE"

        private const val BACKGROUND_ACTION =
            "es.antonborri.home_widget.action.BACKGROUND"

        private const val UPDATE_URI =
            "timeofwar://hourly_update"

        fun schedule(context: Context) {

            val prefs = context.getSharedPreferences(
                "HomeWidgetPreferences",
                Context.MODE_PRIVATE
            )

            val showHour = prefs.getBoolean(
                "showHour",
                true
            )

            if (!showHour) {
                cancel(context)
                return
            }

            val alarmManager =
                context.getSystemService(
                    Context.ALARM_SERVICE
                ) as AlarmManager

            val intent = Intent(
                context,
                HourlyWidgetUpdateReceiver::class.java
            ).apply {
                action = ACTION_HOURLY_UPDATE
            }

            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
                )

            val next = Calendar.getInstance().apply {

                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                /*
                 * Наступне оновлення рівно на xx:01.
                 */
                if (get(Calendar.MINUTE) >= 1) {
                    add(Calendar.HOUR_OF_DAY, 1)
                }

                set(Calendar.MINUTE, 1)
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                next.timeInMillis,
                pendingIntent
            )
        }

        fun cancel(context: Context) {

            val alarmManager =
                context.getSystemService(
                    Context.ALARM_SERVICE
                ) as AlarmManager

            val intent = Intent(
                context,
                HourlyWidgetUpdateReceiver::class.java
            ).apply {
                action = ACTION_HOURLY_UPDATE
            }

            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
                )

            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        if (intent.action != ACTION_HOURLY_UPDATE) {
            return
        }

        val prefs = context.getSharedPreferences(
            "HomeWidgetPreferences",
            Context.MODE_PRIVATE
        )

        val showHour = prefs.getBoolean(
            "showHour",
            true
        )

        /*
         * Якщо години вимкнули —
         * нічого не запускаємо і alarm прибираємо.
         */
        if (!showHour) {
            cancel(context)
            return
        }

        /*
         * Передаємо керування HomeWidget Background Receiver.
         *
         * Він запустить Dart background callback,
         * де буде створено нове widget_image.
         */
        val backgroundIntent = Intent(
            BACKGROUND_ACTION
        ).apply {
            setPackage(context.packageName)
            data = android.net.Uri.parse(
                UPDATE_URI
            )
        }

        context.sendBroadcast(
            backgroundIntent
        )

        /*
         * Одразу плануємо наступне оновлення.
         */
        schedule(context)
    }
}
