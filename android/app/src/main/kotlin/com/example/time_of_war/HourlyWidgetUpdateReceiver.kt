package com.example.time_of_war

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import java.util.Calendar

class HourlyWidgetUpdateReceiver : BroadcastReceiver() {

    companion object {
        private const val REQUEST_CODE = 2022
        private const val ACTION_HOURLY_UPDATE =
            "com.example.time_of_war.HOURLY_WIDGET_UPDATE"

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

        if (!showHour) {
            cancel(context)
            return
        }

        /*
         * Передаємо Android-системі команду оновити
         * всі встановлені віджети.
         *
         * Flutter/HomeWidget після цього може
         * перемалювати widget_image.
         */
        val updateIntent = Intent(
            context,
            WidgetProvider::class.java
        ).apply {
            action =
                android.appwidget.AppWidgetManager
                    .ACTION_APPWIDGET_UPDATE
        }

        context.sendBroadcast(updateIntent)

        /*
         * Ставимо наступне оновлення рівно на xx:01.
         */
        schedule(context)
    }
}
