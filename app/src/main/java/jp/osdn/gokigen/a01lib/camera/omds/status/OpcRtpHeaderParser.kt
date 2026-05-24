package jp.osdn.gokigen.a01lib.camera.omds.status

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatusUpdateNotify
import java.util.Locale

class OpcRtpHeaderParser(private val statusProvider: ICameraStatusUpdateNotify)
{
    private var buffer: ByteArray? = null
    private var statusReceived = false

    fun receiveRtpHeader(byteBuffer: ByteArray)
    {
        try
        {
            buffer = byteBuffer
            statusReceived = true
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            statusReceived = false
        }
    }

    fun parseRtpHeader()
    {
        if (!statusReceived)
        {
            // ----- RTP拡張ヘッダを受信していないので、戻る
            return
        }

        // ----- RTP拡張ヘッダ個別情報を解析する
        try
        {
            if (buffer == null)
            {
                Log.v(TAG, " parseRtpHeader() : null")
                return
            }
            var position = 16
            val maxLength = buffer?.size ?: 0
            if (maxLength <= 0)
            {
                // データがないので何もしない
                Log.v(TAG, " parseRtpHeader() : empty(length 0)")
                return
            }

            if (DUMP_LOG)
            {
                // 受信データのバッファをダンプする
                Log.v(TAG," parseRtpHeader size: $maxLength")
            }

            while (position + 4 < maxLength)
            {
                val commandId: Int = ((buffer?.get(position) ?: 0).toInt() and 0xff) * 256 + ((buffer?.get(position + 1) ?: 0).toInt() and 0xff)
                val length: Int = ((buffer?.get(position + 2) ?: 0).toInt() and 0xff) * 256 + ((buffer?.get(position + 3) ?: 0).toInt() and 0xff)
                //Log.v(TAG, "parseRtpHeader() : commandId: $commandId  length: $length")
                when (commandId)
                {
                    ID_AF_FRAME_INFO -> {  }  // { checkFocused(buffer, position, length) }
                    ID_FRAME_SIZE -> { }
                    ID_MEDIA_INFO -> { checkMediaInfo(buffer, position, length) }  // メディア情報 (カード撮影可否など)
                    ID_ROTATION_INFO -> { checkRotationInfo(buffer, position, length) }  // 回転情報
                    ID_AVAILABLE_SHOTS -> { checkAvailableShots(buffer, position, length) }  // 撮影可能枚数
                    ID_OMDS_UNKNOWN_01 -> { }
                    ID_OMDS_UNKNOWN_02 -> { }
                    ID_SHUTTER_SPEED -> { checkShutterSpeed(buffer, position, length)  }  // シャッタースピード
                    ID_APERTURE -> { checkAperture(buffer, position, length) }            // 絞り値
                    ID_EXPOSURE_COMPENSATION -> { checkExposureCompensation(buffer, position, length) }  // 露出補正値
                    ID_OMDS_UNKNOWN_03 -> { }
                    ID_ISO_SENSITIVITY -> { checkIsoSensitivity(buffer, position, length) }    // ISO感度
                    ID_OMDS_UNKNOWN_04 -> { }
                    ID_OMDS_UNKNOWN_05 -> { }
                    ID_OMDS_UNKNOWN_06 -> { }
                    ID_EXPOSURE_WARNING -> { checkExposureWarning(buffer, position, length) }  // 露出警告
                    ID_FOCUS_TYPE -> { checkFocusType(buffer, position, length) }              // フォーカスモード (AF/MFなど)
                    ID_ZOOM_LENS_INFO -> { checkZoomInfo(buffer, position, length) }           // ズーム情報
                    ID_REMAIN_VIDEO_TIME -> { }
                    ID_POSITION_LEVEL_INFO -> { checkLevelvial(buffer, position, length) }     // 水準器
                    ID_FACE_DETECT_1 -> { }
                    ID_FACE_DETECT_2 -> { }
                    ID_FACE_DETECT_3 -> { }
                    ID_FACE_DETECT_4 -> { }
                    ID_FACE_DETECT_5 -> { }
                    ID_FACE_DETECT_6 -> { }
                    ID_FACE_DETECT_7 -> { }
                    ID_FACE_DETECT_8 -> { }
                    ID_CONTINUOUS_SHOT_PICTURE_INFO -> { }
                    else -> { Log.v(TAG, "RTP HEADER INFO UNKNOWN(cmd: $commandId (0x${commandId.toString(16)}) len: $length)") }
                }
                position += 4 + length * 4  // header : 4bytes , data : length * 4 bytes
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun checkShutterSpeed(buffer: ByteArray?, position: Int, length: Int)
    {
        if ((length != 3)||(buffer == null))
        {
            // データがそろっていないので何もしない
            return
        }

        val numerator = ((((buffer[position + 12].toUInt()).toInt() and 0xff) * 256)) + ((buffer[position + 13].toUInt()).toInt() and 0x00ff)
        val denominator = ((((buffer[position + 14].toUInt()).toInt() and 0xff) * 256)) + ((buffer[position + 15].toUInt()).toInt() and 0x00ff)
        if ((numerator == 0)||(denominator == 0))
        {
            // 値が変なので、なにもしない
            return
        }
        val shutterSpeed = if (numerator > denominator) {
            // 分子が大きい
            if (denominator == 1)  { String.format(Locale.US, "%d\"", numerator) } else { String.format(Locale.US,"%.1f\"", (numerator.toFloat() / denominator.toFloat())) }
        } else {
            // 分母が大きい
            if (numerator == 1) { String.format(Locale.US,"%d/%d", numerator, denominator) } else {String.format(Locale.US,"1/%.1f", (denominator.toFloat() / numerator.toFloat())) }
        }
        statusProvider.updatedShutterSpeed(shutterSpeed)
    }

    private fun checkAperture(buffer: ByteArray?, position: Int, length: Int)
    {
        if ((length != 3)||(buffer == null))
        {
            // データがそろっていないので何もしない
            return
        }
        val focalValue = ((((buffer[position + 12].toUInt()).toInt() and 0xff) * 16777216)) + (((buffer[position + 13].toUInt()).toInt() and 0xff) * 65536) + (((buffer[position + 14].toUInt()).toInt() and 0xff) * 256) + ((buffer[position + 15].toUInt()).toInt() and 0x00ff)
        val aperture = String.format(Locale.US,"F%.1f", (focalValue.toFloat() / 10.0f))
        statusProvider.updatedAperture(aperture)
    }

    private fun checkExposureCompensation(buffer: ByteArray?, position: Int, length: Int)
    {
        if ((length != 3)||(buffer == null))
        {
            // データがそろっていないので何もしない
            return
        }

        val expRevValue = ((((buffer[position + 12].toUInt()).toInt() and 0xff) * 16777216)) + (((buffer[position + 13].toUInt()).toInt() and 0xff) * 65536) + (((buffer[position + 14].toUInt()).toInt() and 0xff) * 256) + ((buffer[position + 15].toUInt()).toInt() and 0x00ff)
        val expRev = String.format(Locale.US,"%+.1f", (expRevValue.toFloat() / 10.0f))
        statusProvider.updatedExposureCompensation(expRev)
    }

    private fun checkIsoSensitivity(buffer: ByteArray?, position: Int, length: Int)
    {
        if ((length != 3) || (buffer == null))
        {
            // データがそろっていないので何もしない
            return
        }
        val autoFlag = ((((buffer[position + 8].toUInt()).toInt() and 0xff) * 256)) + ((buffer[position + 9].toUInt()).toInt() and 0x00ff)
        val isoSensValue = ((((buffer[position + 4].toUInt()).toInt() and 0xff) * 16777216)) + (((buffer[position + 5].toUInt()).toInt() and 0xff) * 65536) + (((buffer[position + 6].toUInt()).toInt() and 0xff) * 256) + ((buffer[position + 7].toUInt()).toInt() and 0x00ff)
        val isoSensitivity = if (autoFlag != 0)
        {
            if (isoSensValue == 0xfffe)
            {
                "ISO-A(LOW)"
            }
            else
            {
                "ISO-A($isoSensValue)"
            }
        }
        else
        {
            if (isoSensValue == 0xfffe)
            {
                "ISO LOW"
            }
            else
            {
                "ISO ($isoSensValue)"
            }
        }
        statusProvider.updateIsoSensitivity(isoSensitivity)
    }

    private fun checkExposureWarning(buffer: ByteArray?, position: Int, length: Int)
    {
        if ((length != 1)||(buffer == null))
        {
            // データがそろっていないので何もしない
            return
        }
        val exposureWarningValue = getUInt32(buffer, position + 4)
         statusProvider.updateExposureWarning(exposureWarningValue)
    }

    private fun checkFocusType(buffer: ByteArray?, position: Int, length: Int)
    {
        if ((length != 1)||(buffer == null))
        {
            // データがそろっていないので何もしない
            return
        }
        val focusType = ((((buffer[position + 4].toUInt()).toInt() and 0xff) * 256)) + ((buffer[position + 5].toUInt()).toInt() and 0x00ff)
        val focusMode = when (focusType)
        {
            0 -> "S-AF"
            1 -> "C-AF"
            2 -> "MF"
            else -> ""
        }
        statusProvider.updateFocusMode(focusMode)
    }

    private fun checkMediaInfo(buffer: ByteArray?, position: Int, length: Int)
    {
        if ((length != 1)||(buffer == null))
        {
            // データがそろっていないので何もしない
            return
        }
        val mediaStatusValue = buffer[position + 7].toUByte().toInt()
        statusProvider.updatedMediaStatus(mediaStatusValue)
    }

    private fun checkRotationInfo(buffer: ByteArray?, position: Int, length: Int)
    {
        if ((length != 1)||(buffer == null))
        {
            // データがそろっていないので何もしない
            return
        }
        val orientationValue = getUInt32(buffer, position + 4)
        statusProvider.updatedOrientation(orientationValue)
    }

    private fun checkAvailableShots(buffer: ByteArray?, position: Int, length: Int)
    {
        if ((length != 1)||(buffer == null))
        {
            // データがそろっていないので何もしない
            return
        }
        val remainValue = getUInt32(buffer, position + 4)
        statusProvider.updatedAvailableShots(remainValue)
    }

    private fun checkZoomInfo(buffer: ByteArray?, position: Int, length: Int)
    {
        if ((length != 3)||(buffer == null))
        {
            // データがそろっていないので何もしない
            return
        }
        val wide    = getUInt16(buffer, position + 6)
        val current = getUInt16(buffer, position + 8)
        val tele    = getUInt16(buffer, position + 10)
        statusProvider.updatedZoomInfo(wide, current, tele)
    }

    private fun checkLevelvial(buffer: ByteArray?, position: Int, length: Int)
    {
        if ((length != 3)||(buffer == null))
        {
            // データがそろっていないので何もしない
            return
        }
        val accuracy    = getUInt16(buffer, position + 4)
        val orientation = getUInt16(buffer, position + 6)
        val roll = getUInt16(buffer, position + 8)
        val pitch = getUInt16(buffer, position + 12)
        statusProvider.updatedLevelGauge(accuracy, orientation, roll, pitch)
    }

    private fun getUInt32(buffer: ByteArray, pos: Int): Int {
        return (buffer[pos].toUByte().toInt()     shl 24) or
                (buffer[pos + 1].toUByte().toInt() shl 16) or
                (buffer[pos + 2].toUByte().toInt() shl 8) or
                (buffer[pos + 3].toUByte().toInt())
    }

    private fun getUInt16(buffer: ByteArray, pos: Int): Int {
        return (buffer[pos].toUByte().toInt() shl 8) or
                (buffer[pos + 1].toUByte().toInt())
    }

    companion object {
        private val TAG = OpcRtpHeaderParser::class.java.simpleName

        // RTP HEADER IDs
        private const val ID_FRAME_SIZE = 0x01
        private const val ID_AF_FRAME_INFO = 0x02
        private const val ID_MEDIA_INFO = 0x03
        private const val ID_ROTATION_INFO = 0x04
        private const val ID_AVAILABLE_SHOTS = 0x05
        private const val ID_OMDS_UNKNOWN_01 = 0x06
        private const val ID_OMDS_UNKNOWN_02 = 0x07
        private const val ID_SHUTTER_SPEED = 0x08
        private const val ID_APERTURE = 0x09
        private const val ID_EXPOSURE_COMPENSATION = 0x0a
        private const val ID_OMDS_UNKNOWN_03 = 0x0b
        private const val ID_ISO_SENSITIVITY = 0x0c
        private const val ID_OMDS_UNKNOWN_04 = 0x0d
        private const val ID_OMDS_UNKNOWN_05 = 0x0e
        private const val ID_OMDS_UNKNOWN_06 = 0x0f
        private const val ID_EXPOSURE_WARNING = 0x10
        private const val ID_FOCUS_TYPE = 0x11
        private const val ID_ZOOM_LENS_INFO = 0x12
        private const val ID_OMDS_UNKNOWN_07 = 0x13
        private const val ID_OMDS_UNKNOWN_08 = 0x14
        private const val ID_OMDS_UNKNOWN_09 = 0x16
        private const val ID_OMDS_UNKNOWN_10 = 0x1a
        private const val ID_REMAIN_VIDEO_TIME = 0x6a
        private const val ID_POSITION_LEVEL_INFO = 0x6b
        private const val ID_FACE_DETECT_1 = 0x6c
        private const val ID_FACE_DETECT_2 = 0x6d
        private const val ID_FACE_DETECT_3 = 0x6e
        private const val ID_FACE_DETECT_4 = 0x6f
        private const val ID_FACE_DETECT_5 = 0x70
        private const val ID_FACE_DETECT_6 = 0x71
        private const val ID_FACE_DETECT_7 = 0x72
        private const val ID_FACE_DETECT_8 = 0x73
        private const val ID_CONTINUOUS_SHOT_PICTURE_INFO = 0xC8

        private const val DUMP_LOG = false
    }
}
