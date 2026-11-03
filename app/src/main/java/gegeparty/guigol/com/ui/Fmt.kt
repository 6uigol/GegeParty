package gegeparty.guigol.com.ui

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object Fmt {
    private val br = Locale.forLanguageTag("pt-BR")
    private val zone get() = ZoneId.systemDefault()
    private val dateF = DateTimeFormatter.ofPattern("dd 'de' MMM", br)
    private val dateFullF = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", br)
    private val timeF = DateTimeFormatter.ofPattern("HH:mm", br)
    private val dateTimeF = DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm", br)
    private val money = NumberFormat.getCurrencyInstance(br)

    fun date(ms: Long) = Instant.ofEpochMilli(ms).atZone(zone).format(dateF)
    fun dateFull(ms: Long) = Instant.ofEpochMilli(ms).atZone(zone).format(dateFullF).replaceFirstChar { it.uppercase() }
    fun time(ms: Long) = Instant.ofEpochMilli(ms).atZone(zone).format(timeF)
    fun dateTime(ms: Long) = Instant.ofEpochMilli(ms).atZone(zone).format(dateTimeF)
    fun money(v: Double): String = money.format(v)
    fun localDate(ms: Long): LocalDate = Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()

    fun daysUntil(ms: Long): Long = ChronoUnit.DAYS.between(LocalDate.now(), localDate(ms))

    fun relativeDay(ms: Long): String = when (val d = daysUntil(ms)) {
        0L -> "Hoje"
        1L -> "Amanhã"
        -1L -> "Ontem"
        in 2..6 -> "Em $d dias"
        else -> date(ms)
    }

    fun ago(ms: Long): String {
        val min = (System.currentTimeMillis() - ms) / 60_000
        return when {
            min < 1 -> "agora"
            min < 60 -> "há $min min"
            min < 60 * 24 -> "há ${min / 60} h"
            else -> date(ms)
        }
    }

    fun parseMoney(s: String): Double {
        val clean = s.replace("R$", "").replace(" ", "").trim()
        val normalized = if (clean.contains(",")) clean.replace(".", "").replace(",", ".") else clean
        return normalized.toDoubleOrNull() ?: 0.0
    }

    fun moneyInput(v: Double): String =
        if (v == 0.0) "" else String.format(br, "%.2f", v)
}
