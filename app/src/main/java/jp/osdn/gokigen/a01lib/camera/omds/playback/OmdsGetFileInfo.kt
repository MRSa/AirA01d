package jp.osdn.gokigen.a01lib.camera.omds.playback

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.ICameraFileInfo
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.IStillImageFileInfo
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OmdsGetFileInfo(
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

    fun getMovieFileInfo(directory: String): ICameraFileInfo.MovieFileInfo
    {
        try {
            val command = if (useOpcProtocol) GET_MOVIE_FILE_INFO_COMMAND_OPC else GET_MOVIE_FILE_INFO_COMMAND
            val cleanCommand = command.removePrefix("/")
            val commandUrl = "${baseUrl}${cleanCommand}?DIR=$directory"

            val response = http.httpGetWithHeader(commandUrl, headerMap, null, timeoutMs)
            if (!response.isNullOrEmpty()) {
                return parseMovieFileInfo(response)
            }
        } catch (e: Exception) {
            Log.e(TAG, "ERR> GET FILE INFO: ${e.localizedMessage}", e)
        }
        return ICameraFileInfo.MovieFileInfo(0, "0x0", false, Date(0))
    }

    fun getStillImageFileInfo(directory: String): IStillImageFileInfo.StillFileParameterInfo
    {
        try {
            val command = if (useOpcProtocol) GET_PICTURE_FILE_INFO_COMMAND_OPC else GET_PICTURE_FILE_INFO_COMMAND
            val cleanCommand = command.removePrefix("/")
            val commandUrl = "${baseUrl}${cleanCommand}?DIR=$directory"

            val response = http.httpGetWithHeader(commandUrl, headerMap, null, timeoutMs)
            if (!response.isNullOrEmpty()) {
                return parseStillImageFileInfo(response)
            }
        } catch (e: Exception) {
            Log.e(TAG, "ERR> GET FILE INFO: ${e.localizedMessage}", e)
        }
        // エラー時やレスポンス空時のフォールバック（デフォルト値のオブジェクトを返す）
        return IStillImageFileInfo.StillFileParameterInfo()
    }

    private fun parseStillImageFileInfo(response: String): IStillImageFileInfo.StillFileParameterInfo
    {
        // 1つだけ存在するタグのコンテンツを既存のヘルパー関数で抽出
        val dateTime = extractTagContent(response, "DateTime") ?: ""
        val locationStatus = extractTagContent(response, "LocationStatus") ?: ""
        val detectVersion = extractTagContent(response, "DetectVersion")?.toIntOrNull() ?: 0
        val detectId = extractTagContent(response, "DetectID")?.toIntOrNull() ?: 0
        val expRev = extractTagContent(response, "EXPREV")?.toDoubleOrNull() ?: 0.0
        val digitalTelecon = extractTagContent(response, "DigitalTelecon") ?: ""
        val groupId = extractTagContent(response, "GroupID")?.toIntOrNull() ?: 0
        val colorTone = extractTagContent(response, "COLORTONE") ?: ""
        val tone = extractTagContent(response, "Tone") ?: ""
        val sharpness = extractTagContent(response, "Sharpness")?.toIntOrNull() ?: 0
        val contrast = extractTagContent(response, "Contrast")?.toIntOrNull() ?: 0
        val saturation = extractTagContent(response, "Saturation")?.toIntOrNull() ?: 0
        val effectType = extractTagContent(response, "EffectType") ?: ""
        val fantasicFocus = extractTagContent(response, "FantasicFocus") ?: ""
        val toyPhoto = extractTagContent(response, "ToyPhoto") ?: ""
        val whiteEdge = extractTagContent(response, "WhiteEdge") ?: ""
        val frameJaggy = extractTagContent(response, "FrameJaggy") ?: ""
        val starlight = extractTagContent(response, "Starlight") ?: ""
        val miniatureVerticl = extractTagContent(response, "MiniatureVerticl") ?: ""

        val miniatureHorizon = extractTagContent(response, "MiniatureHorizon") ?: ""
        val shadingHorizon = extractTagContent(response, "ShadingHorizon") ?: ""
        val shadingVertical = extractTagContent(response, "ShadingVertical") ?: ""
        val monotoneFilter = extractTagContent(response, "MonotoneFilter") ?: ""
        val monotoneColor = extractTagContent(response, "MonotoneColor") ?: ""
        val colorCreatorColor = extractTagContent(response, "ColorCreatorColor")?.toIntOrNull() ?: 0
        val colorCreatorVivid = extractTagContent(response, "ColorCreatorVivid")?.toIntOrNull() ?: 0
        val colorPhase = extractTagContent(response, "ColorPhase")?.toIntOrNull() ?: 0
        val whiteBalance = extractTagContent(response, "WhiteBalance") ?: ""
        val customWbBias = extractTagContent(response, "CustomWBBias")?.toIntOrNull() ?: 0
        val wbAutoLightBulbColorLeaving = extractTagContent(response, "WBAutoLightBulbColorLeaving") ?: ""

        val wbBiasA = extractTagContent(response, "WBBiasA")?.toIntOrNull() ?: 0
        val wbBiasG = extractTagContent(response, "WBBiasG")?.toIntOrNull() ?: 0
        val toneControlHigh = extractTagContent(response, "ToneControlHigh")?.toIntOrNull() ?: 0
        val toneControlMiddle = extractTagContent(response, "ToneControlMiddle")?.toIntOrNull() ?: 0
        val toneControlShadow = extractTagContent(response, "ToneControlShadow")?.toIntOrNull() ?: 0
        val aspectRatio = extractTagContent(response, "AspectRatio") ?: ""
        val photoStory = extractTagContent(response, "PhotoStory") ?: ""
        val photoStoryMode = extractTagContent(response, "PhotoStoryMode") ?: ""
        val photoStoryType = extractTagContent(response, "PhotoStoryType") ?: ""
        val photoStoryDevideNumAspectRatio = extractTagContent(response, "PhotoStoryDevideNumAspectRatio") ?: ""

        val photoStoryLayout = extractTagContent(response, "PhotoStoryLayout") ?: ""
        val photoStoryEffect = extractTagContent(response, "PhotoStoryEffect") ?: ""
        val rollAngleReliability = extractTagContent(response, "RollAngleReliability") ?: ""
        val pitchAngleReliability = extractTagContent(response, "PitchAngleReliability") ?: ""
        val location = extractTagContent(response, "Location") ?: ""
        val roleAngle = extractTagContent(response, "RoleAngle")?.toIntOrNull() ?: 0
        val pitchAngle = extractTagContent(response, "PitchAngle")?.toIntOrNull() ?: 0

        // 複数並ぶ可能性のあるエレメント用の抽出処理
        val lensIdList = extractMultipleTagContent(response, "LensID").mapNotNull { it.toIntOrNull() }
        val accessaryIdList = extractMultipleTagContent(response, "AccessaryID").mapNotNull { it.toIntOrNull() }

        val cameraName = extractTagContent(response, "CameraName") ?: ""

        return IStillImageFileInfo.StillFileParameterInfo(
            dateTime = dateTime,
            locationStatus = locationStatus,
            detectVersion = detectVersion,
            detectId = detectId,
            expRev = expRev,
            digitalTelecon = digitalTelecon,
            groupId = groupId,
            colorTone = colorTone,
            tone = tone,
            sharpness = sharpness,
            contrast = contrast,
            saturation = saturation,
            effectType = effectType,
            fantasicFocus = fantasicFocus,
            toyPhoto = toyPhoto,
            whiteEdge = whiteEdge,
            frameJaggy = frameJaggy,
            starlight = starlight,
            miniatureVerticl = miniatureVerticl,
            miniatureHorizon = miniatureHorizon,
            shadingHorizon = shadingHorizon,
            shadingVertical = shadingVertical,
            monotoneFilter = monotoneFilter,
            monotoneColor = monotoneColor,
            colorCreatorColor = colorCreatorColor,
            colorCreatorVivid = colorCreatorVivid,
            colorPhase = colorPhase,
            whiteBalance = whiteBalance,
            customWbBias = customWbBias,
            wbAutoLightBulbColorLeaving = wbAutoLightBulbColorLeaving,
            wbBiasA = wbBiasA,
            wbBiasG = wbBiasG,
            toneControlHigh = toneControlHigh,
            toneControlMiddle = toneControlMiddle,
            toneControlShadow = toneControlShadow,
            aspectRatio = aspectRatio,
            photoStory = photoStory,
            photoStoryMode = photoStoryMode,
            photoStoryType = photoStoryType,
            photoStoryDevideNumAspectRatio = photoStoryDevideNumAspectRatio,
            photoStoryLayout = photoStoryLayout,
            photoStoryEffect = photoStoryEffect,
            rollAngleReliability = rollAngleReliability,
            pitchAngleReliability = pitchAngleReliability,
            location = location,
            roleAngle = roleAngle,
            pitchAngle = pitchAngle,
            lensId = lensIdList,
            accessaryId = accessaryIdList,
            cameraName = cameraName
        )
    }

    // --- movie fileの情報を解析し、データクラスに格納して応答する
    private fun parseMovieFileInfo(response: String): ICameraFileInfo.MovieFileInfo {
        val playtimeValue = extractTagContent(response, "playtime")
        val movieSizeValue = extractTagContent(response, "moviesize")
        val shortMovieValue = extractTagContent(response, "shortmovie")
        val datetimeValue = extractTagContent(response, "shootingdatetime")

        val playTime = playtimeValue?.toIntOrNull() ?: 0
        val movieSize = movieSizeValue ?: ""
        val isShortMovie = shortMovieValue?.lowercase() == "yes"
        val dateTime = parseDateTime(datetimeValue ?: "")

        return ICameraFileInfo.MovieFileInfo(playTime, movieSize, isShortMovie, dateTime)
    }

    //  ---- 指定されたタグに挟まれた文字列を抽出するヘルパー関数
    private fun extractTagContent(xml: String, tagName: String): String? {
        val regex = "<$tagName>(.*?)</$tagName>".toRegex(RegexOption.IGNORE_CASE)
        val matchResult = regex.find(xml)
        return matchResult?.groups?.get(1)?.value?.trim()
    }

    // ---- 複数存在するタグ（LensID, AccessaryIDなど）から全ての値をリストとして抽出する追加ヘルパー関数
    private fun extractMultipleTagContent(xml: String, tagName: String): List<String> {
        val regex = "<$tagName>(.*?)</$tagName>".toRegex(RegexOption.IGNORE_CASE)
        return regex.findAll(xml).map { it.groups[1]?.value?.trim() ?: "" }.filter { it.isNotEmpty() }.toList()
    }

    // ---- フォーマット "YYYYMMDDThhmm" を Date オブジェクトに変換
    private fun parseDateTime(dateStr: String): Date
    {
        if (dateStr.isEmpty()) return Date(0)
        return try {
            val sdf = SimpleDateFormat("yyyyMMdd'T'HHmm", Locale.US)
            sdf.parse(dateStr) ?: Date(0)
        } catch (_: Exception) {
            Date(0)
        }
    }

    companion object {
        private val TAG = OmdsGetFileInfo::class.java.simpleName
        private const val TIMEOUT_MS = 5500

        private const val GET_MOVIE_FILE_INFO_COMMAND = "get_movfileinfo.cgi"
        private const val GET_MOVIE_FILE_INFO_COMMAND_OPC = "get_movfileinfo.cgi"

        private const val GET_PICTURE_FILE_INFO_COMMAND = "get_imageinfo.cgi"
        private const val GET_PICTURE_FILE_INFO_COMMAND_OPC = "get_imageinfo.cgi"
    }
}
