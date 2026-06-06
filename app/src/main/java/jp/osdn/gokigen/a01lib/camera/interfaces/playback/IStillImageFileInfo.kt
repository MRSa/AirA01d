package jp.osdn.gokigen.a01lib.camera.interfaces.playback

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface IStillImageFileInfo {
    /**
     * Olympus オープンプラットフォームカメラ 通信仕様書(1.0) 準拠 (ページ数)
     * 静止画情報パラメータリスト (Still File Info Parameter List)
     */
    @Serializable
    data class StillFileParameterInfo(
        // --- p.74 掲載パラメータ ---
        @SerialName("DateTime") val dateTime: String = "", // 撮影年月日 (例: "20140618T0302")
        @SerialName("LocationStatus") val locationStatus: String = "NG", // 位置情報有無 (OK/NG)
        @SerialName("DetectVersion") val detectVersion: Int = 0, // 検出Version (10進数)
        @SerialName("DetectID") val detectId: Int = 0, // 検出ID (10進数)
        @SerialName("EXPREV") val expRev: Double = 0.0, // 露出補正値 (-5.0～+5.0)
        @SerialName("DigitalTelecon") val digitalTelecon: String = "OFF", // デジタルテレコン有無 (ON/OFF)
        @SerialName("GroupID") val groupId: Int = 0, // グループID (10進数)
        @SerialName("COLORTONE") val colorTone: String = "", // ピチャーモード (FLATなど)
        @SerialName("Tone") val tone: String = "", // 階調 (Autoなど)
        @SerialName("Sharpness") val sharpness: Int = 0, // シャープネス (-2～+2)
        @SerialName("Contrast") val contrast: Int = 0, // コントラスト (-2～+2)
        @SerialName("Saturation") val saturation: Int = 0, // 彩度 (-2～+2)
        @SerialName("EffectType") val effectType: String = "", // アートフィルターバリエーション (TYPE1など)
        @SerialName("FantasicFocus") val fantasicFocus: String = "OFF", // ファンタジックフォーカス効果 (ON/OFF)
        @SerialName("ToyPhoto") val toyPhoto: String = "OFF", // ピンホール効果 (ON/OFF)
        @SerialName("WhiteEdge") val whiteEdge: String = "OFF", // ホワイトエッジ効果 (ON/OFF)
        @SerialName("FrameJaggy") val frameJaggy: String = "OFF", // フレーム効果 (ON/OFF)
        @SerialName("Starlight") val starlight: String = "OFF", // スターライト効果 (ON/OFF)
        @SerialName("MiniatureVerticl") val miniatureVerticl: String = "OFF", // 上下ぼかし効果 (ON/OFF)

        // --- p.75 掲載パラメータ ---
        @SerialName("MiniatureHorizon") val miniatureHorizon: String = "OFF", // 左右ぼかし効果 (ON/OFF)
        @SerialName("ShadingHorizon") val shadingHorizon: String = "OFF", // 上下シェード効果 (ON/OFF)
        @SerialName("ShadingVertical") val shadingVertical: String = "OFF", // 左右シェード効果 (ON/OFF)
        @SerialName("MonotoneFilter") val monotoneFilter: String = "", // モノクロフィルター効果 (NORMALなど)
        @SerialName("MonotoneColor") val monotoneColor: String = "", // 調色効果 (NORMALなど)
        @SerialName("ColorCreatorColor") val colorCreatorColor: Int = 0, // カラークリエーター用色相 (0～29)
        @SerialName("ColorCreatorVivid") val colorCreatorVivid: Int = 0, // カラークリエーター用彩度 (-4～+3)
        @SerialName("ColorPhase") val colorPhase: Int = 0, // パートカラー色相 (0～17)
        @SerialName("WhiteBalance") val whiteBalance: String = "AUTO", // WBモード
        @SerialName("CustomWBBias") val customWbBias: Int = 0, // CWB時の色温度 (2000～14000)
        @SerialName("WBAutoLightBulbColorLeaving") val wbAutoLightBulbColorLeaving: String = "OFF", // WBオート電球色残し (ON/OFF)

        // --- p.76 掲載パラメータ ---
        @SerialName("WBBiasA") val wbBiasA: Int = 0, // WB補正(A) (-7～+7)
        @SerialName("WBBiasG") val wbBiasG: Int = 0, // WB補正(G) (-7～+7)
        @SerialName("ToneControlHigh") val toneControlHigh: Int = 0, // トーンコントロール high 設定 (-7～+7)
        @SerialName("ToneControlMiddle") val toneControlMiddle: Int = 0, // トーンコントロール middle 設定 (-7～+7)
        @SerialName("ToneControlShadow") val toneControlShadow: Int = 0, // トーンコントロール shadow 設定 (-7～+7)
        @SerialName("AspectRatio") val aspectRatio: String = "", // アスペクト比 (例: "04_03")
        @SerialName("PhotoStory") val photoStory: String = "NG", // フォトストーリー有無 (OK/NG)
        @SerialName("PhotoStoryMode") val photoStoryMode: String = "", // フォトストーリーモード
        @SerialName("PhotoStoryType") val photoStoryType: String = "", // フォトストーリータイプ
        @SerialName("PhotoStoryDevideNumAspectRatio") val photoStoryDevideNumAspectRatio: String = "", // フォトストーリー分割数・アスペクト比

        // --- p.77 掲載パラメータ ---
        @SerialName("PhotoStoryLayout") val photoStoryLayout: String = "", // フォトストーリーレイアウト (例: "02_02_03")
        @SerialName("PhotoStoryEffect") val photoStoryEffect: String = "", // フォトストーリー効果
        @SerialName("RollAngleReliability") val rollAngleReliability: String = "NG", // ロール角の信頼性有無 (OK/NG)
        @SerialName("PitchAngleReliability") val pitchAngleReliability: String = "NG", // ピッチ角の信頼性有無 (OK/NG)
        @SerialName("Location") val location: String = "", // 本体の位置
        @SerialName("RoleAngle") val roleAngle: Int = 0, // ロール角度 (10倍された値 -900～900)
        @SerialName("PitchAngle") val pitchAngle: Int = 0, // ピッチ角度 (10倍された値 -900～900)

        // ※ 複数存在する場合、同じXMLエレメントが並ぶためListで定義
        @SerialName("LensID") val lensId: List<Int> = emptyList(), // レンズID (10進数)

        // --- p.78 掲載パラメータ ---
        // ※ 複数存在する場合、同じXMLエレメントが並ぶためListで定義
        @SerialName("AccessaryID") val accessaryId: List<Int> = emptyList(), // アクセサリID (10進数)
        @SerialName("CameraName") val cameraName: String = "" // 本体名
    )

    // --- ON/OFF や OK/NG を Boolean に変換するヘルパー
    val StillFileParameterInfo.isLocationAvailable: Boolean get() = this.locationStatus == "OK"
    val StillFileParameterInfo.isDigitalTeleconOn: Boolean get() = this.digitalTelecon == "ON"

    // --- 10倍された角度を実際の度数（Double）に変換するヘルパー
    val StillFileParameterInfo.actualRoleAngle: Double get() = this.roleAngle / 10.0
    val StillFileParameterInfo.actualPitchAngle: Double get() = this.pitchAngle / 10.0
}
