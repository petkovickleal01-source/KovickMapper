package com.kovick.mapper

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Mapping(
    val key: String,
    val action: String,
    val x: Float = 0.5f,
    val y: Float = 0.5f
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                MapperHome(
                    onAccessibility = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                )
            }
        }
    }
}

@Composable
fun MapperHome(onAccessibility: () -> Unit) {
    var profile by remember { mutableStateOf("Free Fire") }
    var mappings by remember {
        mutableStateOf(
            listOf(
                Mapping("W", "Mover para cima"),
                Mapping("A", "Mover para esquerda"),
                Mapping("S", "Mover para baixo"),
                Mapping("D", "Mover para direita"),
                Mapping("SPACE", "Pular"),
                Mapping("LMB", "Atirar")
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kovick Mapper") },
                actions = {
                    IconButton(onClick = onAccessibility) {
                        Icon(Icons.Default.Settings, "Configurações")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                mappings = mappings + Mapping("NEW", "Nova ação")
            }) {
                Icon(Icons.Default.Add, "Adicionar")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Perfil", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(profile, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(18.dp))
                Button(onClick = onAccessibility, modifier = Modifier.fillMaxWidth()) {
                    Text("Ativar serviço do Kovick Mapper")
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "A base já está preparada para perfis, mapeamentos e gestos. O modo mira será conectado à camada de entrada na próxima etapa.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(10.dp))
            }
            items(mappings) { mapping ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text(mapping.key) }
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(mapping.action)
                    }
                }
            }
        }
    }
}