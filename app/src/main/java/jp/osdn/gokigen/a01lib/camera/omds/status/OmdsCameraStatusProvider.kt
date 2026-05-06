package jp.osdn.gokigen.a01lib.camera.omds.status

import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatusUpdateNotify

class OmdsCameraStatusProvider : ICameraStatusUpdateNotify
{
    // ----- 送信先
    private val subscriberList = mutableSetOf<ICameraStatusUpdateNotify>()

    // ----- 送信先の登録/登録解除
    fun subscribe(subscriber: ICameraStatusUpdateNotify) = subscriberList.add(subscriber)
    fun unsubscribe(subscriber: ICameraStatusUpdateNotify) = subscriberList.remove(subscriber)

    // ----- 共通の更新処理 -----
    private fun <T> notifyIfChanged(current: T, next: T, action: (ICameraStatusUpdateNotify) -> Unit): T {
        if (next != null && next != "" && next != current) {
            subscriberList.forEach {
                runCatching { action(it) }.onFailure { it -> it.printStackTrace() }
            }
            return next
        }
        return current
    }

    // ----- 撮影モード
    private var currentTakeMode = ""
    override fun changedTakeMode(newMode: String) {
        currentTakeMode = notifyIfChanged(currentTakeMode, newMode) { it.changedTakeMode(newMode) }
    }

    // ----- シャッター速度
    private var currentShutterSpeed = ""
    override fun updatedShutterSpeed(tv: String) {
        currentShutterSpeed = notifyIfChanged(currentShutterSpeed, tv) { it.updatedShutterSpeed(tv) }
    }

    // ----- 絞り値
    private var currentAperture = ""
    override fun updatedAperture(av: String) {
        currentAperture = notifyIfChanged(currentAperture, av) { it.updatedAperture(av) }
    }

    // ----- 露出補正値
    private var currentExposureCompensation = ""
    override fun updatedExposureCompensation(xv: String) {
        currentExposureCompensation = notifyIfChanged(currentExposureCompensation, xv) { it.updatedExposureCompensation(xv) }
    }

    // ----- ISO感度
    private var currentIsoSensitivity = ""
    override fun updateIsoSensitivity(sv: String) {
        currentIsoSensitivity = notifyIfChanged(currentIsoSensitivity, sv) { it.updateIsoSensitivity(sv) }
    }

    // ----- フォーカスモード
    private var currentFocusMode = ""
    override fun updateFocusMode(focusMode: String) {
        currentFocusMode = notifyIfChanged(currentFocusMode, focusMode) { it.updateFocusMode(focusMode) }
    }

    // ----- 露出警告
    private var currentExposureWarning = ""
    override fun updateExposureWarning(exposureWarning: String) {
        currentExposureWarning = notifyIfChanged(currentExposureWarning, exposureWarning) { it.updateFocusMode(exposureWarning) }
    }

    // ----- ドライブモード
    private var currentDriveMode = ""
    override fun updateDriveMode(driveMode: String) {
        currentDriveMode = notifyIfChanged(currentDriveMode, driveMode) { it.updateFocusMode(driveMode) }
    }

    // ----- ピクチャーモード
    private var currentPictureEffect = ""
    override fun updatePictureEffect(pictureEffect: String) {
        currentPictureEffect = notifyIfChanged(currentPictureEffect, pictureEffect) { it.updateFocusMode(pictureEffect) }
    }

    // ----- ホワイトバランス
    private var currentWhiteBalance = ""
    override fun updatedWhiteBalance(whiteBalance: String) {
        currentWhiteBalance = notifyIfChanged(currentWhiteBalance, whiteBalance) { it.updateFocusMode(whiteBalance) }
    }
}
