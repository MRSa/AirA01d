package jp.osdn.gokigen.aira01d.cameraprofile

import android.content.Context
import android.net.Uri
import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.aira01d.AppSingleton
import jp.osdn.gokigen.aira01d.R
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream

@Serializable
data class CameraProperties(
    val propertyName: String,
    val value: String
)

class CameraProfileRepository(private val context: Context)
{

    fun storeAllCameraProfiles(fileNameToPull: String): Boolean
    {
        if (fileNameToPull.isEmpty())
        {
            // ファイル名が指定されていないときはエラー応答
            return false
        }

        val properties = when (AppSingleton.cameraControl.getCameraConnectionProtocol())
        {
            ICameraConnectionStatus.CameraProtocol.OPC -> { getAllCameraPropertiesOpc() }
            ICameraConnectionStatus.CameraProtocol.OMDS -> { getAllCameraPropertiesOmds() }
        }
        if (properties.isEmpty())
        {
            // ----- カメラプロパティの取得に失敗した
            return false
        }
        // --- カメラプロパティの取得に成功した。指定されたファイル名で、ローカルストレージ（ファイル）に保存する。
        Log.v(TAG, "Properties: ${properties.size}")

        try {
            // 保存用ディレクトリ（アプリ専用の内部ストレージ: filesDir）
            val directory = context.filesDir

            // 同名ファイルが存在する場合に重複しないファイル名を生成
            val targetFile = getUniqueFile(directory, fileNameToPull)

            // リストデータをJSON文字列に変換
            val jsonString = Json.encodeToString(properties)

            // ファイルに書き込み
            targetFile.writeText(jsonString)

            return true
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return false
    }

    fun deleteCameraProfile(fileNameToDelete: String): Boolean
    {
        if (fileNameToDelete.isEmpty())
        {
            // ファイル名が指定されていないときはエラー応答
            return false
        }
        try
        {
            // アプリ専用の内部ストレージディレクトリからファイルを削除する
            val directory = context.filesDir
            val file = File(directory, fileNameToDelete)
            if (file.exists())
            {
                return file.delete()
            }
        }
        catch (e: Exception)
        {
            Log.v(TAG, "ERR>File Delete: $fileNameToDelete (${e.localizedMessage})")
        }
        return false
    }

    fun exportCameraPropertyFile(fileName: String, destinationUri: Uri): Boolean
    {
        // ----- ファイルエクスポート実処理
        try
        {
            // 引数で受け取った fileName を使用
            val internalFile = File(context.filesDir, fileName)
            if (!internalFile.exists()) {
                // ファイルがない...エクスポート失敗
                return false
            }
            FileInputStream(internalFile).use { inputStream ->
                context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
        catch (e: Exception)
        {
            Log.v(TAG, "ERR>$fileName to $destinationUri : ${e.localizedMessage}")
            return false
        }
        return true
    }

    private fun getAllCameraPropertiesOpc() : List<CameraProperties>
    {
        // ----- プロパティの全件を取得する (OPC用)
        var exceptionCount = 0
        val propertiesList = ArrayList<CameraProperties>()
        val propertiesArray = context.resources.getStringArray(R.array.opc_properties_key)
        propertiesArray.forEach { propertyName ->
            try
            {
                val propertyValue = getPropertyValue(propertyName)
                if (propertyValue.isNotEmpty())
                {
                    propertiesList.add(CameraProperties(propertyName, propertyValue))
                }
            }
            catch (e: Exception)
            {
                Log.v(TAG, "ERR>GET Property: $propertyName : ${e.localizedMessage}")
                exceptionCount++
            }
        }
        if (exceptionCount > 0)
        {
            return emptyList()
        }
        return propertiesList
    }

    private fun getAllCameraPropertiesOmds() : List<CameraProperties>
    {
        // ----- プロパティの全件を取得する (OMDS用)
        //val propertyValue = getPropertyValue("takemode")

        return emptyList()
    }

    private fun getPropertyValue(propertyName: String): String
    {
        try
        {
            val responseString = AppSingleton.cameraControl.getCameraStatus().getStatusString(propertyName)

            // --- 応答 getPropertyValue(TAKEMODE) : 200 <?xml version="1.0"?><get><value>A</value></get>
            //Log.v(TAG, "getPropertyValue($propertyName) : $responseString")

            val propertyValueIndex = responseString.indexOf("<value>") + "<value>".length
            val propertyValueLastIndex = responseString.indexOf("</value>")
            if ((propertyValueIndex > 0)&&(propertyValueIndex < propertyValueLastIndex))
            {
                return responseString.substring(propertyValueIndex, propertyValueLastIndex)
            }
        }
        catch (e: Exception)
        {
            Log.v(TAG, "get Property: $propertyName : ${e.localizedMessage}")
        }
        return ""
    }

    private fun getUniqueFile(directory: File, baseName: String, extension: String = "json"): File
    {
        // 重複しないファイル名を生成するヘルパー関数
        // 例: "properties01.json" がある場合 -> "properties01-1.json" -> "properties01-2.json"
        var file = File(directory, "$baseName.$extension")
        var count = 1

        while (file.exists()) {
            file = File(directory, "$baseName-$count.$extension")
            count++
        }
        return file
    }

    fun getSavedFileList(extensionFilter: String? = "json"): ArrayList<String>
    {
        // --- 内部ストレージ内のファイル名一覧を取得し、ArrayList<String> で返す
        val fileNamesList = ArrayList<String>()

        // アプリ専用の内部ストレージディレクトリ
        val directory = context.filesDir

        // ディレクトリ内のファイル一覧を取得
        val files: Array<File>? = directory.listFiles()

        if (files != null) {
            // 名前順にソートする
            val sortedFiles = files.sortedBy { it.name }

            for (file in sortedFiles) {
                // ファイルであり、かつ隠しファイル（.で始まるもの）でない場合
                if (file.isFile && !file.isHidden) {
                    // 拡張子の指定がある場合はフィルタリング
                    if (extensionFilter.isNullOrEmpty() || file.extension == extensionFilter) {
                        fileNamesList.add(file.name)
                    }
                }
            }
        }
        return fileNamesList
    }

    companion object {
        private val TAG = CameraProfileRepository::class.java.simpleName
    }
}
