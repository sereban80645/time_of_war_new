package com.example.time_of_war_new

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
        val layoutId = res.getIdentifier("widget_layout", "layout", context.packageName)
        val rootId = res.getIdentifier("widget_root", "id", context.packageName)
        val backgroundId = res.getIdentifier("widget_background", "id", context.packageName)
        val imageId = res.getIdentifier("widget_image", "id", context.packageName)
        if (layoutId == 0) return
        val backgroundPath = widgetData.getString("widget_background_source_path", null)
        val renderedPath = widgetData.getString("widget_image", null) ?: widgetData.getString("filename", null)
        val backgroundBitmap = backgroundPath?.let { path -> File(path).let { if (it.exists()) BitmapFactory.decodeFile(it.absolutePath) else null } }
        val renderedBitmap = renderedPath?.let { path -> File(path).let { if (it.exists()) BitmapFactory.decodeFile(it.absolutePath) else null } }
        if (renderedBitmap == null && backgroundBitmap == null) return
        val bgColor = try { widgetData.getInt("widget_background_color", 0xFF1E1E1E.toInt()) } catch (_: ClassCastException) {
            widgetData.getString("widget_background_color", null)?.toLongOrNull()?.toInt() ?: 0xFF1E1E1E.toInt()
        }
        val opacity = try { widgetData.getFloat("widget_background_opacity", 0.5f) } catch (_: ClassCastException) {
            widgetData.getString("widget_background_opacity", null)?.toFloatOrNull() ?: 0.5f
        }
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, layoutId)
            if (rootId != 0) views.setInt(rootId, "setBackgroundColor", bgColor)
            if (backgroundBitmap != null && backgroundId != 0) {
                views.setImageViewBitmap(backgroundId, backgroundBitmap)
                views.setViewVisibility(backgroundId, android.view.View.VISIBLE)
                views.setInt(backgroundId, "setImageAlpha", (opacity.coerceIn(0f, 1f) * 255f).toInt())
            }
            if (renderedBitmap != null && imageId != 0) {
                views.setImageViewBitmap(imageId, renderedBitmap)
                views.setViewVisibility(imageId, android.view.View.VISIBLE)
            } else if (imageId != 0) continue
            val intent = Intent()
            intent.setClassName(context, "com.example.time_of_war_new.MainActivity")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            if (rootId != 0) views.setOnClickPendingIntent(rootId, pendingIntent)
            if (backgroundId != 0) views.setOnClickPendingIntent(backgroundId, pendingIntent)
            if (imageId != 0) views.setOnClickPendingIntent(imageId, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}