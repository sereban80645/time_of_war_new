package com.example.time_of_war

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.widget.RemoteViews
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
                    Uri.parse(imagePath)
                )
            }

            appWidgetManager.updateAppWidget(
                appWidgetId,
                views
            )
        }
    }
}
