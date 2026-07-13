package jp.osdn.gokigen.aira01d.ui.model

data class ExifDataToDisplay(
    val fileName: String,
    val focalLength: Double?,
    val aperture: String?,
    val exposureTime: String?,
    val iso: String?,
    val meteringMode: String?,
    val programMode: String?,
    val model: String?,
    val hasGpsInfo: Boolean?
)
