package com.kovick.mapper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput

object KovickPairingNotification {
    const val ACTION_PAIR = "com.kovick.mapper.ACTION_PAIR_CODE"
    const val EXTRA_CODE = "pairing_code"

    private const val CHANNEL_ID = "kovick_pairing"
    private const val NOTIFICATION_ID = 7101
    private const val REMOTE_INPUT_KEY = "kovick_pairing_code_input"

    fun show(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Pareamento Kovick",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        val actionIntent = Intent(context, KovickPairingReceiver::class.java).apply {
            action = ACTION_PAIR
        }
        val actionPendingIntent = PendingIntent.getBroadcast(
            context,
            7101,
            actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val remoteInput = RemoteInput.Builder(REMOTE_INPUT_KEY)
            .setLabel("Código de 6 dígitos")
            .build()

        val action = NotificationCompat.Action.Builder(
            0,
            "Conectar",
            actionPendingIntent
        ).addRemoteInput(remoteInput).build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("Kovick — Pareamento Wi-Fi")
            .setContentText("Digite o código de pareamento exibido em Depuração Wi-Fi.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .addAction(action)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            manager.notify(NOTIFICATION_ID, notification)
        }
    }
}
