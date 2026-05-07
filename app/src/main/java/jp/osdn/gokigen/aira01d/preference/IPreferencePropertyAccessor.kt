package jp.osdn.gokigen.aira01d.preference

interface IPreferencePropertyAccessor
{

    companion object
    {
        // --- PREFERENCE KEY AND DEFAULT VALUE ---

        // --- CAMERA CONNECTION METHOD PREFERENCES
        const val PREFERENCE_CAMERA_METHOD_INDEX = "camera_method"
        const val PREFERENCE_CAMERA_METHOD_INDEX_DEFAULT_VALUE = "1"

        // --- AUTO CONNECT TO CAMERA PREFERENCES
        const val PREFERENCE_CAMERA_CONNECT_AUTOMATICALLY = "auto_connect"
        const val PREFERENCE_CAMERA_CONNECT_AUTOMATICALLY_DEFAULT_VALUE = "1"
    }
}
