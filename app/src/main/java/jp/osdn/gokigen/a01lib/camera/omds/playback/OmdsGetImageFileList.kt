package jp.osdn.gokigen.a01lib.camera.omds.playback

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.IPlaybackControl
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.util.Calendar

class OmdsGetImageFileList(
    userAgent: String = "OlympusCameraKit",
    executeUrl: String = "http://192.168.0.10",
    private val timeoutMs: Int = TIMEOUT_MS
) {
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    // --- URLの末尾がスラッシュで終わるように補正（安全対策）
    private val baseUrl = if (executeUrl.endsWith("/")) executeUrl else "$executeUrl/"

    // --- 外部から getter/setter としてアクセスできる Kotlin スタイルのプロパティ
    var useOpcProtocol: Boolean = true

    init {
        headerMap["User-Agent"] = userAgent
        headerMap["X-Protocol"] = userAgent
    }

    /**
     * 指定したディレクトリのファイル一覧を取得します。
     */
    fun getImageFileList(directory: String): List<IPlaybackControl.ImageFileInfo> {
        try {
            // コマンドの組み立て
            val command = if (useOpcProtocol) GET_IMAGELIST_COMMAND_OPC else GET_IMAGELIST_COMMAND
            val cleanCommand = command.removePrefix("/")

            // 例: http://192.168.0.10/get_imglist.cgi?DIR=/DCIM/100OLYMP などの形式に整形
            val commandUrl = "${baseUrl}${cleanCommand}?DIR=$directory"

            val response = http.httpGetWithHeader(commandUrl, headerMap, null, timeoutMs)
            if (!response.isNullOrEmpty()) {
                return parseFileList(response)
            }
        } catch (e: Exception) {
            Log.e(TAG, "ERR> GET FILE INFO: ${e.localizedMessage}", e)
        }
        return emptyList()
    }

    /**
     * レスポンス文字列をパースして ImageFileInfo のリストに変換します。
     */
    private fun parseFileList(content: String): List<IPlaybackControl.ImageFileInfo> {
        val fileList = mutableListOf<IPlaybackControl.ImageFileInfo>()

        content.lines().forEachIndexed { index, line ->
            val lineNumber = index + 1
            val trimmedLine = line.trim()

            // 空行は正常系として静かにスキップ
            if (trimmedLine.isBlank()) {
                return@forEachIndexed
            }

            // バージョンヘッダー（VER_100など）もログなしでスキップ
            if (trimmedLine.startsWith("VER_")) {
                return@forEachIndexed
            }

            // カンマ区切りでトークンに分割
            val tokens = trimmedLine.split(",").map { it.trim() }
            if (tokens.size < 6) {
                Log.w(TAG, "Line $lineNumber: Skipped due to insufficient elements. (Tokens: ${tokens.size})")
                return@forEachIndexed
            }

            try
            {
                val directory = tokens[0]
                val fileName = tokens[1]
                val fileSize = tokens[2].toLong()
                val attrInt = tokens[3].toInt()
                val dateInt = tokens[4].toInt()
                val timeInt = tokens[5].toInt()

                // 属性 (attribute) のビット判定
                val isReadonly = (attrInt and 0x01) != 0
                val isHidden = (attrInt and 0x02) != 0
                val isSystem = (attrInt and 0x04) != 0
                val isVolume = (attrInt and 0x08) != 0
                val isDirectory = (attrInt and 0x10) != 0
                val isArchive = (attrInt and 0x20) != 0

                // 日付 (date) のパース
                val day = dateInt and 0x1F
                val month = ((dateInt shr 5) and 0x0F) - 1 // Calendarは0始まり
                val year = ((dateInt shr 9) and 0x7F) + 1980

                // 時間 (time) のパース
                val second = (timeInt and 0x1F) * 2
                val minute = (timeInt shr 5) and 0x3F
                val hour = (timeInt shr 11) and 0x1F

                // java.util.Date の組み立て
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month.coerceIn(0, 11))
                    set(Calendar.DAY_OF_MONTH, day.coerceIn(1, 31))
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, second)
                    set(Calendar.MILLISECOND, 0)
                }

                // データクラスへマッピングして追加
                fileList.add(
                    IPlaybackControl.ImageFileInfo(
                        directory = directory,
                        fileName = fileName,
                        fileSize = fileSize,
                        isReadonly = isReadonly,
                        isHidden = isHidden,
                        isSystem = isSystem,
                        isVolume = isVolume,
                        isDirectory = isDirectory,
                        isArchive = isArchive,
                        dateTime = calendar.time
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing file info at line $lineNumber: \"$trimmedLine\"", e)
            }
        }
        return fileList
    }

    companion object {
        private val TAG = OmdsGetImageFileList::class.java.simpleName
        private const val TIMEOUT_MS = 5500

        private const val GET_IMAGELIST_COMMAND = "get_imglist.cgi"
        private const val GET_IMAGELIST_COMMAND_OPC = "get_imglist.cgi"
    }
}
