package jp.osdn.gokigen.aira01d.cameraprofile

import android.util.Log
import androidx.exifinterface.media.ExifInterface
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.IStillImageFileInfo

class OpcCameraProfileFormatter
{

    fun formatCameraProfile(fileName: String, exifInfo: ExifInterface?, cameraProfile: IStillImageFileInfo.StillFileParameterInfo): List<CameraProperties>
    {
        // カメラの撮影条件を List<CameraProperties> に加工して応答する
        try
        {
            val properties = ArrayList<CameraProperties>()

            // ----- プロパティを積む順番: アートフィルター種別＞ピクチャーモード＞撮影モード＞その他 の順で積む
            // (1) RECENTLY_ART_FILTER     アートフィルター種別 (???)
            // (2) EXPOSE_MOVIE_SELECT   → ここでは積まない
            // (3) COLORTONE               ピクチャーモード    (COLORTONE)
            // (4) TAKEMODE                撮影モード         (TAG_EXPOSURE_PROGRAM)
            // (5) RAW                   → ここでは積まない
            // (6) DESTINATION_FILE      → ここでは積まない
            // (7) その他



            // 絞り値
            val fNumber = exifInfo?.getAttribute(ExifInterface.TAG_F_NUMBER)

            // ISO感度
            val iso = exifInfo?.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)

            // 焦点距離
            val focalLengthDouble = exifInfo?.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0.0) ?: 0.0

            // モデル
            val model = exifInfo?.getAttribute(ExifInterface.TAG_MODEL)

            // GPS情報があるかどうか
            val latitude = exifInfo?.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
            val longitude = exifInfo?.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
            val hasGpsInfo = !latitude.isNullOrEmpty()

            // プログラムモード
            val programModeIndex = exifInfo?.getAttributeInt(ExifInterface.TAG_EXPOSURE_PROGRAM, 0) ?: 0

            // 測光モード
            val meteringModeRaw = exifInfo?.getAttributeInt(ExifInterface.TAG_METERING_MODE, 0) ?: 0

            // シャッタースピード
            val exposureTimeStr = exifInfo?.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)

            Log.v(TAG, "-----------")
            Log.v(TAG, "[$fileName] ")
            Log.v(TAG, "   model:$model  hasGps:$hasGpsInfo lat:$latitude lng:$longitude")
            Log.v(TAG, "   F:$fNumber  ISO:$iso length:$focalLengthDouble")
            Log.v(TAG, "   PGM:$programModeIndex  meter:$meteringModeRaw exposure:$exposureTimeStr")
            Log.v(TAG, "   $cameraProfile")
            Log.v(TAG, "-----------")

            return properties
        }
        catch (e: Exception)
        {
            Log.v(TAG, "ERROR> ${e.localizedMessage}")
        }
        // ----- エラー時は空っぽで返す
        return emptyList()
    }

    companion object {
        private val TAG = OpcCameraProfileFormatter::class.java.simpleName
    }
}