package jp.osdn.gokigen.aira01d.cameraprofile

import android.util.Log
import androidx.exifinterface.media.ExifInterface
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.IStillImageFileInfo
import java.util.Locale

class OpcCameraProfileFormatter
{
    private fun getTakeMode(index: Int): String
    {
        return when (index)
        {
            1 -> { "M" }
            2 -> { "P" }
            3 -> { "A" }
            4 -> { "S" }
            5 -> { "ART" }
            else -> { "iAuto" }
        }
    }

    private fun getRecentlyArtFilter(colorTone: String): String
    {
        // --- TAKEMODE が ARTモード時に ART フィルターを設定する
        return when (colorTone) {
            "FLAT", "NATURAL", "Monotone", "Portrait",
            "I_FINISH", "VIVID", "ePortrait", "COLOR_CREATOR" -> { "POPART" }
            else -> { colorTone }
        }
    }

    private fun getShutterSpeed(shutterSpeed: String): String
    {
        if (shutterSpeed.isBlank()) return "60"

        val seconds = if (shutterSpeed.contains("/")) {
            val parts = shutterSpeed.split("/")
            if (parts.size == 2) {
                val numerator = parts[0].toDoubleOrNull() ?: 0.0
                val denominator = parts[1].toDoubleOrNull() ?: 1.0
                if (denominator != 0.0) numerator / denominator else 0.0
            } else {
                0.0
            }
        } else {
            shutterSpeed.toDoubleOrNull() ?: 0.0
        }

        val table = listOf(
            // --- 1秒以上 ---
            Pair(4.0, "4\""),
            Pair(3.2, "3.2\""),
            Pair(2.5, "2.5\""),
            Pair(2.0, "2\""),
            Pair(1.6, "1.6\""),
            Pair(1.3, "1.3\""),
            Pair(1.0, "1\""),

            // --- 1秒未満 ---
            Pair(1.0 / 1.3, "1.3"),
            Pair(1.0 / 1.6, "1.6"),
            Pair(1.0 / 2.0, "2"),
            Pair(1.0 / 2.5, "2.5"),
            Pair(1.0 / 3.0, "3"),
            Pair(1.0 / 4.0, "4"),
            Pair(1.0 / 5.0, "5"),
            Pair(1.0 / 6.0, "6"),
            Pair(1.0 / 8.0, "8"),
            Pair(1.0 / 10.0, "10"),
            Pair(1.0 / 13.0, "13"),
            Pair(1.0 / 15.0, "15"),
            Pair(1.0 / 20.0, "20"),
            Pair(1.0 / 25.0, "25"),
            Pair(1.0 / 30.0, "30"),
            Pair(1.0 / 40.0, "40"),
            Pair(1.0 / 50.0, "50"),
            Pair(1.0 / 60.0, "60"),
            Pair(1.0 / 80.0, "80"),
            Pair(1.0 / 100.0, "100"),
            Pair(1.0 / 125.0, "125"),
            Pair(1.0 / 160.0, "160"),
            Pair(1.0 / 200.0, "200"),
            Pair(1.0 / 250.0, "250"),
            Pair(1.0 / 320.0, "320"),
            Pair(1.0 / 400.0, "400"),
            Pair(1.0 / 500.0, "500"),
            Pair(1.0 / 640.0, "640"),
            Pair(1.0 / 800.0, "800"),
            Pair(1.0 / 1000.0, "1000"),
            Pair(1.0 / 1250.0, "1250"),
            Pair(1.0 / 1600.0, "1600"),
            Pair(1.0 / 2000.0, "2000"),
            Pair(1.0 / 2500.0, "2500"),
            Pair(1.0 / 3200.0, "3200"),
            Pair(1.0 / 4000.0, "4000"),
            Pair(1.0 / 5000.0, "5000"),
            Pair(1.0 / 6400.0, "6400"),
            Pair(1.0 / 8000.0, "8000"),
            Pair(1.0 / 10000.0, "10000"),
            Pair(1.0 / 12800.0, "12800"),
            Pair(1.0 / 16000.0, "16000")
        )
        val nearest = table.minByOrNull { kotlin.math.abs(it.first - seconds) } ?: table.first()
        return nearest.second
    }

    private fun getExposureCompensation(expRev: Double): String
    {
        if (expRev <= -5.0)
        {
            return "-5"
        }
        else if (expRev <= -4.7)
        {
            return "-4.7"
        }
        else if (expRev <= -4.3)
        {
            return "-4.3"
        }
        else if (expRev <= -4.0)
        {
            return "-4"
        }
        else if (expRev <= -3.7)
        {
            return "-3.7"
        }
        else if (expRev <= -3.3)
        {
            return "-3.3"
        }
        else if (expRev <= -3.0)
        {
            return "-3"
        }
        else if (expRev <= -2.7)
        {
            return "-2.7"
        }
        else if (expRev <= -2.3)
        {
            return "-2.3"
        }
        else if (expRev <= -2.0)
        {
            return "-2"
        }
        else if (expRev <= -1.7)
        {
            return "-1.7"
        }
        else if (expRev <= -1.3)
        {
            return "-1.3"
        }
        else if (expRev <= -1.0)
        {
            return "-1"
        }
        else if (expRev <= -0.7)
        {
            return "-0.7"
        }
        else if (expRev <= -0.3)
        {
            return "-0.3"
        }
        else if (expRev <= -0.0)
        {
            return "0"
        }
        else if (expRev <= 0.3)
        {
            return "0.3"
        }
        else if (expRev <= 0.7)
        {
            return "0.7"
        }
        else if (expRev <= 1.0)
        {
            return "1"
        }
        else if (expRev <= 1.3)
        {
            return "1.3"
        }
        else if (expRev <= 1.7)
        {
            return "1.7"
        }
        else if (expRev <= 2.0)
        {
            return "2"
        }
        else if (expRev <= 2.3)
        {
            return "2.3"
        }
        else if (expRev <= 2.7)
        {
            return "2.7"
        }
        else if (expRev <= 3.0)
        {
            return "3"
        }
        else if (expRev <= 3.3)
        {
            return "3.3"
        }
        else if (expRev <= 3.7)
        {
            return "3.7"
        }
        else if (expRev <= 4.0)
        {
            return "4"
        }
        else if (expRev <= 4.3)
        {
            return "4.3"
        }
        else if (expRev <= 4.7)
        {
            return "4.7"
        }
        return "5"
    }

    private fun getWhiteBalance(wb: String): String
    {
        return when (wb) {
            "FINE" -> { "MWB_FINE" }
            "SHADE" -> { "MWB_SHADE" }
            "CLOUD" -> { "MWB_CLOUD" }
            "LAMP" -> { "MWB_LAMP" }
            "FLUORESCENCE1" -> { "MWB_FLUORESCENCE1" }
            "WATER_1" -> { "MWB_WATER_1" }
            "CUSTOM1" -> { "WB_CUSTOM1" }
            else -> { "WB_AUTO" }
        }
    }

    private fun getEffectHybridPropertyName(colorTone: String): String
    {
        // アートフィルターバリエーション のプロパティ名を特定する
        return when (colorTone) {
            "POPART" -> { "ART_EFFECT_HYBRID_POPART" }
            "DAYDREAM" -> { "ART_EFFECT_HYBRID_DAYDREAM" }
            "ROUGH_MONOCHROME" -> { "ART_EFFECT_HYBRID_ROUGH_MONOCHROME" }
            "TOY_PHOTO" -> { "ART_EFFECT_HYBRID_TOY_PHOTO" }
            "MINIATURE" -> { "ART_EFFECT_HYBRID_MINIATURE" }
            "CROSS_PROCESS" -> { "ART_EFFECT_HYBRID_CROSS_PROCESS" }
            "DRAMATIC_TONE" -> { "ART_EFFECT_HYBRID_DRAMATIC_TONE" }
            "LIGNE_CLAIR" -> { "ART_EFFECT_HYBRID_LIGNE_CLAIR" }
            "PASTEL" -> { "ART_EFFECT_HYBRID_PASTEL" }
            "VINTAGE" -> { "ART_EFFECT_HYBRID_VINTAGE" }
            "PARTCOLOR" -> { "ART_EFFECT_HYBRID_PARTCOLOR" }
            "FANTASIC_FOCUS" -> { "ART_EFFECT_HYBRID_FANTASIC_FOCUS" }
            "LIGHT_TONE" -> { "ART_EFFECT_HYBRID_LIGHT_TONE" }
            "GENTLE_SEPIA" -> { "ART_EFFECT_HYBRID_GENTLE_SEPIA" }
            else -> { "" }
        }
    }

    private fun getEffectTypePropertyName(colorTone: String): String
    {
        // アートフィルターバリエーション のプロパティ名を特定する
        return when (colorTone) {
            "POPART" -> { "ART_EFFECT_TYPE_POPART" }
            "DAYDREAM" -> { "ART_EFFECT_TYPE_DAYDREAM" }
            "ROUGH_MONOCHROME" -> { "ART_EFFECT_TYPE_ROUGH_MONOCHROME" }
            "TOY_PHOTO" -> { "ART_EFFECT_TYPE_TOY_PHOTO" }
            "MINIATURE" -> { "ART_EFFECT_TYPE_MINIATURE" }
            "CROSS_PROCESS" -> { "ART_EFFECT_TYPE_CROSS_PROCESS" }
            "DRAMATIC_TONE" -> { "ART_EFFECT_TYPE_DRAMATIC_TONE" }
            "LIGNE_CLAIR" -> { "ART_EFFECT_TYPE_LIGNE_CLAIR" }
            "PASTEL" -> { "ART_EFFECT_TYPE_PASTEL" }
            "VINTAGE" -> { "ART_EFFECT_TYPE_VINTAGE" }
            "PARTCOLOR" -> { "ART_EFFECT_TYPE_PARTCOLOR" }
            else -> { "" }
        }
    }

    private fun getSaturationPropertyName(colorTone: String): String
    {
        // 彩度のプロパティ名を特定する
        return when (colorTone) {
            "I_FINISH" -> { "SATURATION_LEVEL_I_FINISH" }
            "VIVID" -> { "SATURATION_LEVEL_VIVID" }
            "NATURAL" -> { "SATURATION_LEVEL_NATURAL" }
            "FLAT" -> { "SATURATION_LEVEL_FLAT" }
            "Portrait" -> { "SATURATION_LEVEL_SOFT" }
            else -> { "" }
        }
    }

    private fun getContrastPropertyName(colorTone: String): String
    {
        // コントラストのプロパティ名を特定する
        return when (colorTone) {
            "I_FINISH" -> { "CONTRAST_I_FINISH" }
            "VIVID" -> { "CONTRAST_VIVID" }
            "NATURAL" -> { "CONTRAST_NATURAL" }
            "FLAT" -> { "CONTRAST_FLAT" }
            "Portrait" -> { "CONTRAST_SOFT" }
            "Monotone" -> { "CONTRAST_MONOCHROME" }
            else -> { "" }
        }
    }

    private fun getSharpnessPropertyName(colorTone: String): String
    {
        // シャープネスのプロパティ名を特定する
        return when (colorTone) {
            "I_FINISH" -> { "SHARP_I_FINISH" }
            "VIVID" -> { "SHARP_VIVID" }
            "NATURAL" -> { "SHARP_NATURAL" }
            "FLAT" -> { "SHARP_FLAT" }
            "Portrait" -> { "SHARP_SOFT" }
            "Monotone" -> { "SHARP_MONOCHROME" }
            else -> { "" }
        }
    }

    private fun getTonePropertyName(colorTone: String): String
    {
        // 階調のプロパティ名を特定する
        return when (colorTone) {
            "I_FINISH" -> { "TONE_I_FINISH" }
            "VIVID" -> { "TONE_VIVID" }
            "NATURAL" -> { "TONE_NATURAL" }
            "FLAT" -> { "TONE_FLAT" }
            "Portrait" -> { "TONE_SOFT" }
            "Monotone" -> { "TONE_MONOCHROME" }
            else -> { "" }
        }
    }

    fun formatCameraProfile(fileName: String, exifInfo: ExifInterface?, cameraProfile: IStillImageFileInfo.StillFileParameterInfo): List<CameraProperties>
    {
        // カメラの撮影条件を List<CameraProperties> に加工して応答する
        try
        {
            val properties = ArrayList<CameraProperties>()

            // 絞り値
            val fNumber = exifInfo?.getAttribute(ExifInterface.TAG_F_NUMBER)

            // GPS情報があるかどうか
            val latLong = exifInfo?.getLatLong()
            val hasGpsInfo = (latLong != null)

            // プログラムモード
            val takeMode = getTakeMode(exifInfo?.getAttributeInt(ExifInterface.TAG_EXPOSURE_PROGRAM, 0) ?: 0)

            // シャッタースピード
            val exposureTimeStr = exifInfo?.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)

            // 測光補正値
            val expComp = exifInfo?.getAttributeDouble(ExifInterface.TAG_EXPOSURE_BIAS_VALUE, 0.0) ?: 0.0

            // 撮影モード
            properties.add(CameraProperties("TAKEMODE", takeMode))
            when (takeMode)
            {
                "ART" -> {
                    // ----- アートフィルター種別モード
                    if (cameraProfile.colorTone != "ERROR") {
                        properties.add(
                            CameraProperties(
                                "RECENTLY_ART_FILTER",
                                getRecentlyArtFilter(cameraProfile.colorTone)
                            )
                        )
                    }

                    // ----- 露出補正値
                    properties.add(CameraProperties("EXPREV", getExposureCompensation(expComp)))
                }
                "M" -> {
                    // ----- ピクチャーモード
                    if (cameraProfile.colorTone != "ERROR") {
                        properties.add(CameraProperties("COLORTONE", cameraProfile.colorTone))
                    }

                    // ----- シャッター速度
                    if (exposureTimeStr != null)
                    {
                        properties.add(CameraProperties("SHUTTER", getShutterSpeed(exposureTimeStr)))
                    }

                    // ----- 絞り値
                    if (fNumber != null)
                    {
                        properties.add(CameraProperties("APERTURE", fNumber))
                    }
                }
                "S" -> {
                    // ----- ピクチャーモード
                    if (cameraProfile.colorTone != "ERROR") {
                        properties.add(CameraProperties("COLORTONE", cameraProfile.colorTone))
                    }

                    // ----- シャッター速度
                    if (exposureTimeStr != null)
                    {
                        properties.add(CameraProperties("SHUTTER", getShutterSpeed(exposureTimeStr)))
                    }

                    // ----- 露出補正値
                    properties.add(CameraProperties("EXPREV", getExposureCompensation(expComp)))
                }
                "A" -> {
                    // ----- ピクチャーモード
                    if (cameraProfile.colorTone != "ERROR") {
                        properties.add(CameraProperties("COLORTONE", cameraProfile.colorTone))
                    }

                    // ----- 絞り値
                    if (fNumber != null)
                    {
                        properties.add(CameraProperties("APERTURE", fNumber))
                    }

                    // ----- 露出補正値
                    properties.add(CameraProperties("EXPREV", getExposureCompensation(expComp)))
                }
                else -> {
                    // ----- ピクチャーモード
                    if (cameraProfile.colorTone != "ERROR") {
                        properties.add(CameraProperties("COLORTONE", cameraProfile.colorTone))
                    }

                    // ----- 露出補正値
                    properties.add(CameraProperties("EXPREV", getExposureCompensation(expComp)))
                }
            }

            // ----- ホワイトバランス
            properties.add(CameraProperties("WB", getWhiteBalance(cameraProfile.whiteBalance)))

            // ----- WBオート電球色残し
            properties.add(CameraProperties("AUTO_WB_DENKYU_COLORED_LEAVING", cameraProfile.wbAutoLightBulbColorLeaving))

            if (takeMode != "ART")
            {
                // ----- カラークリエーター用 色相
                properties.add(CameraProperties("COLOR_CREATOR_COLOR", cameraProfile.colorCreatorColor.toString()))
                // ----- カラークリエーター用 彩度
                properties.add(CameraProperties("COLOR_CREATOR_VIVID", cameraProfile.colorCreatorVivid.toString()))
                if (cameraProfile.whiteBalance == "CUSTOM1") {
                    // ----- カスタムホワイトバランス色温度
                    properties.add(CameraProperties("CUSTOM_WB_KELVIN_1", cameraProfile.customWbBias.toString()))
                }

                // ----- WB補正(A)
                properties.add(CameraProperties("WB_REV_AUTO", cameraProfile.wbBiasA.toString()))
                properties.add(CameraProperties("WB_REV_5300K", cameraProfile.wbBiasA.toString()))
                properties.add(CameraProperties("WB_REV_7500K", cameraProfile.wbBiasA.toString()))
                properties.add(CameraProperties("WB_REV_6000K", cameraProfile.wbBiasA.toString()))
                properties.add(CameraProperties("WB_REV_3000K", cameraProfile.wbBiasA.toString()))
                properties.add(CameraProperties("WB_REV_4000K", cameraProfile.wbBiasA.toString()))
                properties.add(CameraProperties("WB_REV_AUTO_UNDER_WATER", cameraProfile.wbBiasA.toString()))

                // ----- WB補正(G)
                properties.add(CameraProperties("WB_REV_G_AUTO", cameraProfile.wbBiasG.toString()))
                properties.add(CameraProperties("WB_REV_G_5300K", cameraProfile.wbBiasG.toString()))
                properties.add(CameraProperties("WB_REV_G_7500K", cameraProfile.wbBiasG.toString()))
                properties.add(CameraProperties("WB_REV_G_6000K", cameraProfile.wbBiasG.toString()))
                properties.add(CameraProperties("WB_REV_G_3000K", cameraProfile.wbBiasG.toString()))
                properties.add(CameraProperties("WB_REV_G_4000K", cameraProfile.wbBiasG.toString()))
                properties.add(CameraProperties("WB_REV_G_AUTO_UNDER_WATER", cameraProfile.wbBiasG.toString()))
            }

            if (cameraProfile.monotoneFilter != "ERROR")
            {
                // ----- モノクロフィルター効果
                val propertyName = when (cameraProfile.colorTone)
                {
                    "ROUGH_MONOCHROME" -> { "MONOTONEFILTER_ROUGH_MONOCHROME" }
                    "DRAMATIC_TONE" -> { "MONOTONEFILTER_DRAMATIC_TONE" }
                    "Monotone" -> { "MONOTONEFILTER_MONOCHROME" }
                    else -> { "MONOTONEFILTER_MONOCHROME" }
                }
                properties.add(CameraProperties(propertyName, cameraProfile.monotoneFilter))
            }

            if (cameraProfile.monotoneColor != "ERROR")
            {
                // ----- 調色効果
                val propertyName = when (cameraProfile.colorTone)
                {
                    "ROUGH_MONOCHROME" -> { "MONOTONECOLOR_ROUGH_MONOCHROME" }
                    "DRAMATIC_TONE" -> { "MONOTONECOLOR_DRAMATIC_TONE" }
                    "Monotone" -> { "MONOTONECOLOR_MONOCHROME" }
                    else -> { "MONOTONECOLOR_MONOCHROME" }
                }
                properties.add(CameraProperties(propertyName, cameraProfile.monotoneColor))
            }

            // ----- 諧調
            val tonePropertyName = getTonePropertyName(cameraProfile.colorTone)
            if (tonePropertyName.isNotEmpty())
            {
                properties.add(CameraProperties(tonePropertyName, cameraProfile.tone.uppercase(Locale.US)))
            }

            // ----- シャープネス
            val sharpnessPropertyName = getSharpnessPropertyName(cameraProfile.colorTone)
            if (sharpnessPropertyName.isNotEmpty())
            {
                properties.add(CameraProperties(sharpnessPropertyName, cameraProfile.sharpness.toString()))
            }

            // ----- コントラスト
            val contrastPropertyName = getContrastPropertyName(cameraProfile.colorTone)
            if (contrastPropertyName.isNotEmpty())
            {
                properties.add(CameraProperties(contrastPropertyName, cameraProfile.contrast.toString()))
            }

            // ----- 彩度
            val saturationPropertyName = getSaturationPropertyName(cameraProfile.colorTone)
            if (saturationPropertyName.isNotEmpty())
            {
                properties.add(CameraProperties(saturationPropertyName, cameraProfile.saturation.toString()))
            }

            // ----- アートフィルターバリエーション
            val effectTypePropertyName = getEffectTypePropertyName(cameraProfile.colorTone)
            if (effectTypePropertyName.isNotEmpty())
            {
                properties.add(CameraProperties(effectTypePropertyName, cameraProfile.effectType))
            }

            // ----- パートカラー色相
            properties.add(CameraProperties("COLOR_PHASE", cameraProfile.colorPhase.toString()))

            // ----- トーンコントロール (High/Middle/Shadow)
            properties.add(CameraProperties("TONE_CONTROL_HIGH", cameraProfile.toneControlHigh.toString()))
            properties.add(CameraProperties("TONE_CONTROL_MIDDLE", cameraProfile.toneControlMiddle.toString()))
            properties.add(CameraProperties("TONE_CONTROL_LOW", cameraProfile.toneControlShadow.toString()))

            // ----- アートエフェクト効果
            val effectHybridPropertyName = getEffectHybridPropertyName(cameraProfile.colorTone)

            // ----- ソフトフォーカス効果
            if (cameraProfile.fantasicFocus == "ON")
            {
                properties.add(CameraProperties(effectHybridPropertyName, "FANTASIC_FOCUS"))
            }
            // ----- ピンホール効果
            if (cameraProfile.toyPhoto == "ON")
            {
                properties.add(CameraProperties(effectHybridPropertyName, "TOY_PHOTO"))
            }
            // ----- ホワイトエッジ効果
            if (cameraProfile.whiteEdge == "ON")
            {
                properties.add(CameraProperties(effectHybridPropertyName, "WHITE_EDGE"))
            }
            // ----- フレーム効果
            if (cameraProfile.frameJaggy == "ON")
            {
                properties.add(CameraProperties(effectHybridPropertyName, "FRAME_JAGGY"))
            }
            // ----- スターライト効果
            if (cameraProfile.starlight == "ON")
            {
                properties.add(CameraProperties(effectHybridPropertyName, "STARLIGHT"))
            }
            // ----- 上下ぼかし効果
            if (cameraProfile.miniatureVertical == "ON")
            {
                properties.add(CameraProperties(effectHybridPropertyName, "MINIATURE_VERTICAL"))
            }
            // ----- 左右ぼかし効果
            if (cameraProfile.miniatureHorizon == "ON")
            {
                properties.add(CameraProperties(effectHybridPropertyName, "MINIATURE_HORIZON"))
            }
            // ----- 左右シェード
            if (cameraProfile.shadingVertical == "ON")
            {
                properties.add(CameraProperties(effectHybridPropertyName, "SHADING_VERTICAL"))
            }
            // ----- 上下シェード
            if (cameraProfile.shadingHorizon == "ON")
            {
                properties.add(CameraProperties(effectHybridPropertyName, "SHADING_HORIZON"))
            }

            // ----- アスペクト比
            properties.add(CameraProperties("ASPECT_RATIO", cameraProfile.aspectRatio))

            // ----- ログ出力
            Log.v(TAG, "----------------------")
            Log.v(TAG, "[$fileName] ")
            Log.v(TAG, "   hasGps:$hasGpsInfo lat:${latLong?.get(0)?:0.0f} lng:${latLong?.get(1)?:0.0f}")
            Log.v(TAG, "   Mode:$takeMode  F:$fNumber Shutter: ${getShutterSpeed(exposureTimeStr ?: "")}($exposureTimeStr)")
            Log.v(TAG, "   $cameraProfile")
            Log.v(TAG, "----------------------")

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
