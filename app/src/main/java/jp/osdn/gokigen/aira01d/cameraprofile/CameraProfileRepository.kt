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

    fun renameProfileFile(oldFileName: String, newFileName: String): Boolean
    {
        if ((oldFileName.isEmpty())||(newFileName.isEmpty()))
        {
            // ファイル名が指定されていないときはエラー応答
            return false
        }
        try
        {
            // アプリ専用の内部ストレージディレクトリからファイルを削除する
            val directory = context.filesDir
            val file = File(directory, oldFileName)
            if (file.exists())
            {
                val fileName = newFileName.removeSuffix(".json")
                return file.renameTo(getUniqueFile(directory, fileName))
            }
        }
        catch (e: Exception)
        {
            Log.v(TAG, "ERR>File Rename: $oldFileName -> $newFileName (${e.localizedMessage})")
        }
        return false
    }

    fun readCameraPropertyFile(fileName: String): List<CameraProperties>
    {
        // ----- ファイルから読み込んで、リストに入れる
        return try
        {
            // 拡張子が付いていない場合は ".json" を付与
            val fullFileName = if (fileName.endsWith(".json")) fileName else "$fileName.json"

            // 対象のファイルオブジェクト
            val file = File(context.filesDir, fullFileName)

            // ファイルが存在しない場合は emptyList を返す
            if (!file.exists()) {
                return emptyList()
            }
            // ファイルの内容（JSON文字列）を読み込み
            val jsonString = file.readText()

            // JSON文字列を ArrayList<CameraProperties> オブジェクトにデコード
            val list: List<CameraProperties> = Json.decodeFromString(jsonString)
            return list
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            emptyList()
        }
    }

    fun setCameraPropertiesToCamera(cameraProperties: List<CameraProperties>): Boolean
    {
        return when (AppSingleton.cameraControl.getCameraConnectionProtocol())
        {
            ICameraConnectionStatus.CameraProtocol.OPC -> { setCameraPropertiesToCameraOpc(cameraProperties) }
            ICameraConnectionStatus.CameraProtocol.OMDS -> { setCameraPropertiesToCameraOmds(cameraProperties) }
        }
    }

    private fun setCameraPropertiesToCameraOpc(cameraProperties: List<CameraProperties>): Boolean
    {
        // ----- OPCカメラ用プロパティ反映ロジック
        var setPropertyCount = 0
        cameraProperties.forEach { cameraProperty ->
            //Log.v(TAG, "key: ${cameraProperty.propertyName} value: ${cameraProperty.value}")

            // ----- 設定要否を確認する
            val descriptor = AppSingleton.cameraControl.getCameraStatus().getDescriptor(cameraProperty.propertyName)
            if ((descriptor.attribute.contains("set"))&&(descriptor.current != cameraProperty.value))
            {
                // ----- カメラプロパティが違うので設定する
                AppSingleton.cameraControl.getCameraStatus().setStatusString(cameraProperty.propertyName, cameraProperty.value)

                Log.v(TAG, "SET PROPERTY(${cameraProperty.propertyName}): ${descriptor.current} -> ${cameraProperty.value}")
                setPropertyCount++

                // ----- ちょっと "待ち" を入れてみる (値設定時)
                Thread.sleep(15L + (0..10).random())
            }
            // ----- ちょっと "待ち" を入れてみる(毎回)
            Thread.sleep(10L + (0..10).random())
        }
        Log.v(TAG, "Set Camera Properties: $setPropertyCount / ${cameraProperties.size}")
        return true
    }

    private fun setCameraPropertiesToCameraOmds(cameraProperties: List<CameraProperties>): Boolean
    {
        // ----- OMDSカメラ用プロパティ反映ロジック（OPCカメラと同じロジック）
        var setPropertyCount = 0
        cameraProperties.forEach { cameraProperty ->
            //Log.v(TAG, "key: ${cameraProperty.propertyName} value: ${cameraProperty.value}")

            // ----- 設定要否を確認する
            val descriptor = AppSingleton.cameraControl.getCameraStatus().getDescriptor(cameraProperty.propertyName)
            if ((descriptor.attribute.contains("set"))&&(descriptor.current != cameraProperty.value))
            {
                // ----- カメラプロパティが違うので設定する
                AppSingleton.cameraControl.getCameraStatus().setStatusString(cameraProperty.propertyName, cameraProperty.value)
                Log.v(TAG, "SET PROPERTY(${cameraProperty.propertyName}): ${descriptor.current} -> ${cameraProperty.value}")
                setPropertyCount++

                // ----- ちょっと "待ち" を入れてみる (値設定時)
                Thread.sleep(15L + (0..10).random())
            }
            // ----- ちょっと "待ち" を入れてみる(毎回)
            Thread.sleep(10L + (0..10).random())
        }
        Log.v(TAG, "Set Camera Properties: $setPropertyCount / ${cameraProperties.size}")
        return true
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
            // ----- 途中で通信エラーが発生した場合は取得失敗にする
            return emptyList()
        }
        return propertiesList
    }

    private fun getAllCameraPropertiesOmds() : List<CameraProperties>
    {
        // ----- プロパティの全件を取得する (OMDS用)
        var exceptionCount = 0
        val propertiesList = ArrayList<CameraProperties>()
        var cgiString = ""
        val rawCommandList = AppSingleton.cameraControl.getCameraStatus().getRawCommandList()
        val startPosIndex = rawCommandList.indexOf(string = "<cgi name=\"get_camprop\">", ignoreCase = true)
        if (startPosIndex > 0) {
            val endPosIndex = rawCommandList.indexOf(string =  "</cgi>", startIndex = startPosIndex, ignoreCase = true) + "</cgi>".length
            cgiString = rawCommandList.substring(startPosIndex, endPosIndex)
        }
        if (cgiString.isEmpty())
        {
            // ----- コマンドプロパティリストが取れなかった
            return emptyList()
        }

        // --- 正規表現で <param2 name="xxx" を見つけるパターンを定義
        val regex = """<param2\s+name="([^"]+)"\s*/?>""".toRegex()

        // --- マッチした全ての抽出結果からキャプチャグループ（nameの中身）をSetとして取得
        val propertyNames: Set<String> = regex.findAll(cgiString)
            .map { matchResult -> matchResult.groupValues[1] } // キャプチャグループ1（名前部分）を取得
            .toSet() // 自動的に重複を排除してSetに変換

        propertyNames.forEach { propertyName ->
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
            // ----- 途中で通信エラーが発生した場合は取得失敗にする
            return emptyList()
        }
        return propertiesList
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
