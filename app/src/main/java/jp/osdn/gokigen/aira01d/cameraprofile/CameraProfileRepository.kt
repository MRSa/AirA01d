package jp.osdn.gokigen.aira01d.cameraprofile

import android.content.Context

class CameraProfileRepository(private val context: Context)
{

    fun storeAllCameraProfiles(fileNameToPull: String): Boolean
    {
        if (fileNameToPull.isEmpty())
        {
            // ファイル名が指定されていないときはエラー応答
            return false
        }

        return true
    }

    private fun getAllCameraProfiles() : List<String>
    {
        return emptyList()
    }

    companion object {
        private val TAG = CameraProfileRepository::class.java.simpleName
    }
}
