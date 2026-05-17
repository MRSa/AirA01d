package jp.osdn.gokigen.aira01d

class StringResourceConverter
{
    private val convertStringIdTable = mutableMapOf<String, Int>()
    private val convertIconIdTable = mutableMapOf<String, Int>()
    init {
        // ----- プロパティ
        convertStringIdTable["WIFI_CH"] = R.string.WIFI_CH
        convertStringIdTable["RAW"] = R.string.RAW
        convertStringIdTable["COLORTONE"] = R.string.COLORTONE
        convertStringIdTable["WB_REV_3000K"] = R.string.WB_REV_3000K
        convertStringIdTable["WB_REV_5300K"] = R.string.WB_REV_5300K
        convertStringIdTable["BRACKET_PICT_PARTCOLOR"] = R.string.BRACKET_PICT_PARTCOLOR
        convertStringIdTable["MONOTONEFILTER_MONOCHROME"] = R.string.MONOTONEFILTER_MONOCHROME
        convertStringIdTable["WB_REV_G_AUTO"] = R.string.WB_REV_G_AUTO
        convertStringIdTable["SHARP_FLAT"] = R.string.SHARP_FLAT
        convertStringIdTable["TONE_FLAT"] = R.string.TONE_FLAT
        convertStringIdTable["APERTURE"] = R.string.APERTURE
        convertStringIdTable["FOCUS_STILL"] = R.string.FOCUS_STILL
        convertStringIdTable["CONTRAST_SOFT"] = R.string.CONTRAST_SOFT
        convertStringIdTable["WB_REV_G_AUTO_UNDER_WATER"] = R.string.WB_REV_G_AUTO_UNDER_WATER
        convertStringIdTable["ART_EFFECT_TYPE_DAYDREAM"] = R.string.ART_EFFECT_TYPE_DAYDREAM
        convertStringIdTable["SATURATION_LEVEL_FLAT"] = R.string.SATURATION_LEVEL_FLAT
        convertStringIdTable["EFFECT_LEVEL_I_FINISH"] = R.string.EFFECT_LEVEL_I_FINISH
        convertStringIdTable["AE"] = R.string.AE
        convertStringIdTable["TONE_NATURAL"] = R.string.TONE_NATURAL
        convertStringIdTable["LENS_PRIORITY_ANTI_SHAKE"] = R.string.LENS_PRIORITY_ANTI_SHAKE
        convertStringIdTable["TOUCH_EFFECTIVE_AREA_UPPER_LEFT"] = R.string.TOUCH_EFFECTIVE_AREA_UPPER_LEFT
        convertStringIdTable["ART_EFFECT_TYPE_MINIATURE"] = R.string.ART_EFFECT_TYPE_MINIATURE
        convertStringIdTable["WB_REV_G_3000K"] = R.string.WB_REV_G_3000K
        convertStringIdTable["TONE_CONTROL_HIGH"] = R.string.TONE_CONTROL_HIGH
        convertStringIdTable["TAKE_DRIVE"] = R.string.TAKE_DRIVE
        convertStringIdTable["BRACKET_PICT_DRAMATIC_TONE"] = R.string.BRACKET_PICT_DRAMATIC_TONE
        convertStringIdTable["BRACKET_PICT_CROSS_PROCESS"] = R.string.BRACKET_PICT_CROSS_PROCESS
        convertStringIdTable["RECENTLY_ART_FILTER"] = R.string.RECENTLY_ART_FILTER
        convertStringIdTable["EXPOSE_MOVIE_SELECT"] = R.string.EXPOSE_MOVIE_SELECT
        convertStringIdTable["ART_EFFECT_TYPE_DRAMATIC_TONE"] = R.string.ART_EFFECT_TYPE_DRAMATIC_TONE
        convertStringIdTable["RECVIEW"] = R.string.RECVIEW
        convertStringIdTable["SHARP_NATURAL"] = R.string.SHARP_NATURAL
        convertStringIdTable["ART_EFFECT_TYPE_VINTAGE"] = R.string.ART_EFFECT_TYPE_VINTAGE
        convertStringIdTable["ART_EFFECT_HYBRID_POPART"] = R.string.ART_EFFECT_HYBRID_POPART
        convertStringIdTable["ART_EFFECT_HYBRID_CROSS_PROCESS"] = R.string.ART_EFFECT_HYBRID_CROSS_PROCESS
        convertStringIdTable["WB_REV_G_7500K"] = R.string.WB_REV_G_7500K
        convertStringIdTable["ART_EFFECT_HYBRID_GENTLE_SEPIA"] = R.string.ART_EFFECT_HYBRID_GENTLE_SEPIA
        convertStringIdTable["SHARP_MONOCHROME"] = R.string.SHARP_MONOCHROME
        convertStringIdTable["ART_EFFECT_HYBRID_PASTEL"] = R.string.ART_EFFECT_HYBRID_PASTEL
        convertStringIdTable["BRACKET_PICT_LIGNE_CLAIR"] = R.string.BRACKET_PICT_LIGNE_CLAIR
        convertStringIdTable["BRACKET_PICT_GENTLE_SEPIA"] = R.string.BRACKET_PICT_GENTLE_SEPIA
        convertStringIdTable["CONTINUOUS_SHOOTING_VELOCITY"] = R.string.CONTINUOUS_SHOOTING_VELOCITY
        convertStringIdTable["TOUCH_AE_EFFECTIVE_AREA_UPPER_LEFT"] = R.string.TOUCH_AE_EFFECTIVE_AREA_UPPER_LEFT
        convertStringIdTable["ART_EFFECT_TYPE_TOY_PHOTO"] = R.string.ART_EFFECT_TYPE_TOY_PHOTO
        convertStringIdTable["FACE_SCAN"] = R.string.FACE_SCAN
        convertStringIdTable["SATURATION_LEVEL_I_FINISH"] = R.string.SATURATION_LEVEL_I_FINISH
        convertStringIdTable["SSID"] = R.string.SSID
        convertStringIdTable["TONE_MONOCHROME"] = R.string.TONE_MONOCHROME
        convertStringIdTable["WB_REV_G_5300K"] = R.string.WB_REV_G_5300K
        convertStringIdTable["ART_EFFECT_HYBRID_VINTAGE"] = R.string.ART_EFFECT_HYBRID_VINTAGE
        convertStringIdTable["BRACKET_PICT_PASTEL"] = R.string.BRACKET_PICT_PASTEL
        convertStringIdTable["ISO"] = R.string.ISO
        convertStringIdTable["CUSTOM_WB_KELVIN_1"] = R.string.CUSTOM_WB_KELVIN_1
        convertStringIdTable["BRACKET_PICT_POPART"] = R.string.BRACKET_PICT_POPART
        convertStringIdTable["TONE_CONTROL_LOW"] = R.string.TONE_CONTROL_LOW
        convertStringIdTable["MONOTONECOLOR_ROUGH_MONOCHROME"] = R.string.MONOTONECOLOR_ROUGH_MONOCHROME
        convertStringIdTable["AF_LOCK_STATE"] = R.string.AF_LOCK_STATE
        convertStringIdTable["IMAGESIZE"] = R.string.IMAGESIZE
        convertStringIdTable["MONOTONECOLOR_DRAMATIC_TONE"] = R.string.MONOTONECOLOR_DRAMATIC_TONE
        convertStringIdTable["AUTO_WB_DENKYU_COLORED_LEAVING"] = R.string.AUTO_WB_DENKYU_COLORED_LEAVING
        convertStringIdTable["QUALITY_MOVIE_SHORT_MOVIE_RECORD_TIME"] = R.string.QUALITY_MOVIE_SHORT_MOVIE_RECORD_TIME
        convertStringIdTable["ART_EFFECT_HYBRID_MINIATURE"] = R.string.ART_EFFECT_HYBRID_MINIATURE
        convertStringIdTable["ART_EFFECT_HYBRID_TOY_PHOTO"] = R.string.ART_EFFECT_HYBRID_TOY_PHOTO
        convertStringIdTable["AE_LOCK_STATE"] = R.string.AE_LOCK_STATE
        convertStringIdTable["WB_REV_4000K"] = R.string.WB_REV_4000K
        convertStringIdTable["TONE_I_FINISH"] = R.string.TONE_I_FINISH
        convertStringIdTable["ART_EFFECT_HYBRID_PARTCOLOR"] = R.string.ART_EFFECT_HYBRID_PARTCOLOR
        convertStringIdTable["ANTI_SHAKE_FOCAL_LENGTH"] = R.string.ANTI_SHAKE_FOCAL_LENGTH
        convertStringIdTable["TONE_VIVID"] = R.string.TONE_VIVID
        convertStringIdTable["WB_REV_AUTO"] = R.string.WB_REV_AUTO
        convertStringIdTable["GPS"] = R.string.GPS
        convertStringIdTable["ART_EFFECT_HYBRID_ROUGH_MONOCHROME"] = R.string.ART_EFFECT_HYBRID_ROUGH_MONOCHROME
        convertStringIdTable["FULL_TIME_AF"] = R.string.FULL_TIME_AF
        convertStringIdTable["WB_REV_AUTO_UNDER_WATER"] = R.string.WB_REV_AUTO_UNDER_WATER
        convertStringIdTable["MONOTONECOLOR_MONOCHROME"] = R.string.MONOTONECOLOR_MONOCHROME
        convertStringIdTable["QUALITY_MOVIE"] = R.string.QUALITY_MOVIE
        convertStringIdTable["BRACKET_PICT_FANTASIC_FOCUS"] = R.string.BRACKET_PICT_FANTASIC_FOCUS
        convertStringIdTable["SHARP_VIVID"] = R.string.SHARP_VIVID
        convertStringIdTable["MONOTONEFILTER_ROUGH_MONOCHROME"] = R.string.MONOTONEFILTER_ROUGH_MONOCHROME
        convertStringIdTable["COLOR_CREATOR_COLOR"] = R.string.COLOR_CREATOR_COLOR
        convertStringIdTable["WB_REV_6000K"] = R.string.WB_REV_6000K
        convertStringIdTable["SHUTTER"] = R.string.SHUTTER
        convertStringIdTable["WB_REV_G_6000K"] = R.string.WB_REV_G_6000K
        convertStringIdTable["ASPECT_RATIO"] = R.string.ASPECT_RATIO
        convertStringIdTable["SATURATION_LEVEL_SOFT"] = R.string.SATURATION_LEVEL_SOFT
        convertStringIdTable["BATTERY_LEVEL"] = R.string.BATTERY_LEVEL
        convertStringIdTable["BRACKET_PICT_TOY_PHOTO"] = R.string.BRACKET_PICT_TOY_PHOTO
        convertStringIdTable["ART_EFFECT_HYBRID_FANTASIC_FOCUS"] = R.string.ART_EFFECT_HYBRID_FANTASIC_FOCUS
        convertStringIdTable["SHARP_SOFT"] = R.string.SHARP_SOFT
        convertStringIdTable["COLOR_CREATOR_VIVID"] = R.string.COLOR_CREATOR_VIVID
        convertStringIdTable["SHARP_I_FINISH"] = R.string.SHARP_I_FINISH
        convertStringIdTable["BRACKET_PICT_MINIATURE"] = R.string.BRACKET_PICT_MINIATURE
        convertStringIdTable["MONOTONEFILTER_DRAMATIC_TONE"] = R.string.MONOTONEFILTER_DRAMATIC_TONE
        convertStringIdTable["BRACKET_PICT_VINTAGE"] = R.string.BRACKET_PICT_VINTAGE
        convertStringIdTable["COMPRESSIBILITY_RATIO"] = R.string.COMPRESSIBILITY_RATIO
        convertStringIdTable["WB_REV_G_4000K"] = R.string.WB_REV_G_4000K
        convertStringIdTable["ART_EFFECT_HYBRID_LIGHT_TONE"] = R.string.ART_EFFECT_HYBRID_LIGHT_TONE
        convertStringIdTable["WB_REV_7500K"] = R.string.WB_REV_7500K
        convertStringIdTable["ANTI_SHAKE_MOVIE"] = R.string.ANTI_SHAKE_MOVIE
        convertStringIdTable["CONTRAST_MONOCHROME"] = R.string.CONTRAST_MONOCHROME
        convertStringIdTable["TOUCH_AE_EFFECTIVE_AREA_LOWER_RIGHT"] = R.string.TOUCH_AE_EFFECTIVE_AREA_LOWER_RIGHT
        convertStringIdTable["CONTRAST_FLAT"] = R.string.CONTRAST_FLAT
        convertStringIdTable["ART_EFFECT_TYPE_POPART"] = R.string.ART_EFFECT_TYPE_POPART
        convertStringIdTable["FOCUS_MOVIE"] = R.string.FOCUS_MOVIE
        convertStringIdTable["SOUND_VOLUME_LEVEL"] = R.string.SOUND_VOLUME_LEVEL
        convertStringIdTable["WB"] = R.string.WB
        convertStringIdTable["ART_EFFECT_TYPE_ROUGH_MONOCHROME"] = R.string.ART_EFFECT_TYPE_ROUGH_MONOCHROME
        convertStringIdTable["TONE_CONTROL_MIDDLE"] = R.string.TONE_CONTROL_MIDDLE
        convertStringIdTable["TOUCH_EFFECTIVE_AREA_LOWER_RIGHT"] = R.string.TOUCH_EFFECTIVE_AREA_LOWER_RIGHT
        convertStringIdTable["CONTRAST_NATURAL"] = R.string.CONTRAST_NATURAL
        convertStringIdTable["ART_EFFECT_HYBRID_DAYDREAM"] = R.string.ART_EFFECT_HYBRID_DAYDREAM
        convertStringIdTable["EXPREV"] = R.string.EXPREV
        convertStringIdTable["ART_EFFECT_TYPE_CROSS_PROCESS"] = R.string.ART_EFFECT_TYPE_CROSS_PROCESS
        convertStringIdTable["DESTINATION_FILE"] = R.string.DESTINATION_FILE
        convertStringIdTable["ART_EFFECT_TYPE_PARTCOLOR"] = R.string.ART_EFFECT_TYPE_PARTCOLOR
        convertStringIdTable["TONE_SOFT"] = R.string.TONE_SOFT
        convertStringIdTable["COLOR_PHASE"] = R.string.COLOR_PHASE
        convertStringIdTable["TAKEMODE"] = R.string.TAKEMODE
        convertStringIdTable["ART_EFFECT_TYPE_LIGNE_CLAIR"] = R.string.ART_EFFECT_TYPE_LIGNE_CLAIR
        convertStringIdTable["ART_EFFECT_HYBRID_LIGNE_CLAIR"] = R.string.ART_EFFECT_HYBRID_LIGNE_CLAIR
        convertStringIdTable["BRACKET_PICT_DAYDREAM"] = R.string.BRACKET_PICT_DAYDREAM
        convertStringIdTable["ART_EFFECT_TYPE_PASTEL"] = R.string.ART_EFFECT_TYPE_PASTEL
        convertStringIdTable["CONTRAST_VIVID"] = R.string.CONTRAST_VIVID
        convertStringIdTable["ART_EFFECT_HYBRID_DRAMATIC_TONE"] = R.string.ART_EFFECT_HYBRID_DRAMATIC_TONE
        convertStringIdTable["SATURATION_LEVEL_VIVID"] = R.string.SATURATION_LEVEL_VIVID
        convertStringIdTable["BRACKET_PICT_LIGHT_TONE"] = R.string.BRACKET_PICT_LIGHT_TONE
        convertStringIdTable["CONTRAST_I_FINISH"] = R.string.CONTRAST_I_FINISH
        convertStringIdTable["SATURATION_LEVEL_NATURAL"] = R.string.SATURATION_LEVEL_NATURAL
        convertStringIdTable["BRACKET_PICT_ROUGH_MONOCHROME"] = R.string.BRACKET_PICT_ROUGH_MONOCHROME

        // ----- アスペクト比
        convertStringIdTable["aspect_04_03"] = R.string.aspect_04_03
        convertStringIdTable["aspect_16_09"] = R.string.aspect_16_09
        convertStringIdTable["aspect_03_02"] = R.string.aspect_03_02
        convertStringIdTable["aspect_06_06"] = R.string.aspect_06_06
        convertStringIdTable["aspect_03_04"] = R.string.aspect_03_04

        // ----- ドライブモード
        convertStringIdTable["DRIVE_NORMAL"] = R.string.DRIVE_NORMAL
        convertStringIdTable["DRIVE_CONTINUE"] = R.string.DRIVE_CONTINUE

        // ----- 測光モード
        convertStringIdTable["AE_ESP"] = R.string.AE_ESP
        convertStringIdTable["AE_CENTER"] = R.string.AE_CENTER
        convertStringIdTable["AE_PINPOINT"] = R.string.AE_PINPOINT

        // ----- ホワイトバランス
        convertStringIdTable["WB_AUTO"] = R.string.WB_AUTO
        convertStringIdTable["MWB_FINE"] = R.string.MWB_FINE
        convertStringIdTable["MWB_SHADE"] = R.string.MWB_SHADE
        convertStringIdTable["MWB_CLOUD"] = R.string.MWB_CLOUD
        convertStringIdTable["MWB_LAMP"] = R.string.MWB_LAMP
        convertStringIdTable["MWB_FLUORESCENCE1"] = R.string.MWB_FLUORESCENCE1
        convertStringIdTable["MWB_WATER_1"] = R.string.MWB_WATER_1
        convertStringIdTable["WB_CUSTOM1"] = R.string.WB_CUSTOM1

        // ----- 仕上がり・ピクチャーモード
        convertStringIdTable["I_FINISH"] = R.string.I_FINISH
        convertStringIdTable["VIVID"] = R.string.VIVID
        convertStringIdTable["NATURAL"] = R.string.NATURAL
        convertStringIdTable["FLAT"] = R.string.FLAT
        convertStringIdTable["Portrait"] = R.string.Portrait
        convertStringIdTable["Monotone"] = R.string.Monotone
        convertStringIdTable["ePortrait"] = R.string.ePortrait
        convertStringIdTable["COLOR_CREATOR"] = R.string.COLOR_CREATOR
        convertStringIdTable["POPART"] = R.string.POPART
        convertStringIdTable["FANTASIC_FOCUS"] = R.string.FANTASIC_FOCUS
        convertStringIdTable["DAYDREAM"] = R.string.DAYDREAM
        convertStringIdTable["LIGHT_TONE"] = R.string.LIGHT_TONE
        convertStringIdTable["ROUGH_MONOCHROME"] = R.string.ROUGH_MONOCHROME
        convertStringIdTable["TOY_PHOTO"] = R.string.TOY_PHOTO
        convertStringIdTable["MINIATURE"] = R.string.MINIATURE
        convertStringIdTable["CROSS_PROCESS"] = R.string.CROSS_PROCESS
        convertStringIdTable["GENTLE_SEPIA"] = R.string.GENTLE_SEPIA
        convertStringIdTable["DRAMATIC_TONE"] = R.string.DRAMATIC_TONE
        convertStringIdTable["LIGNE_CLAIR"] = R.string.LIGNE_CLAIR
        convertStringIdTable["PASTEL"] = R.string.PASTEL
        convertStringIdTable["VINTAGE"] = R.string.VINTAGE
        convertStringIdTable["PARTCOLOR"] = R.string.PARTCOLOR
        convertStringIdTable["ART_BKT"] = R.string.ART_BKT

    }

    fun getStringResourceId(key: String) : Int {
        try { return convertStringIdTable[key]?: 0 } catch (_: Exception) { }
        return 0
    }

    fun getIconResourceId(key: String, defaultId : Int = R.drawable.outline_question_mark_24) : Int {
        try { return convertIconIdTable[key]?: defaultId } catch (_: Exception) { }
        return defaultId
    }
}
