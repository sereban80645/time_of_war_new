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

        const val ACTION_HOURLY_UPDATE =
            "com.example.time_of_war.HOURLY_WIDGET_UPDATE"

        const val ACTION_CANCEL =
            "com.example.time_of_war.CANCEL_HOURLY_UPDATE"

        private const val BACKGROUND_ACTION =
            "es.antonborri.home_widget.action.BACKGROUND"

        private const val UPDATE_URI =
            "timeofwar://hourly_update"

        fun schedule(
            context: Context
        ) {

            val prefs =
                context.getSharedPreferences(
                    "HomeWidgetPreferences",
                    Context.MODE_PRIVATE
                )

            /*
             * Якщо показ годин вимкнений,
             * погодинний будильник не потрібен.
             */
            if (!prefs.getBoolean(
                    "showHour",
                    true
                )
            ) {
                cancel(context)
                return
            }

            val alarmManager =
                context.getSystemService(
                    Context.ALARM_SERVICE
                ) as AlarmManager

            val intent =
                Intent(
                    context,
                    HourlyWidgetUpdateReceiver::class.java
                ).apply {
                    action =
                        ACTION_HOURLY_UPDATE
                }

            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
                )

            /*
             * Наступне оновлення рівно о XX:01:00.
             *
             * Наприклад:
             * 13:01 -> наступне 14:01
             * 14:01 -> наступне 15:01
             */
            val next =
                Calendar.getInstance().apply {

                    add(
                        Calendar.HOUR_OF_DAY,
                        1
                    )

                    set(
                        Calendar.MINUTE,
                        1
                    )

                    set(
                        Calendar.SECOND,
                        0
                    )

                    set(
                        Calendar.MILLISECOND,
                        0
                    )
                }

            try {

                /*
                 * Точний alarm навіть у Doze.
                 *
                 * Для Android 12+ необхідно,
                 * щоб користувач дозволив
                 * "Будильники та нагадування".
                 */
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    next.timeInMillis,
                    pendingIntent
                )

            } catch (
                e: SecurityException
            ) {

                /*
                 * Якщо дозвіл на точні alarm
                 * ще не наданий, не падаємо.
                 *
                 * Після надання дозволу наступний
                 * виклик schedule() поставить
                 * точний alarm.
                 */
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    next.timeInMillis,
                    pendingIntent
                )
            }
        }

        fun cancel(
            context: Context
        ) {

            val alarmManager =
                context.getSystemService(
                    Context.ALARM_SERVICE
                ) as AlarmManager

            val intent =
                Intent(
                    context,
                    HourlyWidgetUpdateReceiver::class.java
                ).apply {
                    action =
                        ACTION_HOURLY_UPDATE
                }

            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
                )

            alarmManager.cancel(
                pendingIntent
            )

            pendingIntent.cancel()
        }
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        when (intent.action) {

            /*
             * Android після перезавантаження
             * телефона.
             */
            Intent.ACTION_BOOT_COMPLETED -> {

                schedule(context)
                return
            }

            ACTION_CANCEL -> {

                cancel(context)
                return
            }

            ACTION_HOURLY_UPDATE -> {

                val prefs =
                    context.getSharedPreferences(
                        "HomeWidgetPreferences",
                        Context.MODE_PRIVATE
                    )

                /*
                 * Якщо користувач вимкнув
                 * "Показ годин" — більше
                 * нічого не плануємо.
                 */
                if (!prefs.getBoolean(
                        "showHour",
                        true
                    )
                ) {

                    cancel(context)
                    return
                }

                /*
                 * Передаємо команду HomeWidget
                 * на виконання backgroundCallback
                 * у main.dart.
                 */
                val backgroundIntent =
                    Intent(
                        BACKGROUND_ACTION
                    ).apply {

                        setPackage(
                            context.packageName
                        )

                        data =
                            android.net.Uri.parse(
                                UPDATE_URI
                            )
                    }

                context.sendBroadcast(
                    backgroundIntent
                )

                /*
                 * Дуже важливо:
                 * після спрацювання поточного alarm
                 * ставимо наступний.
                 */
                schedule(context)
            }
        }
    }
}
