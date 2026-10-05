package com.example.time_of_war

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
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
        val packageName = context.packageName
        val layoutId = context.resources.getIdentifier("widget_layout", "layout", packageName)
        if (layoutId == 0) {
            Log.e("TimeOfWarWidget", "widget_layout not found")
            return
        }

        val rootId = context.resources.getIdentifier("widget_root", "id", packageName)
        val imageId = context.resources.getIdentifier("widget_image", "id", packageName)
        val textId = context.resources.getIdentifier("widget_text", "id", packageName)
        val imagePath = widgetData.getString("widget_image", null)
            ?: widgetData.getString("filename", null)

        Log.d("TimeOfWarWidget", "widget_image path=$imagePath")

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(packageName, layoutId)
            views.setViewVisibility(textId, View.VISIBLE)
            views.setTextViewText(textId, "Час Війни")

            if (imagePath != null) {
                val file = File(imagePath)
                if (file.exists() && file.length() > 0L) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null && imageId != 0) {
                        views.setImageViewBitmap(imageId, bitmap)
                        views.setViewVisibility(textId, View.GONE)
                        Log.d("TimeOfWarWidget", "Rendered widget image ${bitmap.width}x${bitmap.height}")
                    } else {
                        Log.e("TimeOfWarWidget", "Could not decode widget image: $imagePath")
                    }
                } else {
                    Log.e("TimeOfWarWidget", "Widget image does not exist: $imagePath")
                }
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (rootId != 0) views.setOnClickPendingIntent(rootId, pendingIntent)
            if (imageId != 0) views.setOnClickPendingIntent(imageId, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
