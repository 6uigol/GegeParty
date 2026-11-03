package gegeparty.guigol.com.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import gegeparty.guigol.com.data.AuthRepository
import gegeparty.guigol.com.data.Collections
import gegeparty.guigol.com.data.EventItem
import gegeparty.guigol.com.data.PartyConfig
import gegeparty.guigol.com.data.Repo
import gegeparty.guigol.com.data.Users
import gegeparty.guigol.com.ui.Fmt
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

object Prefs {
    private const val FILE = "gege_prefs"
    fun get(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun notifyNews(ctx: Context) = get(ctx).getBoolean("notify_news", true)
    fun notifyCountdown(ctx: Context) = get(ctx).getBoolean("notify_countdown", true)
    fun setNotifyNews(ctx: Context, v: Boolean) = get(ctx).edit().putBoolean("notify_news", v).apply()
    fun setNotifyCountdown(ctx: Context, v: Boolean) = get(ctx).edit().putBoolean("notify_countdown", v).apply()
}

/** Agenda lembretes locais para os eventos da agenda (funciona com o app fechado). */
object ReminderScheduler {
    private const val KEY_SCHEDULED = "scheduled_events"

    fun sync(ctx: Context, events: List<EventItem>) {
        val wm = WorkManager.getInstance(ctx)
        val prefs = Prefs.get(ctx)
        val now = System.currentTimeMillis()
        val newIds = mutableSetOf<String>()

        events.forEach { e ->
            if (e.reminderMinutes < 0 || e.dateMillis <= 0) return@forEach
            val fireAt = e.dateMillis - e.reminderMinutes * 60_000L
            if (fireAt <= now) return@forEach
            newIds += e.id
            val req = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(fireAt - now, TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(
                        "title" to e.title,
                        "when" to Fmt.dateTime(e.dateMillis),
                        "location" to e.location,
                        "id" to e.id,
                    )
                )
                .addTag("event")
                .build()
            wm.enqueueUniqueWork("event_${e.id}", ExistingWorkPolicy.REPLACE, req)
        }

        val old = prefs.getStringSet(KEY_SCHEDULED, emptySet()).orEmpty()
        (old - newIds).forEach { wm.cancelUniqueWork("event_$it") }
        prefs.edit().putStringSet(KEY_SCHEDULED, newIds).apply()
    }

    fun cancelAll(ctx: Context) {
        WorkManager.getInstance(ctx).cancelAllWorkByTag("event")
        Prefs.get(ctx).edit().remove(KEY_SCHEDULED).apply()
    }
}

class ReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val title = inputData.getString("title").orEmpty()
        val whenTxt = inputData.getString("when").orEmpty()
        val loc = inputData.getString("location").orEmpty()
        val id = inputData.getString("id").orEmpty()
        Notifier.show(
            applicationContext, Notifier.CH_REMINDERS, id.hashCode(),
            "⏰ $title", listOf(whenTxt, loc).filter { it.isNotBlank() }.joinToString(" • "), "agenda"
        )
        return Result.success()
    }
}

/**
 * Roda a cada ~15 min: avisa o que o(a) parceiro(a) adicionou, reagenda lembretes
 * e mostra a contagem regressiva uma vez por dia. Não precisa de servidor/Cloud Functions.
 */
class SyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val me = AuthRepository.currentUser ?: return Result.success()
        val ctx = applicationContext
        val prefs = Prefs.get(ctx)
        val now = System.currentTimeMillis()
        val last = prefs.getLong("last_sync", now)

        try {
            if (Prefs.notifyNews(ctx) && last < now) {
                // coleção -> (nome do item, rota da tela)
                val labels = mapOf(
                    Collections.TASKS to ("tarefa" to "tasks"),
                    Collections.EVENTS to ("evento" to "agenda"),
                    Collections.IDEAS to ("ideia" to "ideas"),
                    Collections.NOTES to ("nota" to "notes"),
                    Collections.PLACES to ("lugar" to "places"),
                    Collections.GUESTS to ("convidado" to "guests"),
                    Collections.EXPENSES to ("gasto" to "budget"),
                )
                labels.forEach { (col, pair) ->
                    val docs = Repo.db.collection(col).whereGreaterThan("createdAt", last).get().await().documents
                    val fromPartner = docs.filter { (it.getString("createdBy") ?: me) != me }
                    if (fromPartner.isNotEmpty()) {
                        val who = Users.display(Users.other(me))
                        val first = fromPartner.first().let { it.getString("title") ?: it.getString("name") ?: "" }
                        val text = if (fromPartner.size == 1) "$who adicionou ${pair.first}: $first"
                        else "$who adicionou ${fromPartner.size} novidades (${pair.first} e mais)"
                        Notifier.show(ctx, Notifier.CH_NEWS, col.hashCode(), "✨ Novidade no GegeParty", text, pair.second)
                    }
                }
            }

            // Reagenda lembretes (inclui eventos criados pelo outro celular)
            val events = Repo.db.collection(Collections.EVENTS).get().await().documents
                .mapNotNull { d -> d.toObject(EventItem::class.java)?.also { it.id = d.id } }
            ReminderScheduler.sync(ctx, events)

            // Contagem regressiva diária
            val today = LocalDate.now().toEpochDay()
            if (Prefs.notifyCountdown(ctx) && prefs.getLong("last_countdown", -1) != today &&
                java.time.LocalTime.now().hour >= 9
            ) {
                val cfg = Repo.db.collection(Collections.CONFIG).document(Collections.PARTY_DOC).get().await()
                    .toObject(PartyConfig::class.java)
                val target = cfg?.partyMillis?.takeIf { it > 0 } ?: cfg?.birthdayMillis ?: 0L
                if (target > 0) {
                    val days = ChronoUnit.DAYS.between(
                        LocalDate.now(),
                        java.time.Instant.ofEpochMilli(target).atZone(ZoneId.systemDefault()).toLocalDate()
                    )
                    val text = when {
                        days > 1 -> "Faltam $days dias para a festa! 🎉"
                        days == 1L -> "É amanhã!! Tudo pronto? 🎈"
                        days == 0L -> "Hoje é o grande dia! Parabéns, Baixinha! 🎂"
                        else -> null
                    }
                    text?.let { Notifier.show(ctx, Notifier.CH_COUNTDOWN, 777, cfg?.title ?: "GegeParty", it, "home") }
                }
                prefs.edit().putLong("last_countdown", today).apply()
            }
            prefs.edit().putLong("last_sync", now).apply()
        } catch (_: Exception) {
            return Result.retry()
        }
        return Result.success()
    }

    companion object {
        fun schedule(ctx: Context) {
            val req = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork("gege_sync", ExistingPeriodicWorkPolicy.KEEP, req)
        }

        fun markSeenNow(ctx: Context) {
            Prefs.get(ctx).edit().putLong("last_sync", System.currentTimeMillis()).apply()
        }
    }
}
