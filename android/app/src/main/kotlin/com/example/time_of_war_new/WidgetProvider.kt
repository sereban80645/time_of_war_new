package com.example.time_of_war_new

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
        val layoutId = context.resources.getIdentifier(
            "widget_layout", "layout", context.packageName
        )
        val imageId = context.resources.getIdentifier(
            "widget_image", "id", context.packageName
        )
        if (layoutId == 0 || imageId == 0) return

        // Flutter composes the photo background and timer text into ONE bitmap.
        // This avoids Binder transaction overflow from sending two large bitmaps.
        val renderedPath = widgetData.getString("widget_image", null)
            ?: widgetData.getString("filename", null)
            ?: return

        val renderedFile = File(renderedPath)
        if (!renderedFile.exists()) return

        val renderedBitmap = BitmapFactory.decodeFile(renderedFile.absolutePath)
            ?: return

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, layoutId)
            views.setImageViewBitmap(imageId, renderedBitmap)
            views.setOnClickPendingIntent(imageId, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
