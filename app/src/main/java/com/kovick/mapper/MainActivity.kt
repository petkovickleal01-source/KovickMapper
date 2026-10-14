package com.kovick.mapper

import android.content.Intent
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Screen() }
    }

    @Composable
    private fun Screen() {
        MaterialTheme {
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
                Text("Kovick Mapper", style = MaterialTheme.typography.headlineMedium)
                Text("Fase 1 — motor funcional")
                Spacer(Modifier.height(18.dp))
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Mapa padrão")
                        Text("W / A / S / D → pontos de toque")
                        Text("As posições são salvas no perfil ativo.")
                    }
                }
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button({ startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }, Modifier.weight(1f)) { Text("Acessibilidade") }
                    Button({ startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)) }, Modifier.weight(1f)) { Text("Overlay") }
                }
                Spacer(Modifier.height(10.dp))
                Button({ installDefaultProfile() }, Modifier.fillMaxWidth()) { Text("INSTALAR MAPA PADRÃO") }
            }
        }
    }

    private fun installDefaultProfile() {
        KovickProfileStore.savePoint(this, KeyEvent.KEYCODE_W, .50f, .35f)
        KovickProfileStore.savePoint(this, KeyEvent.KEYCODE_A, .35f, .50f)
        KovickProfileStore.savePoint(this, KeyEvent.KEYCODE_S, .50f, .65f)
        KovickProfileStore.savePoint(this, KeyEvent.KEYCODE_D, .65f, .50f)
    }
}
