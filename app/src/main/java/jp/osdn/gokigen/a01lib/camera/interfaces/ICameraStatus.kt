package jp.osdn.gokigen.a01lib.camera.interfaces

interface ICameraStatus
{
    enum class CameraProperty
    {
        TakeMode,      // プログラムモード(P/A/S/M/iAuto)
        ShutterSpeed,  // シャッタースピード
        Aperture,      // 絞り値
        ExposureCompensation,  // 露出補正値
        IsoSensitivity,  // ISO感度
        WhiteBalance,    // ホワイトバランス
        PictureEffect,   // ピクチャーエフェクト（フォトスタイル）
        FocusMode,     // フォーカス状態 (SAF/CAF/MF)
        DriveMode,       // ドライブモード（単写/連写)
        AfLockState,     // AFロック状態
        AeLockState,     // AEロック状態
        MeteringMode,    // 測光モード
        CaptureMode,     // キャプチャーモード
        BatteryRemain,   // バッテリ残量
        TorchMode,       // トーチモード
        FocalLength,     // 焦点距離
        RemainShots,     // 残り撮影枚数
        ImageSize,       // 撮影画像サイズ
        MovieSize,       // 動画画像サイズ
    }

    fun getStatusList(key: CameraProperty): List<String>
    fun getStatus(key: CameraProperty): String
    fun setStatus(key: CameraProperty, value: String)

}
