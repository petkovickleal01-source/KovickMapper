package com.kovick.mapper

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast

class KovickAccessibilityService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var kovickButton: View? = null

    override fun onServiceConnected() {
        super.onServiceConnected()

        if (Settings.canDrawOverlays(this)) {
            showKovickButton()
        } else {
            Toast.makeText(
                this,
                "Permissão de sobreposição necessária para o Kovick.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showKovickButton() {

        if (kovickButton != null) return

        windowManager =
            getSystemService(WINDOW_SERVICE) as WindowManager

        val button = TextView(this).apply {

            text = "K"

            textSize = 18f

            setTextColor(
                android.graphics.Color.WHITE
            )

            gravity = Gravity.CENTER

            setBackgroundColor(
                android.graphics.Color.rgb(
                    139,
                    92,
                    246
                )
            )

            setOnClickListener {
                openKovickEditor()
            }

            setOnLongClickListener {

                removeKovickButton()

                true
            }
        }

        val type =
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY

        val params =
            WindowManager.LayoutParams(
                64,
                64,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )

        params.gravity =
            Gravity.TOP or Gravity.END

        params.x = 24
        params.y = 120

        try {

            windowManager?.addView(
                button,
                params
            )

            kovickButton = button

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Não foi possível criar o botão Kovick.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openKovickEditor() {

        val intent =
            Intent(
                this,
                MainActivity::class.java
            )

        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        )

        intent.putExtra(
            "open_editor",
            true
        )

        startActivity(intent)
    }

    private fun removeKovickButton() {

        try {

            kovickButton?.let {
                windowManager?.removeView(it)
            }

        } catch (_: Exception) {
        }

        kovickButton = null
    }

    override fun onAccessibilityEvent(
        event: android.view.accessibility.AccessibilityEvent?
    ) {
        // Eventos do aplicativo em primeiro plano
        // serão tratados posteriormente.
    }

    override fun onInterrupt() {
        // Serviço interrompido pelo Android.
    }

    override fun onDestroy() {

        removeKovickButton()

        super.onDestroy()
    }
}
