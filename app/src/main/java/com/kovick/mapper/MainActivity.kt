package com.kovick.mapper

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.hypot
import kotlin.math.roundToInt

private val KovickRed = Color(0xFFD50000)
private val KovickDark = Color(0xFF08080B)
private val KovickPanel = Color(0xFF121218)

class MainActivity : ComponentActivity() {
    private var editorOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        showHome()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (editorOpen) {
            editorOpen = false
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            showHome()
        } else super.onBackPressed()
    }

    private fun showHome() { setContent { HomeScreen() } }

    private fun openEditor(profile: String) {
        KovickProfileStore.setActiveProfile(this, profile)
        editorOpen = true
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContent { MapperEditorScreen(profile) }
    }

    @Composable
    private fun HomeScreen() {
        var profiles by remember { mutableStateOf(KovickProfileStore.profiles(this)) }
        var showAdd by remember { mutableStateOf(false) }
        MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = KovickRed)) {
            Column(Modifier.fillMaxSize().padding(18.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("KOVICK", color = KovickRed, style = MaterialTheme.typography.headlineMedium)
                        Text("Mouse & Keyboard Mapper", style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(onClick = { showSettings() }) { Icon(Icons.Default.Settings, "Configurações", tint = KovickRed) }
                }
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Meus jogos", style = MaterialTheme.typography.titleLarge)
                    OutlinedButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, null); Text("  Jogo") }
                }
                Spacer(Modifier.height(10.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(profiles) { profile ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column { Text(profile, style = MaterialTheme.typography.titleMedium); Text("Perfil de mapeamento", style = MaterialTheme.typography.bodySmall) }
                                Button(onClick = { openEditor(profile) }) { Text("EDITAR") }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Como funciona", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("Abra um jogo, entre no editor em paisagem e arraste os pontos para os locais desejados.")
                        Text("Selecione um ponto e pressione uma tecla para definir o comando.")
                    }
                }
            }
        }
        if (showAdd) {
            AddGameDialog(onDismiss = { showAdd = false }, onAdd = { name ->
                if (KovickProfileStore.addProfile(this, name)) {
                    profiles = KovickProfileStore.profiles(this)
                    showAdd = false
                }
            })
        }
    }

    @Composable
    private fun AddGameDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
        var name by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = onDismiss, title = { Text("Adicionar jogo") }, text = {
            OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, label = { Text("Nome do jogo") })
        }, confirmButton = { TextButton(onClick = { onAdd(name) }) { Text("Adicionar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
    }

    @Composable
    private fun MapperEditorScreen(profile: String) {
        val points = remember {
            mutableStateMapOf<Int, Offset>().apply {
                val defaults = mapOf(
                    KeyEvent.KEYCODE_W to Offset(.18f, .72f),
                    KeyEvent.KEYCODE_A to Offset(.12f, .82f),
                    KeyEvent.KEYCODE_S to Offset(.18f, .92f),
                    KeyEvent.KEYCODE_D to Offset(.24f, .82f),
                    KeyEvent.KEYCODE_SPACE to Offset(.86f, .88f)
                )
                defaults.forEach { (key, fallback) ->
                    val saved = KovickProfileStore.point(this@MainActivity, key)
                    this[key] = if (saved != null) Offset(saved.first, saved.second) else fallback
                }
            }
        }
        var selectedKey by remember { mutableStateOf<Int?>(KeyEvent.KEYCODE_W) }
        val focusRequester = remember { FocusRequester() }

        MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = KovickRed)) {
            Row(Modifier.fillMaxSize().background(KovickDark).focusRequester(focusRequester).onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown || selectedKey == null) return@onPreviewKeyEvent false
                val newKey = event.nativeKeyEvent.keyCode
                if (newKey == KeyEvent.KEYCODE_ESCAPE) return@onPreviewKeyEvent false
                val old = selectedKey!!
                if (newKey != old) {
                    val position = points.remove(old) ?: Offset(.5f, .5f)
                    points[newKey] = position
                    selectedKey = newKey
                    KovickProfileStore.savePoint(this@MainActivity, newKey, position.x, position.y)
                }
                true
            }) {
                Box(Modifier.weight(1f).fillMaxHeight().padding(12.dp)) { MappingSurface(points, selectedKey) { key -> selectedKey = key } }
                Column(Modifier.width(250.dp).fillMaxHeight().background(KovickPanel).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { editorOpen = false; requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT; showHome() }) { Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White) }
                        Text(profile, color = Color.White, style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("MAPEAMENTO", color = KovickRed, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(if (selectedKey == null) "Selecione um ponto" else "Ponto selecionado: ${keyName(selectedKey!!)}", color = Color.White)
                    Spacer(Modifier.height(12.dp))
                    Text("Arraste os pontos na tela. Para trocar a tecla, selecione um ponto e pressione a nova tecla.", color = Color.LightGray)
                    Spacer(Modifier.height(18.dp))
                    Button(onClick = { focusRequester.requestFocus() }, modifier = Modifier.fillMaxWidth()) { Text("CAPTURAR TECLA") }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = { selectedKey = null }, modifier = Modifier.fillMaxWidth()) { Text("Desselecionar") }
                    Spacer(Modifier.height(20.dp))
                    Text("Modo de mira", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text("Preparado para a próxima camada do mapper: arrastar mouse → câmera.", color = Color.LightGray)
                }
            }
        }
    }

    @Composable
    private fun MappingSurface(points: Map<Int, Offset>, selectedKey: Int?, onSelect: (Int) -> Unit) {
        var sizeWidth by remember { mutableStateOf(1) }
        var sizeHeight by remember { mutableStateOf(1) }
        Box(Modifier.fillMaxSize().background(Color(0xFF050507), RoundedCornerShape(18.dp)).pointerInput(points, selectedKey) {
            detectDragGestures(onDragStart = { start ->
                val nearest = nearestPoint(points, start, sizeWidth.toFloat(), sizeHeight.toFloat())
                if (nearest != null) onSelect(nearest)
            }, onDrag = { change, dragAmount ->
                change.consume()
                val key = selectedKey ?: return@detectDragGestures
                val current = points[key] ?: return@detectDragGestures
                val nx = (current.x + dragAmount.x / sizeWidth).coerceIn(.03f, .97f)
                val ny = (current.y + dragAmount.y / sizeHeight).coerceIn(.03f, .97f)
                val next = Offset(nx, ny)
                if (points is MutableMap<Int, Offset>) points[key] = next
                KovickProfileStore.savePoint(this@MainActivity, key, nx, ny)
            })
        }) {
            Canvas(Modifier.fillMaxSize()) {
                sizeWidth = size.width.roundToInt().coerceAtLeast(1)
                sizeHeight = size.height.roundToInt().coerceAtLeast(1)
                val stepX = size.width / 10f
                val stepY = size.height / 6f
                for (i in 1 until 10) drawLine(Color(0x22111111), Offset(stepX * i, 0f), Offset(stepX * i, size.height), 1f)
                for (i in 1 until 6) drawLine(Color(0x22111111), Offset(0f, stepY * i), Offset(size.width, stepY * i), 1f)
                points.forEach { (key, position) ->
                    val center = Offset(position.x * size.width, position.y * size.height)
                    val selected = key == selectedKey
                    drawCircle(if (selected) KovickRed else Color(0xCC25252D), if (selected) 34f else 29f, center)
                    drawCircle(Color.White, 2.5f, center)
                }
            }
        }
    }

    private fun nearestPoint(points: Map<Int, Offset>, touch: Offset, width: Float, height: Float): Int? {
        var result: Int? = null
        var best = 70f
        points.forEach { (key, pos) ->
            val distance = hypot(pos.x * width - touch.x, pos.y * height - touch.y)
            if (distance < best) { best = distance; result = key }
        }
        return result
    }

    private fun keyName(keyCode: Int): String = when (keyCode) {
        KeyEvent.KEYCODE_W -> "W"
        KeyEvent.KEYCODE_A -> "A"
        KeyEvent.KEYCODE_S -> "S"
        KeyEvent.KEYCODE_D -> "D"
        KeyEvent.KEYCODE_SPACE -> "SPACE"
        else -> "KEY $keyCode"
    }

    private fun showSettings() {
        setContent {
            MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = KovickRed)) {
                Column(Modifier.fillMaxSize().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showHome() }) { Icon(Icons.Default.ArrowBack, "Voltar", tint = KovickRed) }
                        Text("Configurações", style = MaterialTheme.typography.headlineMedium, color = KovickRed)
                    }
                    Spacer(Modifier.height(18.dp))
                    Button(onClick = { openOverlaySettings() }, Modifier.fillMaxWidth()) { Text("Sobreposição de tela") }
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { openUsageAccessSettings() }, Modifier.fillMaxWidth()) { Text("Detectar jogo aberto") }
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { openAccessibilitySettings() }, Modifier.fillMaxWidth()) { Text("Ativar serviço Kovick") }
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { requestBatteryOptimization() }, Modifier.fillMaxWidth()) { Text("Bateria sem restrição") }
                }
            }
        }
    }

    private fun openOverlaySettings() { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) }
    private fun openUsageAccessSettings() { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
    private fun openAccessibilitySettings() { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
    private fun requestBatteryOptimization() {
        try { startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName"))) }
        catch (_: Exception) { startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
    }
}
