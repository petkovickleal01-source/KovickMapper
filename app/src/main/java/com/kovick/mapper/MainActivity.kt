package com.kovick.mapper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val KovickRed = Color(0xFFD50000)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        showHome()
    }

    private fun showHome() {
        setContent { HomeScreen() }
    }

    @Composable
    private fun HomeScreen() {
        MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = KovickRed)) {
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("KOVICK", style = MaterialTheme.typography.headlineMedium, color = KovickRed)
                    IconButton(onClick = { showSettings() }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações", tint = KovickRed)
                    }
                }

                Spacer(Modifier.height(28.dp))
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Kovick Mapper", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(6.dp))
                        Text("Gerencie seus perfis e mapeamentos de jogos.")
                    }
                }

                Spacer(Modifier.height(18.dp))
                Button(onClick = { installDefaultProfile() }, Modifier.fillMaxWidth()) {
                    Text("ADICIONAR JOGO")
                }
            }
        }
    }

    @Composable
    private fun Header(title: String, onBack: () -> Unit) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = KovickRed)
            }
            Text(title, style = MaterialTheme.typography.headlineMedium, color = KovickRed)
        }
    }

    private fun showSettings() {
        setContent { SettingsScreen() }
    }

    @Composable
    private fun SettingsScreen() {
        MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = KovickRed)) {
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                Header("Configurações") { showHome() }
                Spacer(Modifier.height(20.dp))

                Button(onClick = { showPermissions() }, Modifier.fillMaxWidth()) {
                    Text("Permissões")
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = { showActivationMode() }, Modifier.fillMaxWidth()) {
                    Text("Modo de ativação")
                }
            }
        }
    }

    private fun showPermissions() {
        setContent { PermissionsScreen() }
    }

    @Composable
    private fun PermissionsScreen() {
        MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = KovickRed)) {
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                Header("Permissões") { showSettings() }
                Spacer(Modifier.height(20.dp))

                Button(onClick = { openOverlaySettings() }, Modifier.fillMaxWidth()) {
                    Text("Sobreposição de tela")
                }
                Spacer(Modifier.height(10.dp))
                Button(onClick = { openUsageAccessSettings() }, Modifier.fillMaxWidth()) {
                    Text("Detectar jogo aberto")
                }
                Spacer(Modifier.height(10.dp))
                Button(onClick = { requestUnrestrictedBattery() }, Modifier.fillMaxWidth()) {
                    Text("Bateria sem restrição")
                }
            }
        }
    }

    private fun showActivationMode() {
        setContent { ActivationModeScreen() }
    }

    @Composable
    private fun ActivationModeScreen() {
        MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = KovickRed)) {
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                Header("Modo de ativação") { showSettings() }
                Spacer(Modifier.height(20.dp))
                Button(onClick = { openWirelessDebugSettings() }, Modifier.fillMaxWidth()) {
                    Text("Depuração Wi-Fi (para ativação)")
                }
            }
        }
    }

    private fun openOverlaySettings() {
        startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
        )
    }

    private fun openUsageAccessSettings() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    private fun requestUnrestrictedBattery() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }

    private fun openWirelessDebugSettings() {
        startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
    }

    private fun installDefaultProfile() {
        KovickProfileStore.savePoint(this, KeyEvent.KEYCODE_W, .50f, .35f)
        KovickProfileStore.savePoint(this, KeyEvent.KEYCODE_A, .35f, .50f)
        KovickProfileStore.savePoint(this, KeyEvent.KEYCODE_S, .50f, .65f)
        KovickProfileStore.savePoint(this, KeyEvent.KEYCODE_D, .65f, .50f)
    }
}
