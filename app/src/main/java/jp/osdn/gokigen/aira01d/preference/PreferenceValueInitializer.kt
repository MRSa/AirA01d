package jp.osdn.gokigen.aira01d.preference

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.core.content.edit

class PreferenceValueInitializer
{
    fun initializePreferences(context : Context)
    {
        try
        {
            Log.v(TAG, "initializePreferences()")
            val preferences = PreferenceManager.getDefaultSharedPreferences(context) ?: return
            val items : Map<String, *> = preferences.all
            preferences.edit {

                if (!items.containsKey(IPreferencePropertyAccessor.PREFERENCE_CAMERA_METHOD_INDEX)) {
                    putString(
                        IPreferencePropertyAccessor.PREFERENCE_CAMERA_METHOD_INDEX,
                        IPreferencePropertyAccessor.PREFERENCE_CAMERA_METHOD_INDEX_DEFAULT_VALUE
                    )
                }

                if (!items.containsKey(IPreferencePropertyAccessor.PREFERENCE_CAMERA_CONNECT_AUTOMATICALLY)) {
                    putString(
                        IPreferencePropertyAccessor.PREFERENCE_CAMERA_CONNECT_AUTOMATICALLY,
                        IPreferencePropertyAccessor.PREFERENCE_CAMERA_CONNECT_AUTOMATICALLY_DEFAULT_VALUE
                    )
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = PreferenceValueInitializer::class.java.simpleName
    }
}
