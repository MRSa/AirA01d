package jp.osdn.gokigen.a01lib.camera.omds.wrapper

import android.graphics.PointF
import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.IFocusingControl
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsAeLockControl
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsAutoFocusControl
import java.lang.Exception

class OmdsFocusControl : IFocusingControl
{
    private val afControl = OmdsAutoFocusControl()
    private val aeControl = OmdsAeLockControl()

    fun setUseOpcProtocol(isOpcProtocol: Boolean)
    {
        afControl.setUseOpcProtocol(isOpcProtocol)
        aeControl.setUseOpcProtocol(isOpcProtocol)
    }

    override fun driveAutoFocus(posX: Float, posY: Float): Boolean
    {
        //Log.v(TAG, "driveAutoFocus()")
        try
        {
            afControl.lockAutoFocus(PointF(posX, posY))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return false
    }

    override fun unlockAutoFocus()
    {
        //Log.v(TAG, "unlockAutoFocus()")
        try
        {
            afControl.unlockAutoFocus()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun lockAutoExposure()
    {
        Log.v(TAG, "lockAutoExposure()")
        try
        {
            aeControl.lockAe()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override  fun unlockAutoExposure()
    {
        Log.v(TAG, "unlockAutoExposure()")
        try
        {
            aeControl.unLockAe()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = OmdsFocusControl::class.java.simpleName
    }
}
