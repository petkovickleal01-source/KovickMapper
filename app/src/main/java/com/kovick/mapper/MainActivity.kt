@file:OptIn(ExperimentalMaterial3Api::class)

package com.kovick.mapper

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class KovickGame(
    val name: String,
    val subtitle: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KovickTheme {
                KovickMapperApp(
                    onAccessibility = {
                        startActivity(
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        )
                    },
                    onAndroidSettings = {
                        startActivity(
                            Intent(Settings.ACTION_SETTINGS)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun KovickTheme(content: @Composable () -> Unit) {

    val colors = darkColorScheme(
        primary = Color(0xFF8B5CF6),
        secondary = Color(0xFF22D3EE),
        background = Color(0xFF07070B),
        surface = Color(0xFF101017),
        surfaceVariant = Color(0xFF191923)
    )

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

@Composable
fun KovickMapperApp(
    onAccessibility: () -> Unit,
    onAndroidSettings: () -> Unit
) {

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    var serviceEnabled by remember {
        mutableStateOf(false)
    }

    val games = remember {
        mutableStateListOf(
            KovickGame(
                "Free Fire",
                "Perfil principal"
            ),
            KovickGame(
                "Roblox",
                "Perfil padrão"
            )
        )
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
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            "MAPPER",
                            style =
                                MaterialTheme.typography.labelSmall,
                            color =
                                MaterialTheme.colorScheme.secondary
                        )
                    }
                },

                actions = {

                    IconButton(
                        onClick = {
                            selectedTab = 3
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
        },

        bottomBar = {

            NavigationBar {

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                    },
                    icon = {
                        Icon(
                            Icons.Default.SportsEsports,
                            null
                        )
                    },
                    label = {
                        Text("Início")
                    }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                    },
                    icon = {
                        Icon(
                            Icons.Default.Keyboard,
                            null
                        )
                    },
                    label = {
                        Text("Mapper")
                    }
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                    },
                    icon = {
                        Icon(
                            Icons.Default.Build,
                            null
                        )
                    },
                    label = {
                        Text("Ferramentas")
                    }
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                    },
                    icon = {
                        Icon(
                            Icons.Default.Settings,
                            null
                        )
                    },
                    label = {
                        Text("Ajustes")
                    }
                )
            }
        }

    ) { padding ->

        when (selectedTab) {

            0 -> HomeScreen(
                padding = padding,
                serviceEnabled = serviceEnabled,
                games = games,
                onAccessibility = onAccessibility,
                onAndroidSettings = onAndroidSettings,
                onToggleService = {
                    serviceEnabled = !serviceEnabled
                }
            )

            1 -> MapperScreen(
                padding = padding
            )

            2 -> ToolsScreen(
                padding = padding
            )

            else -> SettingsScreen(
                padding = padding,
                onAccessibility = onAccessibility,
                onAndroidSettings = onAndroidSettings
            )
        }
    }
}

@Composable
fun HomeScreen(
    padding: PaddingValues,
    serviceEnabled: Boolean,
    games: List<KovickGame>,
    onAccessibility: () -> Unit,
    onAndroidSettings: () -> Unit,
    onToggleService: () -> Unit
) {

    LazyColumn(

        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 18.dp),

        verticalArrangement =
            Arrangement.spacedBy(16.dp),

        contentPadding =
            PaddingValues(vertical = 18.dp)
    ) {

        item {

            StatusCard(
                enabled = serviceEnabled,
                onToggle = onToggleService
            )
        }

        item {

            ActivationCard(
                onAccessibility =
                    onAccessibility,

                onAndroidSettings =
                    onAndroidSettings
            )
        }

        item {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        "Meus jogos",
                        style =
                            MaterialTheme.typography.titleLarge,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        "Perfis prontos para jogar",
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }

                FilledTonalIconButton(
                    onClick = {}
                ) {

                    Icon(
                        Icons.Default.Add,
                        contentDescription =
                            "Adicionar jogo"
                    )
                }
            }
        }

        item {

            LazyRow(
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                items(games) { game ->

                    GameCard(game)
                }
            }
        }

        item {

            QuickToolsCard()
        }
    }
}

@Composable
fun StatusCard(
    enabled: Boolean,
    onToggle: () -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(24.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme
                        .surfaceVariant
            )
    ) {

        Column(
            modifier =
                Modifier.padding(20.dp)
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier =
                        Modifier
                            .size(12.dp)
                            .clip(
                                RoundedCornerShape(50)
                            )
                            .background(
                                if (enabled)
                                    Color(0xFF22C55E)
                                else
                                    Color(0xFFEF4444)
                            )
                )

                Spacer(
                    Modifier.width(10.dp)
                )

                Text(
                    if (enabled)
                        "Kovick está ativo"
                    else
                        "Kovick está desativado",

                    style =
                        MaterialTheme.typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                Modifier.height(8.dp)
            )

            Text(
                if (enabled)
                    "Entrada e controles preparados."
                else
                    "Ative o serviço para começar.",

                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                Modifier.height(16.dp)
            )

            Button(
                onClick = onToggle,
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    if (enabled)
                        "Desativar"
                    else
                        "Ativar Kovick"
                )
            }
        }
    }
}

@Composable
fun ActivationCard(
    onAccessibility: () -> Unit,
    onAndroidSettings: () -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(24.dp)
    ) {

        Column(
            modifier =
                Modifier.padding(20.dp)
        ) {

            Text(
                "Ativação",
                style =
                    MaterialTheme.typography.titleLarge,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                Modifier.height(4.dp)
            )

            Text(
                "Prepare o Kovick no Android.",
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                Modifier.height(14.dp)
            )

            ActivationRow(
                icon =
                    Icons.Default.Wifi,

                title =
                    "Depuração sem fio",

                subtitle =
                    "Preparar conexão ADB / Wi-Fi",

                onClick =
                    onAndroidSettings
            )

            ActivationRow(
                icon =
                    Icons.Default.Build,

                title =
                    "Acessibilidade",

                subtitle =
                    "Abrir o serviço do Kovick",

                onClick =
                    onAccessibility
            )
        }
    }
}

@Composable
fun ActivationRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(18.dp)
                )
                .clickable(
                    onClick = onClick
                )
                .padding(14.dp),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Surface(
            shape =
                RoundedCornerShape(14.dp),

            color =
                MaterialTheme.colorScheme
                    .primary
                    .copy(alpha = 0.15f)
        ) {

            Icon(
                icon,
                contentDescription = null,
                modifier =
                    Modifier.padding(12.dp),

                tint =
                    MaterialTheme.colorScheme.primary
            )
        }

        Spacer(
            Modifier.width(14.dp)
        )

        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            Text(
                title,
                fontWeight =
                    FontWeight.SemiBold
            )

            Text(
                subtitle,
                style =
                    MaterialTheme.typography.bodySmall,

                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
fun GameCard(
    game: KovickGame
) {

    Card(
        modifier =
            Modifier.width(210.dp),

        shape =
            RoundedCornerShape(22.dp)
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Surface(
                modifier =
                    Modifier.size(54.dp),

                shape =
                    RoundedCornerShape(16.dp),

                color =
                    MaterialTheme.colorScheme
                        .primary
                        .copy(alpha = 0.18f)
            ) {

                Icon(
                    Icons.Default.SportsEsports,
                    contentDescription = null,

                    modifier =
                        Modifier.padding(14.dp),

                    tint =
                        MaterialTheme.colorScheme.primary
                )
            }

            Spacer(
                Modifier.height(18.dp)
            )

            Text(
                game.name,

                style =
                    MaterialTheme.typography
                        .titleLarge,

                fontWeight =
                    FontWeight.Bold
            )

            Text(
                game.subtitle,

                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                Modifier.height(16.dp)
            )

            FilledTonalButton(
                onClick = {},

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    "Abrir perfil"
                )
            }
        }
    }
}

@Composable
fun QuickToolsCard() {

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(24.dp)
    ) {

        Column(
            modifier =
                Modifier.padding(20.dp)
        ) {

            Text(
                "Ferramentas rápidas",

                style =
                    MaterialTheme.typography
                        .titleLarge,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                Modifier.height(14.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                ToolChip(
                    Icons.Default.Keyboard,
                    "Teclado",
                    Modifier.weight(1f)
                )

                ToolChip(
                    Icons.Default.Mouse,
                    "Mouse",
                    Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ToolChip(
    icon: ImageVector,
    text: String,
    modifier: Modifier
) {

    FilledTonalButton(
        onClick = {},
        modifier = modifier
    ) {

        Icon(
            icon,
            contentDescription = null
        )

        Spacer(
            Modifier.width(6.dp)
        )

        Text(text)
    }
}

@Composable
fun MapperScreen(
    padding: PaddingValues
) {

    LazyColumn(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(18.dp),

        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {

        item {

            Text(
                "Editor de mapeamento",

                style =
                    MaterialTheme.typography
                        .headlineSmall,

                fontWeight =
                    FontWeight.Bold
            )

            Text(
                "Configure seus controles visualmente.",

                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }

        item {
            MappingPreview()
        }

        item {

            Button(
                onClick = {},
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Icon(
                    Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(
                    Modifier.width(8.dp)
                )

                Text(
                    "Adicionar controle"
                )
            }
        }
    }
}

@Composable
fun MappingPreview() {

    Box(

        modifier =
            Modifier
                .fillMaxWidth()
                .height(330.dp)
                .clip(
                    RoundedCornerShape(24.dp)
                )
                .background(
                    Color(0xFF0C0C12)
                )
    ) {

        Text(
            "ÁREA DO JOGO",

            modifier =
                Modifier.align(
                    Alignment.Center
                ),

            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )

        MappingBubble(
            "WASD",
            Modifier.align(
                Alignment.BottomStart
            )
        )

        MappingBubble(
            "LMB",
            Modifier.align(
                Alignment.CenterEnd
            )
        )

        MappingBubble(
            "SPACE",
            Modifier.align(
                Alignment.BottomEnd
            )
        )

        MappingBubble(
            "AIM",
            Modifier.align(
                Alignment.CenterStart
            )
        )
    }
}

@Composable
fun MappingBubble(
    text: String,
    modifier: Modifier
) {

    Surface(

        modifier =
            modifier.padding(14.dp),

        shape =
            RoundedCornerShape(14.dp),

        color =
            MaterialTheme.colorScheme
                .primary
                .copy(alpha = 0.9f)
    ) {

        Text(
            text,

            modifier =
                Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 9.dp
                ),

            fontWeight =
                FontWeight.Bold
        )
    }
}

@Composable
fun ToolsScreen(
    padding: PaddingValues
) {

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(18.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        Text(
            "Ferramentas",

            style =
                MaterialTheme.typography
                    .headlineSmall,

            fontWeight =
                FontWeight.Bold
        )

        ToolRow(
            "🎯",
            "Modo mira",
            "Sensibilidade e área de captura"
        )

        ToolRow(
            "🖱️",
            "Mouse",
            "Cursor e botões"
        )

        ToolRow(
            "⌨️",
            "Teclado",
            "Teclas e atalhos"
        )

        ToolRow(
            "⚡",
            "Macro",
            "Ações sequenciais"
        )
    }
}

@Composable
fun ToolRow(
    icon: String,
    title: String,
    subtitle: String
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(20.dp)
    ) {

        Row(

            modifier =
                Modifier.padding(18.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                icon,

                style =
                    MaterialTheme.typography
                        .headlineSmall
            )

            Spacer(
                Modifier.width(14.dp)
            )

            Column {

                Text(
                    title,
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
}

@Composable
fun SettingsScreen(
    padding: PaddingValues,
    onAccessibility: () -> Unit,
    onAndroidSettings: () -> Unit
) {

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(18.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        Text(
            "Configurações",

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
                "Abrir Acessibilidade"
            )
        }

        OutlinedButton(
            onClick =
                onAndroidSettings,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                "Configurações do Android"
            )
        }

        Text(
            "Kovick Mapper • 0.2.0",

            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )
    }
}
