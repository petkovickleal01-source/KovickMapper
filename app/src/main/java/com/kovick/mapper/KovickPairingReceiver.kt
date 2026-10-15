package com.kovick.mapper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.RemoteInput
import com.wirelessdebug.WdbContext
import com.wirelessdebug.service.AdbPairClient
import com.wirelessdebug.service.AdbShellController
import java.util.concurrent.Executors

class KovickPairingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != KovickPairingNotification.ACTION_PAIR) return

        // RemoteInput não chega como Intent extra comum. O código digitado
        // na notificação precisa ser lido através de RemoteInput.getResultsFromIntent().
        val results = RemoteInput.getResultsFromIntent(intent)
        val code = results
            ?.getCharSequence("kovick_pairing_code_input")
            ?.toString()
            ?.filter(Char::isDigit)
            ?.take(6)

        if (code.isNullOrEmpty()) {
            Toast.makeText(context, "Digite o código de 6 dígitos", Toast.LENGTH_SHORT).show()
            return
        }

        if (code.length != 6) {
            Toast.makeText(context, "Código de pareamento inválido", Toast.LENGTH_SHORT).show()
            return
        }

        val pending = goAsync()
        Executors.newSingleThreadExecutor().execute {
            try {
                WdbContext.init(context)

                // A porta temporária _adb-tls-pairing existe enquanto a tela
                // "Parear usando código de pareamento" estiver aberta.
                val pairPort = AdbShellController.findPairPort(context)
                if (pairPort <= 0) {
                    Toast.makeText(
                        context,
                        "Porta de pareamento não encontrada. Deixe o código aberto na tela.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@execute
                }

                val result = AdbPairClient.pair("127.0.0.1", pairPort, code)
                val message = if (result.ok) {
                    "Kovick conectado com sucesso!"
                } else {
                    result.message
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            } catch (t: Throwable) {
                Toast.makeText(
                    context,
                    "Falha no pareamento: ${t.message ?: t.javaClass.simpleName}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                pending.finish()
            }
        }
    }
}
