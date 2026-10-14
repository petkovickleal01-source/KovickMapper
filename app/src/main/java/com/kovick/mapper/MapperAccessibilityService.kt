package com.kovick.mapper

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class MapperAccessibilityService : AccessibilityService() {
    companion object {
        var instance: MapperAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Reservado para detecção de janela/jogo e gerenciamento dos perfis.
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }
}