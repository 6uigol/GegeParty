package gegeparty.guigol.com.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gegeparty.guigol.com.data.Collections
import gegeparty.guigol.com.data.EventItem
import gegeparty.guigol.com.ui.Fmt
import gegeparty.guigol.com.ui.MainViewModel
import gegeparty.guigol.com.ui.components.AddFab
import gegeparty.guigol.com.ui.components.ChipRow
import gegeparty.guigol.com.ui.components.DateTimeField
import gegeparty.guigol.com.ui.components.EmptyState
import gegeparty.guigol.com.ui.components.FieldLabel
import gegeparty.guigol.com.ui.components.FormSheet
import gegeparty.guigol.com.ui.components.GegeCard
import gegeparty.guigol.com.ui.components.GegeField
import gegeparty.guigol.com.ui.components.ScreenHeader
import gegeparty.guigol.com.ui.components.UserBadge
import gegeparty.guigol.com.ui.theme.Gege
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

private val reminderOptions = listOf(-1, 0, 15, 60, 180, 1440, 10080)
private fun reminderLabel(m: Int) = when (m) {
    -1 -> "Sem aviso"; 0 -> "Na hora"; 15 -> "15 min antes"; 60 -> "1 h antes"
    180 -> "3 h antes"; 1440 -> "1 dia antes"; 10080 -> "1 semana antes"; else -> "$m min"
}

@Composable
fun AgendaScreen(vm: MainViewModel) {
    val events by vm.events.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<EventItem?>(null) }
    var showPast by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val fab by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    val startToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val (past, future) = events.partition { it.dateMillis < startToday }
    val byDay = future.groupBy { Fmt.localDate(it.dateMillis) }.toSortedMap()

    Scaffold(containerColor = Gege.Bg, contentWindowInsets = WindowInsets(0, 0, 0, 0), floatingActionButton = { AddFab("Novo evento", { editing = EventItem() }, fab) }) { pad ->
        LazyColumn(
            state = listState, modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { ScreenHeader("Agenda", if (future.isEmpty()) "Provas, reservas, encomendas…" else "${future.size} compromissos pela frente") }
            if (future.isEmpty()) item {
                EmptyState("📅", "Agenda livre", "Adicione degustação do bolo, visita ao local, prova do look… e receba lembretes.", action = "Adicionar evento", onAction = { editing = EventItem() })
            }
            byDay.forEach { (day, list) ->
                item(key = "d$day") {
                    val ms = list.first().dateMillis
                    Text(
                        Fmt.relativeDay(ms).let { if (it == Fmt.date(ms)) Fmt.dateFull(ms) else "$it • ${Fmt.dateFull(ms)}" },
                        style = MaterialTheme.typography.titleSmall, color = Gege.Pink,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
                items(list, key = { it.id }) { e -> EventCard(e) { editing = e } }
            }
            if (past.isNotEmpty()) item {
                TextButton(onClick = { showPast = !showPast }, modifier = Modifier.padding(horizontal = 12.dp)) {
                    Text(if (showPast) "Ocultar anteriores" else "Ver ${past.size} anteriores")
                }
            }
            if (showPast) items(past.reversed(), key = { "p" + it.id }) { e -> EventCard(e, faded = true) { editing = e } }
        }
    }

    editing?.let { e ->
        EventSheet(e, onDismiss = { editing = null }, onSave = { vm.saveEvent(it); editing = null },
            onDelete = if (e.id.isNotBlank()) ({ vm.delete(Collections.EVENTS, e.id); editing = null }) else null)
    }
}

@Composable
private fun EventCard(e: EventItem, faded: Boolean = false, onClick: () -> Unit) {
    GegeCard(Modifier.padding(horizontal = 20.dp), onClick = onClick, color = if (faded) Gege.Bg else Color.White) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(width = 56.dp, height = 56.dp).clip(RoundedCornerShape(16.dp)).background(if (faded) Gege.Line else Gege.YellowSoft),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(Fmt.time(e.dateMillis), style = MaterialTheme.typography.titleSmall, color = Gege.Coral)
                    Text(Fmt.date(e.dateMillis).substringBefore(" "), style = MaterialTheme.typography.labelSmall, color = Gege.InkSoft)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(e.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (e.location.isNotBlank()) Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Place, null, Modifier.size(14.dp), tint = Gege.InkSoft)
                    Text(" ${e.location}", style = MaterialTheme.typography.bodyMedium, color = Gege.InkSoft, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (e.description.isNotBlank()) Text(e.description, style = MaterialTheme.typography.bodyMedium, color = Gege.InkSoft, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End) {
                if (e.reminderMinutes >= 0) Icon(Icons.Outlined.NotificationsActive, reminderLabel(e.reminderMinutes), tint = Gege.Pink, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                UserBadge(e.createdBy, compact = true)
            }
        }
    }
}

@Composable
private fun EventSheet(initial: EventItem, onDismiss: () -> Unit, onSave: (EventItem) -> Unit, onDelete: (() -> Unit)?) {
    var title by remember { mutableStateOf(initial.title) }
    var desc by remember { mutableStateOf(initial.description) }
    var loc by remember { mutableStateOf(initial.location) }
    var date by remember {
        mutableStateOf(
            initial.dateMillis.takeIf { it > 0 }
                ?: LocalDate.now().plusDays(1).atTime(LocalTime.of(19, 0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
    }
    var reminder by remember { mutableStateOf(initial.reminderMinutes) }

    FormSheet(
        if (initial.id.isBlank()) "Novo evento" else "Editar evento", onDismiss = onDismiss,
        saveEnabled = title.isNotBlank(), onDelete = onDelete,
        onSave = { onSave(initial.copy(title = title.trim(), description = desc.trim(), location = loc.trim(), dateMillis = date, reminderMinutes = reminder)) },
    ) {
        GegeField(title, { title = it }, "Título (ex.: Degustação do bolo)")
        DateTimeField("Quando", date, { date = it })
        GegeField(loc, { loc = it }, "Onde")
        FieldLabel("Lembrete")
        ChipRow(reminderOptions, reminder, ::reminderLabel, { reminder = it })
        GegeField(desc, { desc = it }, "Detalhes", singleLine = false, minLines = 3)
    }
}
