package io.github.batmancpu.watchcompanion

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import java.io.OutputStream

data class FaceSource(val width: Int, val height: Int)

/**
 * Produces a standalone PNG preview, NOT a firmware-compatible watch-face package.
 * Output resolution and encoding MUST be verified from the original watch-face protocol.
 * This code has no Bluetooth API and cannot write to a watch.
 */
object FaceLab {
    fun inspect(resolver: ContentResolver, uri: Uri): FaceSource {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: error("Could not open image")
        require(options.outWidth in 1..16384 && options.outHeight in 1..16384) {
            "Image dimensions invalid or too large"
        }
        return FaceSource(options.outWidth, options.outHeight)
    }

    fun supportedSize(width: Int, height: Int): Boolean =
        width in 64..640 && height in 64..640 && width.toLong() * height <= 409600L

    fun renderPng(
        resolver: ContentResolver,
        imageUri: Uri,
        output: OutputStream,
        width: Int,
        height: Int
    ) {
        require(supportedSize(width, height)) { "Choose dimensions from 64 to 640 pixels." }
        val metadata = inspect(resolver, imageUri)
        var sample = 1
        while (metadata.width / sample > 1400 || metadata.height / sample > 1400) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val source = resolver.openInputStream(imageUri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: error("Could not decode selected image")
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(image)
            canvas.drawColor(android.graphics.Color.BLACK)
            val scale = maxOf(width.toFloat() / source.width, height.toFloat() / source.height)
            val newWidth = source.width * scale
            val newHeight = source.height * scale
            val left = (width - newWidth) / 2f
            val top = (height - newHeight) / 2f
            canvas.drawBitmap(
                source, null,
                RectF(left, top, left + newWidth, top + newHeight),
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            )
            check(image.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                "PNG export failed"
            }
            output.flush()
        } finally {
            source.recycle()
            image.recycle()
        }
    }
}
