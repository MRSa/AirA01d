package jp.osdn.gokigen.aira01d

interface IVibrator
{
    enum class VibratePattern
    {
        NONE, SIMPLE_SHORT_SHORT, SIMPLE_SHORT, SIMPLE_MIDDLE, SIMPLE_LONG
    }

    fun vibrate(vibratePattern: VibratePattern)

}