package jp.osdn.gokigen.a01lib.camera.interfaces.screen

interface IIndicatorControl
{
    // --- 撮影状態の記録
    enum class ShootingStatus {
        Unknown, Starting, Started, Stopping, Stopped
    }
    fun onAfLockUpdate(focusingStatus: IAutoFocusFrameDisplay.FocusFrameStatus)
    fun onShootingStatusUpdate(status: ShootingStatus?)
    fun onMovieStatusUpdate(status: ShootingStatus?)
    fun onBracketingStatusUpdate(message: String?)
}
