package com.kovick.mapper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.provider.Settings
import android.view.Gravity
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlin.math.max
import kotlin.math.min

class KovickAccessibilityService : AccessibilityService() {
    private var wm: WindowManager? = null
    private var kovickButton: View? = null
    private var panel: View? = null
    private var aimOverlay: AimOverlay? = null
    private var mappingOverlay: MappingOverlay? = null
    private var mappingArmed = false
    private var pendingX = -1f
    private var pendingY = -1f
    private var mapperEnabled = true
    private var aimEnabled = false

    private val prefs by lazy { getSharedPreferences("kovick_profiles", MODE_PRIVATE) }

    override fun onServiceConnected() {
        super.onServiceConnected()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        mapperEnabled = prefs.getBoolean("enabled", true)
        showKovickButton()
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN || event.repeatCount > 0) return false

        if (mappingArmed && pendingX >= 0f) {
            prefs.edit().putString("map_${event.keyCode}", "$pendingX,$pendingY").apply()
            mappingArmed = false
            pendingX = -1f
            pendingY = -1f
            removeMappingOverlay()
            toast("Tecla ${KeyEvent.keyCodeToString(event.keyCode)} mapeada")
            return true
        }

        if (!mapperEnabled) return false
        val raw = prefs.getString("map_${event.keyCode}", null) ?: return false
        val parts = raw.split(',')
        if (parts.size != 2) return false
        val x = parts[0].toFloatOrNull() ?: return false
        val y = parts[1].toFloatOrNull() ?: return false
        tap(x, y)
        return true
    }

    private fun tap(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 45))
            .build()
        dispatchGesture(gesture, null, null)
    }

    private fun showKovickButton() {
        if (kovickButton != null) return
        if (!Settings.canDrawOverlays(this)) {
            toast("Ative a sobreposição do Kovick para usar o painel")
            return
        }
        val button = TextView(this).apply {
            text = "K"
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = roundedBackground(Color.rgb(139, 92, 246), 18f)
            elevation = 12f
            setOnClickListener { togglePanel() }
            setOnLongClickListener { removePanel(); removeKovickButton(); true }
        }
        val p = WindowManager.LayoutParams(64, 64, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT)
        p.gravity = Gravity.TOP or Gravity.END
        p.x = 24; p.y = 120
        runCatching { wm?.addView(button, p); kovickButton = button }
            .onFailure { toast("Não foi possível criar o botão Kovick") }
    }

    private fun togglePanel() { if (panel == null) showPanel() else removePanel() }

    private fun showPanel() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 16, 18, 16)
            background = roundedBackground(Color.argb(248, 12, 12, 20), 24f)
            elevation = 18f
        }
        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val title = TextView(this).apply {
            text = "KOVICK  •  ALPHA"
            textSize = 16f; setTextColor(Color.WHITE)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        val close = TextView(this).apply {
            text = "✕"; textSize = 18f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
            setPadding(18, 6, 6, 6); setOnClickListener { removePanel() }
        }
        header.addView(title, LinearLayout.LayoutParams(0, 48, 1f)); header.addView(close, LinearLayout.LayoutParams(48, 48)); root.addView(header)
        val status = TextView(this).apply {
            text = "Teclas → toques reais  •  mouse → modo mira"
            textSize = 12f; setTextColor(Color.rgb(175,175,190)); setPadding(2,0,2,12)
        }
        root.addView(status)

        addButton(root, if (mapperEnabled) "●  MAPEADOR ATIVO" else "○  MAPEADOR DESATIVADO", if (mapperEnabled) Color.rgb(32,90,62) else Color.rgb(55,55,68)) {
            mapperEnabled = !mapperEnabled
            prefs.edit().putBoolean("enabled", mapperEnabled).apply()
            removePanel(); showPanel()
        }
        addButton(root, "⌨  ADICIONAR / MAPEAR TECLA", Color.rgb(42,42,55)) { armMapping() }
        addButton(root, if (aimEnabled) "🎯  MODO AIM: ATIVO" else "🎯  MODO AIM: DESATIVADO", if (aimEnabled) Color.rgb(76,48,120) else Color.rgb(42,42,55)) {
            aimEnabled = !aimEnabled
            if (aimEnabled) showAimOverlay() else removeAimOverlay()
            removePanel(); showPanel()
        }
        addButton(root, "⚙  SENSIBILIDADE DA MIRA", Color.rgb(42,42,55)) { adjustSensitivity() }
        addButton(root, "🧹  LIMPAR MAPAS", Color.rgb(70,38,45)) {
            prefs.edit().clear().putBoolean("enabled", mapperEnabled).apply()
            toast("Mapeamentos apagados"); removePanel(); showPanel()
        }

        val bottom = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(0,10,0,0) }
        val editor = smallButton("▣ EDITOR") { openEditor("visual") }
        val save = smallButton("💾 SALVAR") { toast("Perfil salvo neste dispositivo") }
        bottom.addView(editor, LinearLayout.LayoutParams(0,46,1f).apply { marginEnd=6 })
        bottom.addView(save, LinearLayout.LayoutParams(0,46,1f).apply { marginStart=6 })
        root.addView(bottom)

        val p = WindowManager.LayoutParams(460, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT)
        p.gravity = Gravity.TOP or Gravity.END; p.x=24; p.y=100
        root.setOnTouchListener(createDragListener(p))
        runCatching { wm?.addView(root,p); panel=root }.onFailure { toast("Não foi possível abrir o painel") }
    }

    private fun armMapping() {
        removePanel()
        mappingArmed = true
        pendingX = -1f; pendingY = -1f
        val overlay = MappingOverlay()
        val p = WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT)
        p.gravity = Gravity.TOP or Gravity.START
        runCatching { wm?.addView(overlay,p); mappingOverlay=overlay }
        toast("Toque no ponto do jogo que a tecla deverá controlar")
    }

    private fun showAimOverlay() {
        if (aimOverlay != null) return
        val overlay = AimOverlay()
        val p = WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT)
        p.gravity = Gravity.TOP or Gravity.START
        runCatching { wm?.addView(overlay,p); aimOverlay=overlay }
    }

    private fun removeAimOverlay() { aimOverlay?.let { runCatching { wm?.removeView(it) } }; aimOverlay=null }
    private fun removeMappingOverlay() { mappingOverlay?.let { runCatching { wm?.removeView(it) } }; mappingOverlay=null }

    private fun adjustSensitivity() {
        val current = prefs.getFloat("sensitivity", 1.0f)
        val next = if (current >= 2.0f) 0.5f else current + 0.25f
        prefs.edit().putFloat("sensitivity", next).apply()
        toast("Sensibilidade: ${"%.2f".format(next)}x")
    }

    private fun addButton(parent: LinearLayout, text: String, color: Int, action: () -> Unit) {
        val b=TextView(this).apply { this.text=text; textSize=13f; setTextColor(Color.WHITE); gravity=Gravity.CENTER_VERTICAL; setPadding(18,0,18,0); background=roundedBackground(color,14f); setOnClickListener{action()} }
        parent.addView(b,LinearLayout.LayoutParams(-1,52).apply{bottomMargin=8})
    }
    private fun smallButton(text:String, action:()->Unit)=TextView(this).apply{this.text=text;textSize=12f;setTextColor(Color.WHITE);gravity=Gravity.CENTER;background=roundedBackground(Color.rgb(30,30,42),12f);setOnClickListener{action()}}

    private fun createDragListener(params: WindowManager.LayoutParams): View.OnTouchListener {
        var dx=0f; var dy=0f; var sx=0; var sy=0
        return View.OnTouchListener { v,e ->
            when(e.actionMasked){
                MotionEvent.ACTION_DOWN->{dx=e.rawX;dy=e.rawY;sx=params.x;sy=params.y;true}
                MotionEvent.ACTION_MOVE->{params.x=sx-(e.rawX-dx).toInt();params.y=sy+(e.rawY-dy).toInt();runCatching{wm?.updateViewLayout(v,params)};true}
                else->false
            }
        }
    }

    private fun openEditor(mode:String){
        startActivity(Intent(this,MainActivity::class.java).apply{addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP);putExtra("open_editor",true);putExtra("editor_mode",mode)})
    }
    private fun removePanel(){panel?.let{runCatching{wm?.removeView(it)}};panel=null}
    private fun removeKovickButton(){kovickButton?.let{runCatching{wm?.removeView(it)}};kovickButton=null}
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_SHORT).show()
    private fun roundedBackground(c:Int,r:Float)=GradientDrawable().apply{setColor(c);cornerRadius=r}

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy(){removePanel();removeAimOverlay();removeMappingOverlay();removeKovickButton();super.onDestroy()}

    private inner class MappingOverlay: TextView(this@KovickAccessibilityService){
        init{ text="TOQUE NO LOCAL\n\nDepois pressione a tecla desejada"; textSize=18f; setTextColor(Color.WHITE);gravity=Gravity.CENTER;setBackgroundColor(Color.argb(70,139,92,246));setOnTouchListener{_,e->if(e.actionMasked==MotionEvent.ACTION_UP){pendingX=e.x;pendingY=e.y;setText("POSIÇÃO DEFINIDA\n\nAgora pressione a tecla desejada");toast("Agora pressione a tecla do teclado");true}else true}}
    }

    private inner class AimOverlay: View(this@KovickAccessibilityService){
        private var lastX=0f; private var lastY=0f
        override fun onGenericMotionEvent(e:MotionEvent):Boolean{
            if(!aimEnabled || e.source and InputDevice.SOURCE_MOUSE == 0) return super.onGenericMotionEvent(e)
            if(e.actionMasked==MotionEvent.ACTION_HOVER_MOVE){
                val dx=if(e.getAxisValue(MotionEvent.AXIS_RELATIVE_X)!=0f)e.getAxisValue(MotionEvent.AXIS_RELATIVE_X) else e.x-lastX
                val dy=if(e.getAxisValue(MotionEvent.AXIS_RELATIVE_Y)!=0f)e.getAxisValue(MotionEvent.AXIS_RELATIVE_Y) else e.y-lastY
                lastX=e.x;lastY=e.y
                moveCamera(dx,dy)
                return true
            }
            return super.onGenericMotionEvent(e)
        }
        override fun onTouchEvent(e:MotionEvent):Boolean = false
        private fun moveCamera(dx:Float,dy:Float){
            val s=prefs.getFloat("sensitivity",1.0f)
            val cx=resources.displayMetrics.widthPixels/2f; val cy=resources.displayMetrics.heightPixels/2f
            val ex=cx+max(-180f,min(180f,dx*s*3f)); val ey=cy+max(-120f,min(120f,dy*s*3f))
            val path=Path().apply{moveTo(cx,cy);lineTo(ex,ey)}
            val g=GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path,0,16)).build()
            dispatchGesture(g,null,null)
        }
    }
}
