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

            val renderedPath =
                widgetData.getString(
                    "widget_rendered",
                    null
                )

            val backgroundPath =
                widgetData.getString(
                    "imagePath",
                    null
                )

            var finalBitmap: Bitmap? = null

            /*
             * Load the Flutter-rendered widget.
             */
            if (!renderedPath.isNullOrEmpty()) {

                val renderedFile =
                    File(renderedPath)

                if (
                    renderedFile.exists() &&
                    renderedFile.length() > 0
                ) {
                    finalBitmap =
                        BitmapFactory.decodeFile(
                            renderedFile.absolutePath
                        )
                }
            }

            /*
             * Load the selected background image.
             */
            if (!backgroundPath.isNullOrEmpty()) {

                val backgroundFile =
                    File(backgroundPath)

                if (
                    backgroundFile.exists() &&
                    backgroundFile.length() > 0
                ) {

                    val backgroundBitmap =
                        BitmapFactory.decodeFile(
                            backgroundFile.absolutePath
                        )

                    if (backgroundBitmap != null) {

                        val renderedBitmap =
                            finalBitmap

                        if (renderedBitmap != null) {

                            val width =
                                renderedBitmap.width

                            val height =
                                renderedBitmap.height

                            val composedBitmap =
                                Bitmap.createBitmap(
                                    width,
                                    height,
                                    Bitmap.Config.ARGB_8888
                                )

                            val canvas =
                                Canvas(composedBitmap)

                            val sourceWidth =
                                backgroundBitmap.width
                                    .toFloat()

                            val sourceHeight =
                                backgroundBitmap.height
                                    .toFloat()

                            val targetWidth =
                                width.toFloat()

                            val targetHeight =
                                height.toFloat()

                            val sourceRatio =
                                sourceWidth /
                                    sourceHeight

                            val targetRatio =
                                targetWidth /
                                    targetHeight

                            val srcRect: RectF

                            if (
                                sourceRatio >
                                    targetRatio
                            ) {

                                val cropWidth =
                                    sourceHeight *
                                        targetRatio

                                val left =
                                    (
                                        sourceWidth -
                                            cropWidth
                                    ) / 2f

                                srcRect =
                                    RectF(
                                        left,
                                        0f,
                                        left +
                                            cropWidth,
                                        sourceHeight
                                    )

                            } else {

                                val cropHeight =
                                    sourceWidth /
                                        targetRatio

                                val top =
                                    (
                                        sourceHeight -
                                            cropHeight
                                    ) / 2f

                                srcRect =
                                    RectF(
                                        0f,
                                        top,
                                        sourceWidth,
                                        top +
                                            cropHeight
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

                            /*
                             * Draw selected photo.
                             */
                            canvas.drawBitmap(
                                backgroundBitmap,
                                srcRect,
                                dstRect,
                                paint
                            )

                            /*
                             * Draw Flutter widget on top.
                             */
                            canvas.drawBitmap(
                                renderedBitmap,
                                0f,
                                0f,
                                paint
                            )

                            finalBitmap =
                                composedBitmap

                        } else {

                            /*
                             * If Flutter rendering is unavailable,
                             * show the selected photo.
                             */
                            finalBitmap =
                                backgroundBitmap
                        }
                    }
                }
            }

            /*
             * Put the final bitmap into the widget.
             */
            val bitmapToDisplay =
                finalBitmap

            if (bitmapToDisplay != null) {

                views.setImageViewBitmap(
                    R.id.widget_image,
                    bitmapToDisplay
                )
            }

            /*
             * Open the application when the widget
             * is tapped.
             */
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
    }

    override fun onEnabled(
        context: Context
    ) {
        super.onEnabled(context)

        HourlyWidgetUpdateReceiver
            .schedule(context)
    }

    override fun onDisabled(
        context: Context
    ) {
        HourlyWidgetUpdateReceiver
            .cancel(context)

        super.onDisabled(context)
    }
}
