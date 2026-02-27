package com.example.wherewasmytime.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {
    
    /**
     * Sıkıştırır ve belirtilen çözünürlüğe kadar küçültür.
     * @param context uygulama contexti
     * @param uri orijinal büyük fotoğrafın unisi
     * @return sıkıştırılıp kopyalanan kalıcı dosyanın adresi
     */
    suspend fun compressAndSaveImage(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return@withContext null

            // Maximum bounds
            val MAX_SIZE = 1024
            var width = originalBitmap.width
            var height = originalBitmap.height

            if (width > MAX_SIZE || height > MAX_SIZE) {
                val ratio = width.toFloat() / height.toFloat()
                if (width > height) {
                    width = MAX_SIZE
                    height = (width / ratio).toInt()
                } else {
                    height = MAX_SIZE
                    width = (height * ratio).toInt()
                }
            }

            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)
            
            // Generate filename based on timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "IMG_${timestamp}.jpg"
            val file = File(context.filesDir, filename)

            val fos = FileOutputStream(file)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
            fos.flush()
            fos.close()

            // Belleği temizle
            originalBitmap.recycle()
            if (originalBitmap != scaledBitmap) {
                scaledBitmap.recycle()
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createTempImageFile(context: Context): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File.createTempFile(
            "JPEG_${timestamp}_",
            ".jpg",
            context.cacheDir
        )
    }
}
