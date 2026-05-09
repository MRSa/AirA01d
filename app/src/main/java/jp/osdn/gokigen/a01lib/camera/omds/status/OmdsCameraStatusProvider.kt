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
    private var currentExposureWarning = 0
    override fun updateExposureWarning(exposureWarning: Int) {
        currentExposureWarning = notifyIfChanged(currentExposureWarning, exposureWarning) { it.updateExposureWarning(exposureWarning) }
    }

    // ----- ドライブモード
    private var currentDriveMode = ""
    override fun updateDriveMode(driveMode: String) {
        currentDriveMode = notifyIfChanged(currentDriveMode, driveMode) { it.updateDriveMode(driveMode) }
    }

    // ----- ピクチャーモード
    private var currentPictureEffect = ""
    override fun updatePictureEffect(pictureEffect: String) {
        currentPictureEffect = notifyIfChanged(currentPictureEffect, pictureEffect) { it.updatePictureEffect(pictureEffect) }
    }

    // ----- アートフィルター
    private var currentArtFilter = ""
    override fun updateArtFilter(artFilter: String) {
        currentArtFilter = notifyIfChanged(currentArtFilter, artFilter) { it.updateArtFilter(artFilter) }
    }

    // ----- ホワイトバランス
    private var currentWhiteBalance = ""
    override fun updatedWhiteBalance(whiteBalance: String) {
        currentWhiteBalance = notifyIfChanged(currentWhiteBalance, whiteBalance) { it.updatedWhiteBalance(whiteBalance) }
    }

    // ----- RAWモード
    private var currentRawMode = ""
    override fun updatedRawMode(rawMode: String) {
        currentRawMode = notifyIfChanged(currentRawMode, rawMode) { it.updatedRawMode(rawMode) }
    }

    // ----- アスペクト比
    private var currentAspectRatio = ""
    override fun updatedAspectRatio(aspectRatio: String) {
        currentAspectRatio = notifyIfChanged(currentAspectRatio, aspectRatio) { it.updatedAspectRatio(aspectRatio) }
    }

    // ----- AEロック状態
    private var currentAeLockState = ""
    override fun updatedAeLockState(aeLockState: String) {
        currentAeLockState = notifyIfChanged(currentAeLockState, aeLockState) { it.updatedAeLockState(aeLockState) }
    }

    // ----- バッテリー残量
    private var currentBatteryLevel = ""
    override fun updateRemainBattery(batteryLevel: String) {
        currentBatteryLevel = notifyIfChanged(currentBatteryLevel, batteryLevel) { it.updateRemainBattery(batteryLevel) }
    }

    // ----- 測光モード
    private var currentMeteringMode = ""
    override fun updatedMeteringMode(meteringMode: String) {
        currentMeteringMode = notifyIfChanged(currentMeteringMode, meteringMode) { it.updatedMeteringMode(meteringMode) }
    }

    private var currentMediaStatus = 0
    override fun updatedMediaStatus(mediaStatus: Int) {
        currentMediaStatus = notifyIfChanged(currentMediaStatus, mediaStatus) { it.updatedMediaStatus(mediaStatus) }
    }

    private var currentOrientation = 0
    override fun updatedOrientation(orientation: Int) {
        currentOrientation = notifyIfChanged(currentOrientation, orientation) { it.updatedOrientation(orientation) }
    }

    private var currentNumOfImages = 0
    override fun updatedAvailableShots(numOfImages: Int) {
        currentNumOfImages = notifyIfChanged(currentNumOfImages, numOfImages) { it.updatedOrientation(numOfImages) }
    }

    private var currentWideLength = 0
    private var currentLength = 0
    private var currentTeleLength = 0
    override fun updatedZoomInfo(wide: Int, current: Int, tele: Int) {
        if ((wide != currentWideLength)||(current != currentLength)||(tele != currentTeleLength))
        {
            subscriberList.forEach {
                try {
                    it.updatedZoomInfo(wide, current, tele)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            currentWideLength = wide
            currentLength = current
            currentTeleLength = tele
        }
    }

    private var currentAccuracy = 0
    private var currentLevelOrientation = 0
    private var currentRoll = 0
    private var currentPitch = 0
    override fun updatedLevelGauge(
        accuracy: Int,
        orientation: Int,
        roll: Int,
        pitch: Int
    ) {
        if ((accuracy != currentAccuracy)||(orientation != currentLevelOrientation)||(roll != currentRoll)||(pitch != currentPitch))
        {
            subscriberList.forEach {
                try {
                    it.updatedLevelGauge(accuracy, orientation, roll, pitch)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            currentAccuracy = accuracy
            currentLevelOrientation = orientation
            currentRoll = roll
            currentPitch = pitch
        }
    }
}
