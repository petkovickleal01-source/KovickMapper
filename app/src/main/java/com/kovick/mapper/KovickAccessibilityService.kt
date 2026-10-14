package com.kovick.mapper

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class KovickAccessibilityService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var kovickButton: View? = null
    private var mapperPanel: View? = null
    private var panelParams: WindowManager.LayoutParams? = null

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

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val button = TextView(this).apply {
            text = "K"
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = roundedBackground(Color.rgb(139, 92, 246), 18f)
            elevation = 12f
            setOnClickListener { toggleMapperPanel() }
            setOnLongClickListener {
                removeMapperPanel()
                removeKovickButton()
                true
            }
        }

        val params = WindowManager.LayoutParams(
            64,
            64,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.END
        params.x = 24
        params.y = 120

        try {
            windowManager?.addView(button, params)
            kovickButton = button
        } catch (_: Exception) {
            Toast.makeText(this, "Não foi possível criar o botão Kovick.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleMapperPanel() {
        if (mapperPanel != null) {
            removeMapperPanel()
        } else {
            showMapperPanel()
        }
    }

    private fun showMapperPanel() {
        if (mapperPanel != null) return

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 16, 18, 16)
            background = roundedBackground(Color.argb(245, 14, 14, 22), 24f)
            elevation = 18f
        }

        val titleRow = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(this).apply {
            text = "KOVICK  •  MAPPER"
            textSize = 16f
            setTextColor(Color.WHITE)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val close = TextView(this).apply {
            text = "✕"
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(18, 6, 6, 6)
            setOnClickListener { removeMapperPanel() }
        }

        titleRow.addView(title, LinearLayout.LayoutParams(0, 48, 1f))
        titleRow.addView(close, LinearLayout.LayoutParams(48, 48))
        panel.addView(titleRow)

        val subtitle = TextView(this).apply {
            text = "Ferramentas rápidas de mapeamento"
            textSize = 12f
            setTextColor(Color.rgb(170, 170, 185))
            setPadding(2, 0, 2, 14)
        }
        panel.addView(subtitle)

        addPanelButton(panel, "⌨  ADICIONAR TECLA", Color.rgb(42, 42, 55)) {
            openEditor("keyboard")
        }
        addPanelButton(panel, "🖱  MOUSE / MIRA", Color.rgb(42, 42, 55)) {
            openEditor("mouse")
        }
        addPanelButton(panel, "🎯  MODO AIM", Color.rgb(55, 38, 85)) {
            openEditor("aim")
        }
        addPanelButton(panel, "⚙  SENSIBILIDADE", Color.rgb(42, 42, 55)) {
            openEditor("settings")
        }

        val bottomRow = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 12, 0, 0)
        }

        val save = smallPanelButton("💾 SALVAR") {
            Toast.makeText(this@KovickAccessibilityService, "Perfil salvo para o jogo atual.", Toast.LENGTH_SHORT).show()
        }
        val editor = smallPanelButton("▣ EDITOR") {
            openEditor("visual")
        }

        bottomRow.addView(save, LinearLayout.LayoutParams(0, 46, 1f).apply { marginEnd = 6 })
        bottomRow.addView(editor, LinearLayout.LayoutParams(0, 46, 1f).apply { marginStart = 6 })
        panel.addView(bottomRow)

        val params = WindowManager.LayoutParams(
            430,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.END
        params.x = 24
        params.y = 100

        panel.setOnTouchListener(createDragListener(params))

        try {
            windowManager?.addView(panel, params)
            mapperPanel = panel
            panelParams = params
        } catch (_: Exception) {
            Toast.makeText(this, "Não foi possível abrir o painel Kovick.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addPanelButton(parent: LinearLayout, text: String, color: Int, action: () -> Unit) {
        val button = TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(18, 0, 18, 0)
            background = roundedBackground(color, 14f)
            setOnClickListener { action() }
        }
        parent.addView(button, LinearLayout.LayoutParams(-1, 52).apply { bottomMargin = 8 })
    }

    private fun smallPanelButton(text: String, action: () -> Unit): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 12f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = roundedBackground(Color.rgb(30, 30, 42), 12f)
            setOnClickListener { action() }
        }
    }

    private fun createDragListener(params: WindowManager.LayoutParams): View.OnTouchListener {
        var downX = 0f
        var downY = 0f
        var startX = 0
        var startY = 0

        return View.OnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    startX = params.x
                    startY = params.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = startX - (event.rawX - downX).toInt()
                    params.y = startY + (event.rawY - downY).toInt()
                    try {
                        windowManager?.updateViewLayout(view, params)
                    } catch (_: Exception) {
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun openEditor(mode: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("open_editor", true)
            putExtra("editor_mode", mode)
        }
        startActivity(intent)
    }

    private fun removeMapperPanel() {
        try {
            mapperPanel?.let { windowManager?.removeView(it) }
        } catch (_: Exception) {
        }
        mapperPanel = null
        panelParams = null
    }

    private fun removeKovickButton() {
        try {
            kovickButton?.let { windowManager?.removeView(it) }
        } catch (_: Exception) {
        }
        kovickButton = null
    }

    private fun roundedBackground(color: Int, radius: Float): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
        }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {
        // O aplicativo em primeiro plano será usado posteriormente para aplicar o perfil selecionado.
    }

    override fun onInterrupt() {
        // Serviço interrompido pelo Android.
    }

    override fun onDestroy() {
        removeMapperPanel()
        removeKovickButton()
        super.onDestroy()
    }
}
