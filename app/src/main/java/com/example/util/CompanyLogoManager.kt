package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object CompanyLogoManager {

    private const val LOGO_FILE_NAME = "company_logo.png"

    fun getLogoFile(context: Context): File {
        return File(context.filesDir, LOGO_FILE_NAME)
    }

    fun hasCustomLogo(context: Context): Boolean {
        val file = getLogoFile(context)
        return file.exists() && file.length() > 0
    }

    fun getLogoBitmap(context: Context): Bitmap? {
        val file = getLogoFile(context)
        if (!file.exists() || file.length() == 0L) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (_: Exception) {
            null
        }
    }

    fun saveLogoFromUri(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream) ?: return false
                val targetFile = getLogoFile(context)
                FileOutputStream(targetFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
                }
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteCustomLogo(context: Context): Boolean {
        val file = getLogoFile(context)
        return if (file.exists()) {
            file.delete()
        } else {
            true
        }
    }
}
