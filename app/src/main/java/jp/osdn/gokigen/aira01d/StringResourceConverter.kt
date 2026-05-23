package jp.osdn.gokigen.aira01d

import android.content.Context
import jp.osdn.gokigen.aira01d.preference.camera.OpcProperty

class StringResourceConverter
{
    private val convertStringIdTable = mutableMapOf<String, Int>()
    private val convertIconIdTable = mutableMapOf<String, Int>()
    init {
        // ----- カメラプロパティグループ
        convertStringIdTable["cameraProp_BASIC"] = R.string.cameraProp_BASIC
        convertStringIdTable["cameraProp_BASIC_FOCUS"] = R.string.cameraProp_BASIC_FOCUS
        convertStringIdTable["cameraProp_BASIC_IMAGE"] = R.string.cameraProp_BASIC_IMAGE
        convertStringIdTable["cameraProp_DEVICE"] = R.string.cameraProp_DEVICE
        convertStringIdTable["cameraProp_SHOOTING"] = R.string.cameraProp_SHOOTING
        convertStringIdTable["cameraProp_WB"] = R.string.cameraProp_WB
        convertStringIdTable["cameraProp_ART"] = R.string.cameraProp_ART
        convertStringIdTable["cameraProp_COLORTONE"] = R.string.cameraProp_COLORTONE
        convertStringIdTable["cameraProp_CT_CONTRAST"] = R.string.cameraProp_CT_CONTRAST
        convertStringIdTable["cameraProp_CT_SHARP"] = R.string.cameraProp_CT_SHARP
        convertStringIdTable["cameraProp_CT_STRT"] = R.string.cameraProp_CT_STRT
        convertStringIdTable["cameraProp_CT_TONE"] = R.string.cameraProp_CT_TONE
        convertStringIdTable["cameraProp_CT_MONO"] = R.string.cameraProp_CT_MONO
        convertStringIdTable["cameraProp_ART_BRACKET"] = R.string.cameraProp_ART_BRACKET
        convertStringIdTable["cameraProp_ART_EFFECT"] = R.string.cameraProp_ART_EFFECT

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

    // ----- stringのリソースIDを取得する
    private fun getStringResIdByReflection(key: String): Int {
        return try {
            // R.string クラスから指定したキー（フィールド）を取得
            val field = R.string::class.java.getField(key)
            // フィールド（Int値）を返す
            field.getInt(null)
        } catch (_: Exception) {
            // 見つからなかった場合のフォールバック（デフォルト値など）
            0
        }
    }

    // ----- カメラプロパティの定義を読み取って展開する
    fun loadOpcPropertiesFromXml(context: Context): List<OpcProperty> {
        val rawArray = context.resources.getStringArray(R.array.opc_properties_definition)

        return rawArray.mapNotNull { item ->
            val parts = item.split(",")
            if (parts.size >= 2) {
                val category = parts[0].trim()
                val key = parts[1].trim()

                // リフレクションで R.string からIDを取得
                val resId = getStringResIdByReflection(key)

                OpcProperty(key = key, labelId = resId, category = category)
            } else null
        }
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
