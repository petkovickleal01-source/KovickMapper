package com.kovick.mapper

import android.content.Context

object KovickProfileStore {
    private const val PREFS = "kovick_profiles"
    private const val ACTIVE = "active_profile"
    private const val PROFILES = "profiles"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun profiles(context: Context): List<String> {
        val stored = prefs(context).getStringSet(PROFILES, null)
        if (stored.isNullOrEmpty()) {
            val defaults = linkedSetOf("Free Fire", "Roblox")
            prefs(context).edit().putStringSet(PROFILES, defaults).apply()
            return defaults.toList()
        }
        return stored.toList().sorted()
    }

    fun addProfile(context: Context, name: String): Boolean {
        val clean = name.trim()
        if (clean.isEmpty()) return false
        val set = prefs(context).getStringSet(PROFILES, emptySet())!!.toMutableSet()
        if (!set.add(clean)) return false
        prefs(context).edit().putStringSet(PROFILES, set).apply()
        setActiveProfile(context, clean)
        return true
    }

    fun activeProfile(context: Context): String =
        prefs(context).getString(ACTIVE, profiles(context).first()) ?: profiles(context).first()

    fun setActiveProfile(context: Context, name: String) {
        prefs(context).edit().putString(ACTIVE, name).apply()
    }

    fun savePoint(context: Context, keyCode: Int, x: Float, y: Float) {
        prefs(context).edit()
            .putFloat("${activeProfile(context)}_x_$keyCode", x.coerceIn(0f, 1f))
            .putFloat("${activeProfile(context)}_y_$keyCode", y.coerceIn(0f, 1f))
            .apply()
    }

    fun point(context: Context, keyCode: Int): Pair<Float, Float>? {
        val p = prefs(context)
        val prefix = "${activeProfile(context)}_"
        if (!p.contains(prefix + "x_$keyCode")) return null
        return p.getFloat(prefix + "x_$keyCode", 0.5f) to
            p.getFloat(prefix + "y_$keyCode", 0.5f)
    }
}
