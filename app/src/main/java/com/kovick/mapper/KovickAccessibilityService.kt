package com.kovick.mapper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.KeyEvent

class KovickAccessibilityService : AccessibilityService() {
    companion object {
        @Volatile var instance: KovickAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN || event.repeatCount > 0) return false
        val point = KovickProfileStore.point(this, event.keyCode) ?: return false
        tapNormalized(point.first, point.second)
        return true
    }

    fun tapNormalized(x: Float, y: Float) {
        val metrics = resources.displayMetrics
        val path = Path().apply {
            moveTo(x.coerceIn(0f, 1f) * metrics.widthPixels, y.coerceIn(0f, 1f) * metrics.heightPixels)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 1))
            .build()
        dispatchGesture(gesture, null, null)
    }
}
