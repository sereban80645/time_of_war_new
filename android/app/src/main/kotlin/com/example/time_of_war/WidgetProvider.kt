package com.example.time_of_war

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.SharedPreferences
import android.widget.RemoteViews
import android.graphics.BitmapFactory
import android.app.PendingIntent
import android.content.Intent
import es.antonborri.home_widget.HomeWidgetProvider
import java.io.File

class WidgetProvider : HomeWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray, widgetData: SharedPreferences) {
        val res = context.resources
        val layoutId = res.getIdentifier("widget_layout", "layout", "com.example.time_of_war")
        val rootId = res.getIdentifier("widget_root", "id", "com.example.time_of_war")
        val imageId = res.getIdentifier("widget_image", "id", "com.example.time_of_war")
        val textId = res.getIdentifier("widget_text", "id", "com.example.time_of_war")

        if (layoutId == 0) return

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews("com.example.time_of_war", layoutId)

            val imagePath = widgetData.getString("widget_image", null) ?: widgetData.getString("filename", null)
            if (imagePath != null) {
                val file = File(imagePath)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null && imageId != 0) {
                        views.setImageViewBitmap(imageId, bitmap)
                        // Ховаємо текст-підказку, коли картинка готова
                        if (textId != 0) views.setViewVisibility(textId, android.view.View.GONE)
                    }
                }
            }

            // ПРЯМИЙ виклик прихованого MainActivity замість стандартного
            val intent = Intent()
            intent.setClassName(context, "com.example.time_of_war.MainActivity")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            val pendingIntent = PendingIntent.getActivity(
                context, 
                0, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            if (rootId != 0) views.setOnClickPendingIntent(rootId, pendingIntent)
            if (imageId != 0) views.setOnClickPendingIntent(imageId, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
