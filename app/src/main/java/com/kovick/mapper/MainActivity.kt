@file:OptIn(ExperimentalMaterial3Api::class)

package com.kovick.mapper

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class InstalledGame(
    val name: String,
    val packageName: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        setContent {
            KovickTheme {

                KovickMapperApp(
                    packageManager = packageManager,

                    onAccessibility = {
                        startActivity(
                            Intent(
                                Settings.ACTION_ACCESSIBILITY_SETTINGS
                            )
                        )
                    },

                    onAndroidSettings = {
                        startActivity(
                            Intent(Settings.ACTION_SETTINGS)
                        )
                    },

                    onLaunchGame = {
                        launchGame(it)
                    }
                )
            }
        }
    }

    private fun launchGame(packageName: String) {

        getSharedPreferences(
            "kovick",
            MODE_PRIVATE
        )
            .edit()
            .putString(
                "selected_package",
                packageName
            )
            .apply()

        val launchIntent =
            packageManager.getLaunchIntentForPackage(
                packageName
            )

        if (launchIntent != null) {
            startActivity(launchIntent)
        }
    }
}

@Composable
private fun KovickTheme(
    content: @Composable () -> Unit
) {

    MaterialTheme(

        colorScheme = darkColorScheme(

            primary =
                Color(0xFF8B5CF6),

            secondary =
                Color(0xFF22D3EE),

            background =
                Color(0xFF07070B),

            surface =
                Color(0xFF101017),

            surfaceVariant =
                Color(0xFF191923)
        ),

        content = content
    )
}

@Composable
private fun KovickMapperApp(
    packageManager: PackageManager,
    onAccessibility: () -> Unit,
    onAndroidSettings: () -> Unit,
    onLaunchGame: (String) -> Unit
) {

    var selectedGame by remember {
        mutableStateOf<InstalledGame?>(null)
    }

    var showGamePicker by remember {
        mutableStateOf(false)
    }

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    val games = remember {
        loadLaunchableApps(packageManager)
    }

    Scaffold(

        topBar = {

            CenterAlignedTopAppBar(

                title = {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            "KOVICK",
                            fontWeight =
                                FontWeight.Black
                        )

                        Text(
                            "MAPPER • LANDSCAPE",

                            style =
                                MaterialTheme.typography
                                    .labelSmall,

                            color =
                                MaterialTheme.colorScheme
                                    .secondary
                        )
                    }
                },

                actions = {

                    IconButton(
                        onClick = {
                            selectedTab = 2
                        }
                    ) {

                        Icon(
                            Icons.Default.Settings,
                            contentDescription =
                                "Configurações"
                        )
                    }
                }
            )
        }

    ) { padding ->

        when (selectedTab) {

            0 -> HomeScreen(

                padding = padding,

                selectedGame =
                    selectedGame,

                games =
                    games,

                onAddGame = {
                    showGamePicker = true
                },

                onSelectGame = {
                    selectedGame = it
                },

                onLaunchGame = {
                    onLaunchGame(it)
                },

                onAccessibility =
                    onAccessibility
            )

            1 -> EditorScreen(
                padding = padding
            )

            else -> SettingsScreen(
                padding = padding,

                onAccessibility =
                    onAccessibility,

                onAndroidSettings =
                    onAndroidSettings
            )
        }
    }

    if (showGamePicker) {

        GamePickerDialog(

            games = games,

            onDismiss = {
                showGamePicker = false
            },

            onSelect = {

                selectedGame = it

                showGamePicker = false
            }
        )
    }
}

private fun loadLaunchableApps(
    packageManager: PackageManager
): List<InstalledGame> {

    val intent =
        Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

    return packageManager
        .queryIntentActivities(
            intent,
            PackageManager.MATCH_ALL
        )
        .asSequence()

        .filter {
            it.activityInfo.packageName !=
                    "com.kovick.mapper"
        }

        .map {

            InstalledGame(

                name =
                    it.loadLabel(
                        packageManager
                    ).toString(),

                packageName =
                    it.activityInfo.packageName
            )
        }

        .distinctBy {
            it.packageName
        }

        .sortedBy {
            it.name.lowercase()
        }

        .toList()
}

@Composable
private fun HomeScreen(
    padding: PaddingValues,
    selectedGame: InstalledGame?,
    games: List<InstalledGame>,
    onAddGame: () -> Unit,
    onSelectGame: (InstalledGame) -> Unit,
    onLaunchGame: (String) -> Unit,
    onAccessibility: () -> Unit
) {

    Row(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(22.dp),

        horizontalArrangement =
            Arrangement.spacedBy(20.dp)
    ) {

        Card(

            modifier =
                Modifier
                    .weight(1.15f)
                    .fillMaxHeight(),

            shape =
                RoundedCornerShape(28.dp)
        ) {

            Column(
                modifier =
                    Modifier.padding(24.dp)
            ) {

                Text(
                    "KOVICK MAPPER",

                    style =
                        MaterialTheme.typography
                            .headlineMedium,

                    fontWeight =
                        FontWeight.Black
                )

                Text(
                    "Mapeamento em tela deitada",

                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    Modifier.height(24.dp)
                )

                if (selectedGame == null) {

                    EmptyGameState(
                        onAddGame
                    )

                } else {

                    Text(
                        "JOGO SELECIONADO",

                        color =
                            MaterialTheme.colorScheme
                                .secondary,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        Modifier.height(6.dp)
                    )

                    Text(
                        selectedGame.name,

                        style =
                            MaterialTheme.typography
                                .headlineSmall,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        selectedGame.packageName,

                        style =
                            MaterialTheme.typography
                                .bodySmall,

                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )

                    Spacer(
                        Modifier.height(20.dp)
                    )

                    Button(

                        onClick = {
                            onLaunchGame(
                                selectedGame.packageName
                            )
                        },

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                    ) {

                        Icon(
                            Icons.Default.PlayArrow,
                            null
                        )

                        Spacer(
                            Modifier.width(8.dp)
                        )

                        Text(
                            "INICIAR JOGO + KOVICK",
                            fontWeight =
                                FontWeight.Bold
                        )
                    }

                    Spacer(
                        Modifier.height(10.dp)
                    )

                    OutlinedButton(

                        onClick =
                            onAddGame,

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Icon(
                            Icons.Default.Add,
                            null
                        )

                        Spacer(
                            Modifier.width(8.dp)
                        )

                        Text(
                            "TROCAR JOGO"
                        )
                    }
                }

                Spacer(
                    Modifier.weight(1f)
                )

                Text(
                    "PRIMEIRO USO",
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    "Ative a Acessibilidade do Kovick para o botão flutuante aparecer sobre o jogo.",

                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    Modifier.height(8.dp)
                )

                OutlinedButton(

                    onClick =
                        onAccessibility,

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        "ATIVAR KOVICK"
                    )
                }
            }
        }

        Card(

            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),

            shape =
                RoundedCornerShape(28.dp)
        ) {

            Column(
                modifier =
                    Modifier.padding(24.dp)
            ) {

                Text(
                    "APLICATIVOS",

                    style =
                        MaterialTheme.typography
                            .titleLarge,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    "Escolha o jogo que receberá o perfil.",

                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    Modifier.height(12.dp)
                )

                LazyColumn(

                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    items(games) { game ->

                        GameListItem(

                            game = game,

                            selected =
                                selectedGame
                                    ?.packageName ==
                                    game.packageName,

                            onClick = {
                                onSelectGame(game)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyGameState(
    onAddGame: () -> Unit
) {

    Column(

        modifier =
            Modifier.fillMaxWidth(),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Icon(

            Icons.Default.Gamepad,

            contentDescription = null,

            modifier =
                Modifier.size(64.dp),

            tint =
                MaterialTheme.colorScheme
                    .primary
        )

        Spacer(
            Modifier.height(14.dp)
        )

        Text(
            "Nenhum jogo selecionado",
            fontWeight =
                FontWeight.Bold
        )

        Text(
            "Escolha um aplicativo instalado para criar seu perfil.",

            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )

        Spacer(
            Modifier.height(18.dp)
        )

        Button(
            onClick =
                onAddGame
        ) {

            Icon(
                Icons.Default.Add,
                null
            )

            Spacer(
                Modifier.width(8.dp)
            )

            Text(
                "ADICIONAR JOGO"
            )
        }
    }
}

@Composable
private fun GameListItem(
    game: InstalledGame,
    selected: Boolean,
    onClick: () -> Unit
) {

    Surface(

        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick
                ),

        shape =
            RoundedCornerShape(16.dp),

        color =
            if (selected)
                MaterialTheme.colorScheme
                    .primary
                    .copy(alpha = 0.18f)
            else
                MaterialTheme.colorScheme
                    .surfaceVariant
    ) {

        Row(

            modifier =
                Modifier.padding(14.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(

                Icons.Default.Gamepad,

                contentDescription = null,

                tint =
                    MaterialTheme.colorScheme
                        .primary
            )

            Spacer(
                Modifier.width(12.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    game.name,
                    fontWeight =
                        FontWeight.SemiBold
                )

                Text(
                    game.packageName,

                    style =
                        MaterialTheme.typography
                            .bodySmall,

                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }

            if (selected) {

                Text(
                    "✓",

                    color =
                        MaterialTheme.colorScheme
                            .secondary,

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GamePickerDialog(
    games: List<InstalledGame>,
    onDismiss: () -> Unit,
    onSelect: (InstalledGame) -> Unit
) {

    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {
            Text("Escolher jogo")
        },

        text = {

            LazyColumn(

                modifier =
                    Modifier.heightIn(
                        max = 420.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(6.dp)
            ) {

                items(games) { game ->

                    GameListItem(

                        game = game,

                        selected = false,

                        onClick = {
                            onSelect(game)
                        }
                    )
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    "Fechar"
                )
            }
        }
    )
}

@Composable
private fun EditorScreen(
    padding: PaddingValues
) {

    Row(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),

        horizontalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {

        EditorCard(
            Icons.Default.Keyboard,
            "TECLADO",
            "WASD, atalhos e teclas"
        )

        EditorCard(
            Icons.Default.Mouse,
            "MOUSE",
            "Clique, mira e movimentação"
        )

        EditorCard(
            Icons.Default.Build,
            "CONTROLES",
            "Botões, swipe e macros"
        )
    }
}

@Composable
private fun RowScope.EditorCard(
    icon: ImageVector,
    title: String,
    subtitle: String
) {

    Card(

        modifier =
            Modifier
                .weight(1f)
                .fillMaxHeight(),

        shape =
            RoundedCornerShape(24.dp)
    ) {

        Column(
            modifier =
                Modifier.padding(22.dp)
        ) {

            Icon(

                icon,

                contentDescription = null,

                modifier =
                    Modifier.size(38.dp),

                tint =
                    MaterialTheme.colorScheme
                        .primary
            )

            Spacer(
                Modifier.height(14.dp)
            )

            Text(
                title,

                style =
                    MaterialTheme.typography
                        .titleLarge,

                fontWeight =
                    FontWeight.Bold
            )

            Text(
                subtitle,

                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsScreen(
    padding: PaddingValues,
    onAccessibility: () -> Unit,
    onAndroidSettings: () -> Unit
) {

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        Text(
            "CONFIGURAÇÕES",

            style =
                MaterialTheme.typography
                    .headlineSmall,

            fontWeight =
                FontWeight.Bold
        )

        OutlinedButton(

            onClick =
                onAccessibility,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                "ABRIR ACESSIBILIDADE"
            )
        }

        OutlinedButton(

            onClick =
                onAndroidSettings,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                "CONFIGURAÇÕES DO ANDROID"
            )
        }

        Text(
            "Kovick Mapper • 0.3.0",

            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )
    }
}
