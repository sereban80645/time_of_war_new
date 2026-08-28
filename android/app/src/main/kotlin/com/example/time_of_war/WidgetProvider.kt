package com.example.time_of_war

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import es.antonborri.home_widget.HomeWidgetProvider
import java.io.File

class WidgetProvider : HomeWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        widgetData: SharedPreferences
    ) {
        for (appWidgetId in appWidgetIds) {

            val views = RemoteViews(
                context.packageName,
                R.layout.widget_layout
            )

            /*
             * Flutter вже генерує ГОТОВИЙ PNG:
             *
             * - фон
             * - фотографія
             * - прозорість
             * - два лічильники
             * - текст
             * - контур
             *
             * Тому тут більше НЕ складаємо
             * backgroundBitmap + renderedBitmap.
             */

            val renderedPath = widgetData.getString(
                "widget_rendered",
                null
            )

            var finalBitmap: Bitmap? = null

            if (!renderedPath.isNullOrEmpty()) {

                val renderedFile = File(renderedPath)

                if (
                    renderedFile.exists() &&
                    renderedFile.length() > 0
                ) {
                    finalBitmap =
                        BitmapFactory.decodeFile(
                            renderedFile.absolutePath
                        )
                }
            }

            /*
             * Якщо готовий Flutter PNG існує —
             * саме його показуємо у віджеті.
             */
            if (finalBitmap != null) {

                views.setImageViewBitmap(
                    R.id.widget_image,
                    finalBitmap
                )
            }

            /*
             * Натискання на віджет відкриває застосунок.
             */
            val intent =
                Intent(
                    context,
                    MainActivity::class.java
                ).apply {
                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                }

            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
                )

            views.setOnClickPendingIntent(
                R.id.widget_root,
                pendingIntent
            )

            appWidgetManager.updateAppWidget(
                appWidgetId,
                views
            )
        }

        /*
         * Після створення/оновлення віджета
         * переконуємося, що погодинний alarm
         * встановлений.
         */
        HourlyWidgetUpdateReceiver.schedule(context)
    }

    override fun onEnabled(
        context: Context
    ) {
        super.onEnabled(context)

        HourlyWidgetUpdateReceiver.schedule(
            context
        )
    }

    override fun onDisabled(
        context: Context
    ) {
        HourlyWidgetUpdateReceiver.cancel(
            context
        )

        super.onDisabled(context)
    }
}
