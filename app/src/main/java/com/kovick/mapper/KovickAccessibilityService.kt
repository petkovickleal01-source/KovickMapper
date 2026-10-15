package com.kovick.mapper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
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
        dispatchGesture(GestureDescription.Builder().build(), null, null)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (KovickProfileStore.point(this, event.keyCode) == null) return false

        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                if (event.repeatCount == 0) {
                    pressedKeys.add(event.keyCode)
                    dispatchMappedKeys()
                }
                return true
            }

            KeyEvent.ACTION_UP -> {
                if (pressedKeys.remove(event.keyCode)) {
                    dispatchMappedKeys()
                }
                return true
            }
        }

        return true
    }

    private fun dispatchMappedKeys() {
        val builder = GestureDescription.Builder()
        val metrics = resources.displayMetrics
        var strokes = 0

        pressedKeys.forEach { keyCode ->
            val point = KovickProfileStore.point(this, keyCode) ?: return@forEach
            val x = point.first.coerceIn(0f, 1f) * metrics.widthPixels
            val y = point.second.coerceIn(0f, 1f) * metrics.heightPixels
            val path = Path().apply { moveTo(x, y) }
            builder.addStroke(
                GestureDescription.StrokeDescription(
                    path,
                    0L,
                    HOLD_DURATION_MS
                )
            )
            strokes++
        }

        if (strokes == 0) {
            // Starting an empty gesture cancels the previous long-running gesture
            // without generating another touch point.
            dispatchGesture(builder.build(), null, null)
            return
        }

        dispatchGesture(builder.build(), null, null)
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
