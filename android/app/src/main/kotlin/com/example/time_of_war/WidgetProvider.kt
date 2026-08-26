package com.example.time_of_war

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
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

            val renderedPath = widgetData.getString(
                "widget_rendered",
                null
            )

            val backgroundPath = widgetData.getString(
                "imagePath",
                null
            )

            var finalBitmap: Bitmap? = null

            if (!renderedPath.isNullOrEmpty()) {
                val renderedFile = File(renderedPath)

                if (
                    renderedFile.exists() &&
                    renderedFile.length() > 0
                ) {
                    finalBitmap = BitmapFactory.decodeFile(
                        renderedFile.absolutePath
                    )
                }
            }

            if (!backgroundPath.isNullOrEmpty()) {
                val backgroundFile = File(backgroundPath)

                if (
                    backgroundFile.exists() &&
                    backgroundFile.length() > 0
                ) {
                    val backgroundBitmap =
                        BitmapFactory.decodeFile(
                            backgroundFile.absolutePath
                        )

                    if (backgroundBitmap != null) {

                        val renderedBitmap = finalBitmap

                        if (renderedBitmap != null) {

                            val width = renderedBitmap.width
                            val height = renderedBitmap.height

                            val composedBitmap =
                                Bitmap.createBitmap(
                                    width,
                                    height,
                                    Bitmap.Config.ARGB_8888
                                )

                            val canvas =
                                Canvas(composedBitmap)

                            val sourceWidth =
                                backgroundBitmap.width.toFloat()

                            val sourceHeight =
                                backgroundBitmap.height.toFloat()

                            val targetWidth =
                                width.toFloat()

                            val targetHeight =
                                height.toFloat()

                            val sourceRatio =
                                sourceWidth / sourceHeight

                            val targetRatio =
                                targetWidth / targetHeight

                            val srcRect: Rect

                            if (sourceRatio > targetRatio) {

                                val cropWidth =
                                    sourceHeight * targetRatio

                                val left =
                                    (sourceWidth - cropWidth) / 2f

                                srcRect = Rect(
                                    left.toInt(),
                                    0,
                                    (left + cropWidth).toInt(),
                                    sourceHeight.toInt()
                                )

                            } else {

                                val cropHeight =
                                    sourceWidth / targetRatio

                                val top =
                                    (sourceHeight - cropHeight) / 2f

                                srcRect = Rect(
                                    0,
                                    top.toInt(),
                                    sourceWidth.toInt(),
                                    (top + cropHeight).toInt()
                                )
                            }

                            val dstRect =
                                RectF(
                                    0f,
                                    0f,
                                    targetWidth,
                                    targetHeight
                                )

                            val paint =
                                Paint(
                                    Paint.ANTI_ALIAS_FLAG
                                )

                            canvas.drawBitmap(
                                backgroundBitmap,
                                srcRect,
                                dstRect,
                                paint
                            )

                            canvas.drawBitmap(
                                renderedBitmap,
                                0f,
                                0f,
                                paint
                            )

                            finalBitmap =
                                composedBitmap

                        } else {

                            finalBitmap =
                                backgroundBitmap
                        }
                    }
                }
            }

            if (finalBitmap != null) {

                views.setImageViewBitmap(
                    R.id.widget_image,
                    finalBitmap
                )
            }

            val intent =
                Intent(
                    context,
                    MainActivity::class.java
                ).apply {
                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                }

            val pendingIntent =
                PendingIntent.getActivity(
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

            appWidgetManager.updateAppWidget(
                appWidgetId,
                views
            )
        }

        /*
         * Запускаємо/перезапускаємо точний
         * погодинний AlarmManager.
         */
        HourlyWidgetUpdateReceiver.schedule(context)
    }

    override fun onEnabled(
        context: Context
    ) {
        super.onEnabled(context)

        HourlyWidgetUpdateReceiver.schedule(
            context
        )
    }

    override fun onDisabled(
        context: Context
    ) {
        HourlyWidgetUpdateReceiver.cancel(
            context
        )

        super.onDisabled(context)
    }
}
