package com.example.time_of_war

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.SharedPreferences
import android.widget.RemoteViews
import androidx.core.net.toUri
import es.antonborri.home_widget.HomeWidgetProvider

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

            val imagePath = widgetData.getString(
                "widget_image",
                null
            )

            if (!imagePath.isNullOrEmpty()) {
                views.setImageViewUri(
                    R.id.widget_image,
                    imagePath.toUri()
                )
            }

            appWidgetManager.updateAppWidget(
                appWidgetId,
                views
            )
        }
    }
}
