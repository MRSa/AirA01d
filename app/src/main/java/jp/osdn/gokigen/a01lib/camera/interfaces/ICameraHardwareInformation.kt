package jp.osdn.gokigen.a01lib.camera.interfaces

interface ICameraHardwareInformation
{
    fun getCameraStatus(callback: Callback)

    interface Callback {
        fun operationResult(result: Map<String, String?>)
    }

    object Hardware {
        const val CARDSTATUS = "cardstatus"
        const val CARDREMAINNUM = "cardremainnum"
        const val CARDREMAINSEC = "cardremainsec"
        const val CARDREMAINBYTE = "cardremainbyte"
        const val LENSMOUNTSTATUS = "lensmountstatus"
        const val IMAGINGSTATE = "imagingstate"
        const val FOCALLENGTH = "focallength"
        const val WIDEFOCALLENGTH = "widefocallength"
        const val TELEFOCALLENGTH = "telefocallength"
        const val ELECTRICZOOM = "electriczoom"
        const val MACROSETTING = "macrosetting"
    }
}
