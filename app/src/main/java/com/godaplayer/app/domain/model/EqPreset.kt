package com.godaplayer.app.domain.model

data class EqPreset(
    val id: Long = 0,
    val name: String,
    val isCustom: Boolean = true,
    val band60Hz: Int = 0,      // -1200 to +1200 millibels (-12 to +12 dB)
    val band250Hz: Int = 0,
    val band1kHz: Int = 0,
    val band4kHz: Int = 0,
    val band16kHz: Int = 0,
    val bassBoost: Int = 0,     // 0-1000
    val virtualizer: Int = 0,   // 0-1000
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        // Helper to convert dB to millibels for UI display
        fun dbToMillibels(db: Float): Int = (db * 100).toInt()
        fun millibelsToDb(mb: Int): Float = mb / 100f

        // Helper to convert bass boost percentage to strength
        fun dbToBassBoostStrength(db: Float): Int = ((db / 15f) * 1000).toInt().coerceIn(0, 1000)
        fun bassBoostStrengthToDb(strength: Int): Float = (strength / 1000f) * 15f

        // Helper to convert virtualizer percentage
        fun percentToVirtualizerStrength(percent: Float): Int = ((percent / 100f) * 1000).toInt().coerceIn(0, 1000)
        fun virtualizerStrengthToPercent(strength: Int): Float = (strength / 1000f) * 100f

        val FLAT = EqPreset(name = "Flat", isCustom = false)
    }
}
