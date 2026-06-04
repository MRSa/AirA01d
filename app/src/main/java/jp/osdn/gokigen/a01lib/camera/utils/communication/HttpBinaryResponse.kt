package jp.osdn.gokigen.a01lib.camera.utils.communication

data class HttpBinaryResponse(
    val responseCode: Int,
    val headers: Map<String, List<String>>, // 全てのレスポンスヘッダ
    val body: ByteArray?                    // バイナリデータ（画像やファイル）
)
