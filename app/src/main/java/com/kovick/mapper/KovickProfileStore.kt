package com.kovick.mapper

import android.content.Context

/** Persistent per-game mapping store used by the mapper service and editor. */
class KovickProfileStore(context: Context) {
    private val prefs = context.getSharedPreferences("kovick_profiles_v2", Context.MODE_PRIVATE)

    fun profileKey(packageName: String) = "profile_$packageName"

    fun get(packageName: String, keyCode: Int): Mapping? {
        val raw = prefs.getString("${profileKey(packageName)}_$keyCode", null) ?: return null
        val p = raw.split(',')
        if (p.size != 3) return null
        return Mapping(p[0].toFloatOrNull() ?: return null, p[1].toFloatOrNull() ?: return null, p[2].toIntOrNull() ?: return null)
    }

    fun put(packageName: String, keyCode: Int, mapping: Mapping) {
        prefs.edit().putString("${profileKey(packageName)}_$keyCode", "${mapping.x},${mapping.y},${mapping.action}").apply()
    }

    fun sensitivity(packageName: String): Float = prefs.getFloat("${profileKey(packageName)}_sensitivity", 1.0f)

    fun setSensitivity(packageName: String, value: Float) {
        prefs.edit().putFloat("${profileKey(packageName)}_sensitivity", value.coerceIn(0.1f, 5f)).apply()
    }

    data class Mapping(val x: Float, val y: Float, val action: Int = ACTION_TAP)

    companion object {
        const val ACTION_TAP = 0
    }
}
