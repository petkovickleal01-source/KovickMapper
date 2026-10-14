@file:OptIn(ExperimentalMaterial3Api::class)

package com.kovick.mapper

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class InstalledGame(
    val name: String,
    val packageName: String,
    val icon: Bitmap,
    val isGame: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val openEditor = intent.getBooleanExtra("open_editor", false)
        setContent {
            KovickTheme {
                KovickMapperApp(
                    packageManager = packageManager,
                    openEditorInitially = openEditor,
                    onAccessibility = { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    onAndroidSettings = { startActivity(Intent(Settings.ACTION_SETTINGS)) },
                    onLaunchGame = ::launchGame
                )
            }
        }
    }

    private fun launchGame(packageName: String) {
        getSharedPreferences("kovick", MODE_PRIVATE).edit()
            .putString("selected_package", packageName)
            .apply()
        packageManager.getLaunchIntentForPackage(packageName)?.let { startActivity(it) }
    }
}

@Composable
private fun KovickTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF8B5CF6),
            secondary = Color(0xFF22D3EE),
            background = Color(0xFF07070B),
            surface = Color(0xFF101017),
            surfaceVariant = Color(0xFF191923)
        ),
        content = content
    )
}

@Composable
private fun KovickMapperApp(
    packageManager: PackageManager,
    openEditorInitially: Boolean,
    onAccessibility: () -> Unit,
    onAndroidSettings: () -> Unit,
    onLaunchGame: (String) -> Unit
) {
    var selectedGame by remember { mutableStateOf<InstalledGame?>(null) }
    var showGamePicker by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(if (openEditorInitially) 1 else 0) }
    val games = remember(packageManager) { loadLaunchableApps(packageManager) }

    LaunchedEffect(games) {
        val saved = getSelectedPackage()
        selectedGame = games.firstOrNull { it.packageName == saved }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    if (selectedTab != 0) IconButton({ selectedTab = 0 }) { Icon(Icons.Default.ArrowBack, "Voltar") }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary) {
                            Text("K", Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("KOVICK", fontWeight = FontWeight.Black)
                            Text("MAPPER V2 • LANDSCAPE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                },
                actions = { IconButton({ selectedTab = 2 }) { Icon(Icons.Default.Settings, "Configurações") } }
            )
        }
    ) { padding ->
        when (selectedTab) {
            0 -> HomeScreen(padding, selectedGame, games, { showGamePicker = true }, {
                selectedGame = it
                saveSelectedPackage(it.packageName)
            }, onLaunchGame, { selectedTab = 1 }, onAccessibility)
            1 -> EditorScreen(padding, selectedGame)
            else -> SettingsScreen(padding, onAccessibility, onAndroidSettings)
        }
    }

    if (showGamePicker) GamePickerDialog(games, { showGamePicker = false }) {
        selectedGame = it
        saveSelectedPackage(it.packageName)
        showGamePicker = false
    }
}

private fun ComponentActivity.getSelectedPackage(): String? =
    getSharedPreferences("kovick", MODE_PRIVATE).getString("selected_package", null)

private fun ComponentActivity.saveSelectedPackage(packageName: String) {
    getSharedPreferences("kovick", MODE_PRIVATE).edit().putString("selected_package", packageName).apply()
}

private fun loadLaunchableApps(packageManager: PackageManager): List<InstalledGame> {
    val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
    return packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        .asSequence()
        .filter { it.activityInfo.packageName != "com.kovick.mapper" }
        .mapNotNull { info ->
            val app = info.activityInfo.applicationInfo ?: return@mapNotNull null
            val name = app.loadLabel(packageManager).toString().trim()
            if (name.isBlank()) return@mapNotNull null
            InstalledGame(name, app.packageName, app.loadIcon(packageManager).toBitmapSafe(96), isLikelyGame(app, name))
        }
        .distinctBy { it.packageName }
        .sortedWith(compareByDescending<InstalledGame> { it.isGame }.thenBy { it.name.lowercase() })
        .toList()
}

private fun isLikelyGame(app: ApplicationInfo, name: String): Boolean {
    val text = name.lowercase()
    val keywords = listOf("game", "jogo", "free fire", "roblox", "minecraft", "pubg", "fortnite", "call of duty")
    return app.category == ApplicationInfo.CATEGORY_GAME || keywords.any(text::contains)
}

private fun Drawable.toBitmapSafe(size: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, size, size)
    draw(canvas)
    return bitmap
}

@Composable
private fun HomeScreen(
    padding: PaddingValues,
    selectedGame: InstalledGame?,
    games: List<InstalledGame>,
    onAddGame: () -> Unit,
    onSelectGame: (InstalledGame) -> Unit,
    onLaunchGame: (String) -> Unit,
    onOpenEditor: () -> Unit,
    onAccessibility: () -> Unit
) {
    Row(Modifier.fillMaxSize().padding(padding).padding(22.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        Card(Modifier.weight(1.15f).fillMaxHeight(), shape = RoundedCornerShape(26.dp)) {
            Column(Modifier.padding(24.dp)) {
                Text("KOVICK MAPPER", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Text("Seu perfil de controles, por jogo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(20.dp))
                if (selectedGame == null) EmptyGameState(onAddGame) else {
                    Text("JOGO SELECIONADO", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(selectedGame.icon.asImageBitmap(), selectedGame.name, Modifier.size(72.dp))
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(selectedGame.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(selectedGame.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Button({ onLaunchGame(selectedGame.packageName) }, Modifier.fillMaxWidth().height(56.dp)) {
                        Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text("INICIAR JOGO", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onOpenEditor, Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Build, null); Spacer(Modifier.width(8.dp)); Text("EDITAR MAPA")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onAddGame, Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("TROCAR JOGO")
                    }
                }
                Spacer(Modifier.weight(1f))
                Text("PRIMEIRO USO", fontWeight = FontWeight.Bold)
                Text("Ative a Acessibilidade para preparar o botão flutuante do mapper.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onAccessibility, Modifier.fillMaxWidth()) { Text("ATIVAR KOVICK") }
            }
        }
        Card(Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(26.dp)) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("JOGOS E APPS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text("${games.size}", Modifier.padding(horizontal = 12.dp, vertical = 5.dp), fontWeight = FontWeight.Bold)
                    }
                }
                Text("Aplicativos instalados com inicializador detectado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(games, key = { it.packageName }) { game -> GameListItem(game, selectedGame?.packageName == game.packageName) { onSelectGame(game) } }
                }
            }
        }
    }
}

@Composable
private fun EmptyGameState(onAddGame: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Gamepad, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        Text("Nenhum jogo selecionado", fontWeight = FontWeight.Bold)
        Text("Escolha um aplicativo instalado para criar seu perfil.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Button(onAddGame) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("ADICIONAR JOGO") }
    }
}

@Composable
private fun GameListItem(game: InstalledGame, selected: Boolean, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(15.dp), color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .18f) else MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(game.icon.asImageBitmap(), game.name, Modifier.size(46.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(game.name, fontWeight = FontWeight.SemiBold)
                    if (game.isGame) { Spacer(Modifier.width(7.dp)); Text("JOGO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold) }
                }
                Text(game.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (selected) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun GamePickerDialog(games: List<InstalledGame>, onDismiss: () -> Unit, onSelect: (InstalledGame) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Escolher jogo") }, text = {
        LazyColumn(Modifier.heightIn(max = 430.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(games, key = { it.packageName }) { game -> GameListItem(game, false) { onSelect(game) } }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } })
}

@Composable
private fun EditorScreen(padding: PaddingValues, selectedGame: InstalledGame?) {
    Column(Modifier.fillMaxSize().padding(padding).padding(22.dp)) {
        Text("EDITOR DE MAPA", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text(selectedGame?.let { "Perfil: ${it.name}" } ?: "Selecione um jogo para começar", color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            EditorCard(Icons.Default.Keyboard, "TECLADO", "WASD, atalhos e teclas")
            EditorCard(Icons.Default.Mouse, "MOUSE", "Clique, mira e movimentação")
            EditorCard(Icons.Default.Build, "CONTROLES", "Botões, swipe e ações")
        }
    }
}

@Composable
private fun RowScope.EditorCard(icon: ImageVector, title: String, subtitle: String) {
    Card(Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(20.dp)) {
            Icon(icon, null, Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(14.dp))
            Text(title, fontWeight = FontWeight.Black)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text("PREPARADO PARA O OVERLAY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun SettingsScreen(padding: PaddingValues, onAccessibility: () -> Unit, onAndroidSettings: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
        Text("CONFIGURAÇÕES", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAccessibility) { Text("ATIVAR ACESSIBILIDADE") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onAndroidSettings) { Text("CONFIGURAÇÕES DO ANDROID") }
        Spacer(Modifier.height(20.dp))
        Text("KovickMapper v2", fontWeight = FontWeight.Bold)
        Text("Perfis individuais por pacote, overlay e editor visual são a base da próxima etapa.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
