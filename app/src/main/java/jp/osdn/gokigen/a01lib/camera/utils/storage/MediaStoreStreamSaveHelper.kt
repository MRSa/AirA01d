package jp.osdn.gokigen.a01lib.camera.utils.storage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MediaStoreStreamSaveHelper(
    private val context: Context,
    private val fileName: String
) {
    private var outputStream: OutputStream? = null
    private var targetUri: Uri? = null
    private var targetFile: File? = null
    private val isVideo = fileName.endsWith(".MOV", ignoreCase = true)
    private val isRaw = fileName.endsWith(".ORF", ignoreCase = true)
    private val mimeType = if (isVideo) "video/mp4" else if (isRaw) "image/x-olympus-orf" else "image/jpeg"
    private val relativePath = "${Environment.DIRECTORY_DCIM}/AirA01d"

    // ----- ダウンロード開始時にファイルをオープンする
    fun open(): Boolean
    {
        Log.v(TAG, "- - - Store Start : $fileName")
        return try {
            val resolver = context.contentResolver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.TITLE, fileName)
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val collection = if (isVideo) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                }
                targetUri = resolver.insert(collection, contentValues)
                if (targetUri != null) {
                    outputStream = resolver.openOutputStream(targetUri!!)
                }
            } else {
                // Android 9以下
                val dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                val targetFolder = File(dcimDir, "AirA01d")
                if (!targetFolder.exists()) targetFolder.mkdirs()

                targetFile = File(targetFolder, fileName)
                outputStream = FileOutputStream(targetFile)
            }
            outputStream != null
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            false
        }
    }

    // --- 逐次データが届くたびに書き込む
    fun write(data: ByteArray)
    {
        try
        {
            outputStream?.write(data)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    // --- ダウンロード完了時にストリームを閉じ、Pending状態を解除する
    fun close(success: Boolean)
    {
        try
        {
            outputStream?.flush()
            outputStream?.close()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        finally
        {
            outputStream = null
        }

        val resolver = context.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            targetUri?.let { uri ->
                if (success)
                {
                    // 成功した場合は IS_PENDING を 0 にしてシステムに登録
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.IS_PENDING, 0)
                    }
                    resolver.update(uri, contentValues, null, null)
                }
                else
                {
                    // 途中で失敗した場合は、不完全なファイルを削除
                    resolver.delete(uri, null, null)
                }
            }
        }
        else
        {
            // --- Android 9以下
            targetFile?.let { file ->
                if (success) {
                    @Suppress("DEPRECATION")
                    android.media.MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null, null)
                } else {
                    if (file.exists()) file.delete()
                }
            }
        }
        Log.v(TAG, "- - - Store Finish : $fileName")
    }

    companion object {
        private val TAG = MediaStoreStreamSaveHelper::class.java.simpleName
    }
}
