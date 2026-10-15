package com.kovick.mapper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.wirelessdebug.WdbContext
import com.wirelessdebug.service.AdbPairClient
import com.wirelessdebug.service.AdbShellController
import java.util.concurrent.Executors

class KovickPairingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != KovickPairingNotification.ACTION_PAIR) return

        val code = intent.getStringExtra(KovickPairingNotification.EXTRA_CODE)
            ?.filter(Char::isDigit)
            ?.take(6)
            ?: return

        if (code.length != 6) {
            Toast.makeText(context, "Código de pareamento inválido", Toast.LENGTH_SHORT).show()
            return
        }

        val pending = goAsync()
        Executors.newSingleThreadExecutor().execute {
            try {
                WdbContext.init(context)

                // O Android anuncia a porta temporária _adb-tls-pairing somente
                // enquanto a tela "Pair using pairing code" está aberta.
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
