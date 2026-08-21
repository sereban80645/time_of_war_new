package com.example.time_of_war

import android.appwidget.AppWidgetManager
import android.content.Context
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
        for (appWidgetId in appWidgetIds) {

            val views = RemoteViews(
                context.packageName,
                R.layout.widget_layout
            )

            /*
             * Основне зображення, яке генерується
             * Flutter через HomeWidget.renderFlutterWidget().
             */
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

                    val bitmap =
                        BitmapFactory.decodeFile(
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

            /*
             * Оновлюємо сам віджет.
             */
            appWidgetManager.updateAppWidget(
                appWidgetId,
                views
            )
        }
    }
}
