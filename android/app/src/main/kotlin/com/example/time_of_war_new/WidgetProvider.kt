package com.example.time_of_war_new

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.widget.RemoteViews
import es.antonborri.home_widget.HomeWidgetProvider
import java.io.File
import java.util.Calendar
import kotlin.math.max

class WidgetProvider : HomeWidgetProvider() {
    companion object {
        private const val WIDTH = 800
        private const val HEIGHT = 400
        private fun bool(data: SharedPreferences, key: String, default: Boolean) = data.getString(key, null)?.toBooleanStrictOrNull() ?: default
        private fun float(data: SharedPreferences, key: String, default: Float) = data.getString(key, null)?.toFloatOrNull() ?: default
        private fun int(data: SharedPreferences, key: String, default: Int) = data.getString(key, null)?.toIntOrNull() ?: default

        private fun timeText(start: Calendar, now: Calendar, daysOnly: Boolean, showHour: Boolean): String {
            if (daysOnly) {
                val totalDays = ((now.timeInMillis - start.timeInMillis) / 86_400_000L).toInt()
                var hours = now.get(Calendar.HOUR_OF_DAY) - start.get(Calendar.HOUR_OF_DAY)
                val minutes = now.get(Calendar.MINUTE) - start.get(Calendar.MINUTE)
                if (minutes < 0) hours--
                if (hours < 0) hours += 24
                return if (showHour) "${totalDays}Ð´. ${hours}Ð³." else "${totalDays}Ð´."
            }
            var years = now.get(Calendar.YEAR) - start.get(Calendar.YEAR)
            var months = now.get(Calendar.MONTH) - start.get(Calendar.MONTH)
            var days = now.get(Calendar.DAY_OF_MONTH) - start.get(Calendar.DAY_OF_MONTH)
            var hours = now.get(Calendar.HOUR_OF_DAY) - start.get(Calendar.HOUR_OF_DAY)
            var minutes = now.get(Calendar.MINUTE) - start.get(Calendar.MINUTE)
            if (minutes < 0) { hours--; minutes += 60 }
            if (hours < 0) { days--; hours += 24 }
            if (days < 0) {
                months--
                val previousMonth = Calendar.getInstance().apply {
                    set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 1)
                    add(Calendar.DAY_OF_MONTH, -1)
                }
                days += previousMonth.get(Calendar.DAY_OF_MONTH)
            }
            if (months < 0) { years--; months += 12 }
            return "${years}Ñ. ${months}Ð¼ÑÑ. ${days}Ð´. ${hours}Ð³."
        }

        private fun startCalendar(year: Int, month: Int, day: Int, hour: Int, minute: Int) =
            Calendar.getInstance().apply {
                set(Calendar.YEAR, year); set(Calendar.MONTH, month - 1); set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }

        private fun buildWidgetBitmap(data: SharedPreferences): Bitmap {
            val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val opacity = float(data, "native_opacity", 0.5f).coerceIn(0f, 1f)
            canvas.drawColor(Color.argb((opacity * 255f).toInt(), int(data, "native_br", 30), int(data, "native_bg", 30), int(data, "native_bb", 30)))

            val imagePath = data.getString("widget_background_source_path", null)
            if (!imagePath.isNullOrBlank()) {
                val file = File(imagePath)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)?.let { source ->
                        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = (opacity * 255f).toInt() }
                        val srcRatio = source.width.toFloat() / source.height.toFloat()
                        val dstRatio = WIDTH.toFloat() / HEIGHT.toFloat()
                        val srcRect = if (srcRatio > dstRatio) {
                            val cropWidth = (source.height * dstRatio).toInt()
                            val left = (source.width - cropWidth) / 2
                            android.graphics.Rect(left, 0, left + cropWidth, source.height)
                        } else {
                            val cropHeight = (source.width / dstRatio).toInt()
                            val top = (source.height - cropHeight) / 2
                            android.graphics.Rect(0, top, source.width, top + cropHeight)
                        }
                        canvas.drawBitmap(source, srcRect, android.graphics.Rect(0, 0, WIDTH, HEIGHT), paint)
                        source.recycle()
                    }
                }
            }

            val textColor = Color.rgb(int(data, "native_tr", 255), int(data, "native_tg", 255), int(data, "native_tb", 255))
            val strokeColor = Color.rgb(int(data, "native_sr", 0), int(data, "native_sg", 0), int(data, "native_sb", 0))
            val fontSize = float(data, "native_fontSize", 22f) * 2.5f
            val strokeWidth = float(data, "native_strokeWidth", 3f) * 2.5f
            val show2022 = bool(data, "native_show2022", true)
            val show2014 = bool(data, "native_show2014", false)
            val showHour = bool(data, "native_showHour", true)
            val daysOnly = bool(data, "native_showDaysOnly", false)
            val now = Calendar.getInstance()
            val rows = mutableListOf<Pair<String, String>>()
            if (show2022) rows.add("ÐÐ¾Ð²Ð½Ð¾Ð¼Ð°ÑÑÑÐ°Ð±Ð½Ð° Ð²ÑÐ¹Ð½Ð°:" to timeText(startCalendar(2022, 2, 24, 2, 40), now, daysOnly, showHour))
            if (show2014) rows.add("ÐÑÐ¹Ð½Ð° Ð· 2014 ÑÐ¾ÐºÑ:" to timeText(startCalendar(2014, 2, 20, 12, 0), now, daysOnly, showHour))

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(180, 255, 255, 255); textSize = fontSize * 0.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL); textAlign = Paint.Align.CENTER
            }
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor; textSize = fontSize
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER
            }
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = strokeColor; textSize = fontSize
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER
                style = Paint.Style.STROKE; this.strokeWidth = max(0f, strokeWidth)
            }

            val rowHeight = if (rows.size == 2) 170f else 125f
            val totalHeight = rows.size * rowHeight
            var centerY = HEIGHT / 2f - totalHeight / 2f + 32f
            rows.forEach { row ->
                canvas.drawText(row.first, WIDTH / 2f, centerY, titlePaint)
                canvas.drawText(row.second, WIDTH / 2f, centerY + fontSize + 8f, strokePaint)
                canvas.drawText(row.second, WIDTH / 2f, centerY + fontSize + 8f, fillPaint)
                centerY += rowHeight
            }
            return bitmap
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray, widgetData: SharedPreferences) {
        val layoutId = context.resources.getIdentifier("widget_layout", "layout", context.packageName)
        val imageId = context.resources.getIdentifier("widget_image", "id", context.packageName)
        if (layoutId == 0 || imageId == 0) return

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val renderedBitmap = buildWidgetBitmap(widgetData)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, layoutId)
            views.setImageViewBitmap(imageId, renderedBitmap)
            views.setOnClickPendingIntent(imageId, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        renderedBitmap.recycle()
    }
}
