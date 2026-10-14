package com.kovick.mapper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.KeyEvent

/** Converts physical keyboard events into gestures for the selected game's profile. */
class KovickInputRouter(private val service: AccessibilityService) {
    private val store = KovickProfileStore(service)

    fun handleKey(packageName: String, event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN || event.repeatCount != 0) return false
        val mapping = store.get(packageName, event.keyCode) ?: return false
        val path = Path().apply { moveTo(mapping.x, mapping.y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 35))
            .build()
        return service.dispatchGesture(gesture, null, null)
    }
}
