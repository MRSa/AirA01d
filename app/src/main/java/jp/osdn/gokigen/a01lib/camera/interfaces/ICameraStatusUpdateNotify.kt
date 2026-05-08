package jp.osdn.gokigen.a01lib.camera.interfaces

interface ICameraStatusUpdateNotify
{
    fun changedTakeMode(newMode: String)
    fun updatedShutterSpeed(tv: String)
    fun updatedAperture(av: String)
    fun updatedExposureCompensation(xv: String)
    fun updateIsoSensitivity(sv: String)
    fun updateFocusMode(focusMode: String)
    fun updateExposureWarning(exposureWarning: String)
    fun updateDriveMode(driveMode: String)
    fun updatePictureEffect(pictureEffect: String)
    fun updatedWhiteBalance(whiteBalance: String)
    fun updatedRawMode(rawMode: String)
    fun updatedAspectRatio(aspectRatio: String)
    fun updatedAeLockState(aeLockState: String)
    fun updateRemainBattery(batteryLevel: String)
    fun updatedMeteringMode(meteringMode: String)

    // fun updatedAfLockState(afLockState: String)

    //fun updateRemainBattery(percentage: Int)

    //fun updateStorageStatus(status: String?)
    //fun updateShootMode(shootMode: String?)
}
