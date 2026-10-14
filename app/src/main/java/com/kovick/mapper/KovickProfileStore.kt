package com.kovick.mapper

import android.content.Context

object KovickProfileStore {
    private const val PREFS = "kovick_profiles"
    private const val ACTIVE = "active_profile"

    fun activeProfile(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(ACTIVE, "Default") ?: "Default"

    fun setActiveProfile(context: Context, name: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(ACTIVE, name).apply()
    }

    fun savePoint(context: Context, keyCode: Int, x: Float, y: Float) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putFloat("${activeProfile(context)}_x_$keyCode", x)
            .putFloat("${activeProfile(context)}_y_$keyCode", y)
            .apply()
    }

    fun point(context: Context, keyCode: Int): Pair<Float, Float>? {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val prefix = "${activeProfile(context)}_"
        if (!p.contains(prefix + "x_$keyCode")) return null
        return p.getFloat(prefix + "x_$keyCode", 0.5f) to
            p.getFloat(prefix + "y_$keyCode", 0.5f)
    }
}
