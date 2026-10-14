package com.kovick.mapper

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class KovickAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {
        // Eventos de acessibilidade serão usados
        // posteriormente pelo sistema de mapeamento.
    }

    override fun onInterrupt() {
        // Serviço interrompido pelo Android.
    }
}
