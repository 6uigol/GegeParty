package gegeparty.guigol.com.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import gegeparty.guigol.com.MainActivity
import gegeparty.guigol.com.R

object Notifier {
    const val CH_REMINDERS = "lembretes"
    const val CH_NEWS = "novidades"
    const val CH_COUNTDOWN = "contagem"

    fun createChannels(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannels(
            listOf(
                NotificationChannel(CH_REMINDERS, "Lembretes da agenda", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CH_NEWS, "Novidades do(a) parceiro(a)", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CH_COUNTDOWN, "Contagem regressiva", NotificationManager.IMPORTANCE_LOW),
            )
        )
    }

    fun canNotify(ctx: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun show(ctx: Context, channel: String, id: Int, title: String, text: String, route: String? = null) {
        if (!canNotify(ctx)) return
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            route?.let { putExtra(MainActivity.EXTRA_ROUTE, it) }
        }
        val pi = PendingIntent.getActivity(
            ctx, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(ctx, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFFFF6F91.toInt())
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        try {
            NotificationManagerCompat.from(ctx).notify(id, n)
        } catch (_: SecurityException) {
        }
    }
}
