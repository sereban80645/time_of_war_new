package com.example.time_of_war

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
        val viewsPackage = context.packageName

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(
                viewsPackage,
                R.layout.widget_layout
            )

            val imagePath = widgetData.getString(
                "widget_image",
                null
            ) ?: widgetData.getString(
                "filename",
                null
            )

            if (!imagePath.isNullOrEmpty()) {
                val file = File(imagePath)

                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(
                        file.absolutePath
                    )

                    if (bitmap != null) {
                        views.setImageViewBitmap(
                            R.id.widget_image,
                            bitmap
                        )
                    }
                }
            }

            val intent = Intent(
                context,
                MainActivity::class.java
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
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

            views.setOnClickPendingIntent(
                R.id.widget_image,
                pendingIntent
            )

            appWidgetManager.updateAppWidget(
                appWidgetId,
                views
            )
        }
    }
}
