package com.whoareyou.app

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

object ShareFileStore {
    private const val MAX_CACHED_SHARE_FILES = 8

    fun writePng(context: Context, fileName: String, bitmap: Bitmap): File {
        val directory = File(context.cacheDir, "shared_results")
        check(directory.exists() || directory.mkdirs()) {
            "Unable to prepare share cache directory"
        }

        val file = File(directory, fileName)
        FileOutputStream(file).use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                "Unable to encode share image"
            }
        }

        directory.listFiles()
            ?.filter { it.isFile && it != file }
            ?.sortedByDescending(File::lastModified)
            ?.drop(MAX_CACHED_SHARE_FILES - 1)
            ?.forEach { stale -> runCatching { stale.delete() } }

        return file
    }
}
