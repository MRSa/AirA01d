package jp.osdn.gokigen.a01lib.camera.interfaces.playback

import java.util.Date

interface ICameraFileInfo
{
    data class ImageFileInfo(
        val directory: String,
        val fileName: String,
        val fileSize: Long,
        val isReadonly: Boolean,
        val isHidden: Boolean,
        val isSystem: Boolean,
        val isVolume: Boolean,
        val isDirectory: Boolean,
        val isArchive: Boolean,
        val dateTime: Date,
    )

    data class MovieFileInfo(
        val playTime: Int,
        val movieSize: String,
        val isShortMovie: Boolean,
        val dateTime: Date,
    )


/*

    fun getDatetime(): Date?
    fun getDirectoryPath(): String?
    fun getFilename(): String?
    fun getOriginalFilename(): String?

    fun getAperture(): String?
    fun getShutterSpeed(): String?
    fun getIsoSensitivity(): String?
    fun getExpRev(): String?
    fun getOrientation(): Int
    fun getAspectRatio(): String?
    fun getModel(): String?
    fun getLatLng(): String?
    fun getCaptured(): Boolean

    fun updateValues(
        dateTime: String,
        av: String,
        tv: String,
        sv: String,
        xv: String,
        orientation: Int,
        aspectRatio: String,
        model: String,
        latLng: String,
        captured: Boolean
    )
*/
}
