package com.kovick.mapper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

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

        // The six-digit code is captured here. The actual ADB pairing transport
        // still requires the pairing endpoint (IP/port) and ADB pairing protocol.
        // It is intentionally not faked as a successful connection.
        Toast.makeText(context, "Código recebido pelo Kovick", Toast.LENGTH_SHORT).show()
    }
}
