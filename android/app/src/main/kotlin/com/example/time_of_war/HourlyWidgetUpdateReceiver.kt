package com.example.time_of_war

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
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

        private fun pendingIntent(
            context: Context
        ): PendingIntent {

            val intent =
                Intent(
                    context,
                    HourlyWidgetUpdateReceiver::class.java
                ).apply {
                    action =
                        ACTION_HOURLY_UPDATE
                }

            return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )
        }

        /*
         * Наступний запуск завжди:
         *
         * 13:01:00
         * 14:01:00
         * 15:01:00
         *
         * Якщо зараз 13:01:30 —
         * наступний буде 14:01:00,
         * а не 13:01:00.
         */
        private fun nextUpdateTime(): Long {

            val now =
                Calendar.getInstance()

            val next =
                Calendar.getInstance().apply {

                    set(
                        Calendar.SECOND,
                        0
                    )

                    set(
                        Calendar.MILLISECOND,
                        0
                    )

                    set(
                        Calendar.MINUTE,
                        1
                    )

                    set(
                        Calendar.HOUR_OF_DAY,
                        now.get(
                            Calendar.HOUR_OF_DAY
                        ) + 1
                    )
                }

            /*
             * Додатковий захист:
             * якщо через перехід часу або іншу
             * особливість next опинився в минулому,
             * переносимо його ще на годину.
             */
            if (
                next.timeInMillis <=
                now.timeInMillis
            ) {
                next.add(
                    Calendar.HOUR_OF_DAY,
                    1
                )
            }

            return next.timeInMillis
        }

        fun schedule(
            context: Context
        ) {

            val alarmManager =
                context.getSystemService(
                    Context.ALARM_SERVICE
                ) as AlarmManager

            val pi =
                pendingIntent(context)

            val triggerTime =
                nextUpdateTime()

            /*
             * ВАЖЛИВО:
             *
             * Alarm НЕ вимикаємо, якщо
             * showHour == false.
             *
             * Навіть у режимі "Облік в днях"
             * віджет повинен перейти на нову
             * добу.
             */
            try {

                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.S
                ) {

                    if (
                        alarmManager
                            .canScheduleExactAlarms()
                    ) {

                        alarmManager
                            .setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pi
                            )

                    } else {

                        /*
                         * Точний alarm ще недоступний.
                         *
                         * Ставимо резервний alarm,
                         * щоб віджет все одно оновлювався.
                         *
                         * Після надання користувачем
                         * доступу система передасть
                         * ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
                         * і ми знову поставимо точний alarm.
                         */
                        alarmManager
                            .setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pi
                            )
                    }

                } else {

                    alarmManager
                        .setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pi
                        )
                }

            } catch (
                e: SecurityException
            ) {

                /*
                 * Додатковий захист від SecurityException.
                 */
                try {

                    alarmManager
                        .setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pi
                        )

                } catch (
                    ignored: Exception
                ) {
                }
            }
        }

        fun cancel(
            context: Context
        ) {

            val alarmManager =
                context.getSystemService(
                    Context.ALARM_SERVICE
                ) as AlarmManager

            val pi =
                pendingIntent(context)

            alarmManager.cancel(pi)

            pi.cancel()
        }
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        when (intent.action) {

            /*
             * Після перезавантаження телефона.
             */
            Intent.ACTION_BOOT_COMPLETED -> {

                schedule(context)
                return
            }

            /*
             * Після оновлення/перевстановлення
             * APK поверх старої версії.
             */
            Intent.ACTION_MY_PACKAGE_REPLACED -> {

                schedule(context)
                return
            }

            /*
             * Користувач надав доступ
             * "Будильники та нагадування".
             *
             * Відразу ставимо точний alarm.
             */
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> {

                val alarmManager =
                    context.getSystemService(
                        Context.ALARM_SERVICE
                    ) as AlarmManager

                if (
                    Build.VERSION.SDK_INT <
                    Build.VERSION_CODES.S ||
                    alarmManager.canScheduleExactAlarms()
                ) {
                    schedule(context)
                }

                return
            }

            ACTION_CANCEL -> {

                cancel(context)
                return
            }

            ACTION_HOURLY_UPDATE -> {

                /*
                 * Передаємо команду HomeWidget
                 * на backgroundCallback у main.dart.
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

                try {

                    context.sendBroadcast(
                        backgroundIntent
                    )

                } catch (
                    ignored: Exception
                ) {
                }

                /*
                 * AlarmManager використовуємо
                 * одноразовими точними alarm.
                 *
                 * Після кожного спрацювання
                 * встановлюємо наступний.
                 */
                schedule(context)

                return
            }
        }
    }
}
