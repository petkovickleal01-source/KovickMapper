package com.kovick.mapper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.DisplayMetrics
import android.view.KeyEvent

class KovickAccessibilityService : AccessibilityService() {
    companion object {
        @Volatile var instance: KovickAccessibilityService? = null
        private const val HOLD_DURATION_MS = 60_000L
    }

    private val pressedKeys = linkedSetOf<Int>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        pressedKeys.clear()
        instance = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) = Unit
    override fun onInterrupt() {
        pressedKeys.clear()
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val mapped = KovickProfileStore.point(this, keyCode) ?: return false

        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                if (event.repeatCount == 0) {
                    pressedKeys.add(keyCode)
                    dispatchMappedKeys()
                }
                return true
            }

            KeyEvent.ACTION_UP -> {
                if (pressedKeys.remove(keyCode)) {
                    dispatchMappedKeys()
                }
                return true
            }
        }

        return mapped.let { true }
    }

    private fun dispatchMappedKeys() {
        if (pressedKeys.isEmpty()) {
            dispatchGesture(GestureDescription.Builder().build(), null, null)
            return
        }

        val metrics = resources.displayMetrics
        val builder = GestureDescription.Builder()
        var duration = 1L

        pressedKeys.forEach { keyCode ->
            val point = KovickProfileStore.point(this, keyCode) ?: return@forEach
            val x = point.first.coerceIn(0f, 1f) * metrics.widthPixels
            val y = point.second.coerceIn(0f, 1f) * metrics.heightPixels
            val path = Path().apply { moveTo(x, y) }
            builder.addStroke(
                GestureDescription.StrokeDescription(path, 0L, HOLD_DURATION_MS)
            )
            duration = HOLD_DURATION_MS
        }

        if (duration > 0L) {
            dispatchGesture(builder.build(), null, null)
        }
    }

    fun tapNormalized(x: Float, y: Float) {
        val metrics = resources.displayMetrics
        val path = Path().apply {
            moveTo(
                x.coerceIn(0f, 1f) * metrics.widthPixels,
                y.coerceIn(0f, 1f) * metrics.heightPixels
            )
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, 1L))
            .build()
        dispatchGesture(gesture, null, null)
    }
}
