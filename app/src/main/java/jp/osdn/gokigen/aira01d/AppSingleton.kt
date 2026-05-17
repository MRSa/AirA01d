package jp.osdn.gokigen.aira01d

import android.app.Application
import android.util.Log
import jp.osdn.gokigen.a01lib.camera.omds.OmdsCameraControlSingleton

class AppSingleton : Application()
{
    override fun onCreate()
    {
        super.onCreate()
        Log.v(TAG, "AppSingleton::create()")
        cameraControl = OmdsCameraControlSingleton()
    }

    companion object
    {
        private val TAG = AppSingleton::class.java.simpleName
        lateinit var cameraControl: OmdsCameraControlSingleton
        val resourceConverter = StringResourceConverter()
    }
}
