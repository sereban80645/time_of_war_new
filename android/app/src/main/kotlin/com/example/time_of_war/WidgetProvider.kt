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
        val rootId = context.resources.getIdentifier("widget_root", "id", packageName)
        val imageId = context.resources.getIdentifier("widget_image", "id", packageName)
        val textId = context.resources.getIdentifier("widget_text", "id", packageName)

        Log.d("TimeOfWarWidget", "onUpdate package=$packageName ids=${appWidgetIds.joinToString()}")
        Log.d("TimeOfWarWidget", "resources layout=$layoutId root=$rootId image=$imageId text=$textId")

        if (layoutId == 0 || rootId == 0 || imageId == 0 || textId == 0) {
            Log.e("TimeOfWarWidget", "Widget resources are missing")
            return
        }

        val imagePath = widgetData.getString("widget_image", null)
            ?: widgetData.getString("filename", null)

        Log.d("TimeOfWarWidget", "widget_image path=$imagePath")

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(packageName, layoutId)

            // Native fallback is deliberately visible first. This proves the Android
            // widget itself is rendering even when the Flutter PNG is unavailable.
            views.setViewVisibility(imageId, View.GONE)
            views.setViewVisibility(textId, View.VISIBLE)
            views.setTextViewText(textId, "Час Війни")

            var rendered = false
            if (!imagePath.isNullOrBlank()) {
                val file = File(imagePath)
                Log.d("TimeOfWarWidget", "image exists=${file.exists()} size=${file.length()} path=${file.absolutePath}")
                if (file.exists() && file.length() > 0L) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        views.setImageViewBitmap(imageId, bitmap)
                        views.setViewVisibility(imageId, View.VISIBLE)
                        views.setViewVisibility(textId, View.GONE)
                        rendered = true
                        Log.d("TimeOfWarWidget", "Rendered Flutter bitmap ${bitmap.width}x${bitmap.height}")
                    } else {
                        Log.e("TimeOfWarWidget", "BitmapFactory could not decode $imagePath")
                    }
                } else {
                    Log.e("TimeOfWarWidget", "Flutter widget image does not exist or is empty")
                }
            } else {
                Log.e("TimeOfWarWidget", "No Flutter widget image path in SharedPreferences")
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

            views.setOnClickPendingIntent(rootId, pendingIntent)
            views.setOnClickPendingIntent(imageId, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
            Log.d("TimeOfWarWidget", "updateAppWidget id=$appWidgetId rendered=$rendered")
        }
    }
}
