package gegeparty.guigol.com.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gegeparty.guigol.com.data.TaskStatus
import gegeparty.guigol.com.data.Users
import gegeparty.guigol.com.ui.CheckItem
import gegeparty.guigol.com.ui.Fmt
import gegeparty.guigol.com.ui.MainViewModel
import gegeparty.guigol.com.ui.components.GegeCard
import gegeparty.guigol.com.ui.components.Pill
import gegeparty.guigol.com.ui.components.ProgressRing
import gegeparty.guigol.com.ui.components.SectionTitle
import gegeparty.guigol.com.ui.components.SoftProgress
import gegeparty.guigol.com.ui.theme.Gege
import java.time.LocalTime

@Composable
fun HomeScreen(vm: MainViewModel, open: (String) -> Unit) {
    val user by vm.user.collectAsStateWithLifecycle()
    val cfg by vm.config.collectAsStateWithLifecycle()
    val status by vm.status.collectAsStateWithLifecycle()
    val events by vm.events.collectAsStateWithLifecycle()
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val me = user.orEmpty()

    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> "Bom dia"
        in 12..17 -> "Boa tarde"
        else -> "Boa noite"
    }
    val target = cfg.partyMillis.takeIf { it > 0 } ?: cfg.birthdayMillis
    val upcoming = events.filter { it.dateMillis >= System.currentTimeMillis() - 3_600_000 }.take(5)
    val myTasks = tasks.filter { !it.done && (it.assignee == me || it.assignee == Users.BOTH) }
        .sortedWith(compareByDescending<gegeparty.guigol.com.data.TaskItem> { it.priority }.thenBy { if (it.dueMillis == 0L) Long.MAX_VALUE else it.dueMillis })
        .take(4)
    var tab by rememberSaveable { mutableIntStateOf(0) }

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        // ---------- Hero ----------
        item {
            Box(
                Modifier.fillMaxWidth()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                    .background(Gege.Hero)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Text("$greeting, ${Users.display(me)} ${Users.emoji(me)}", color = Color.White.copy(alpha = .9f), style = MaterialTheme.typography.titleMedium)
                    Text(cfg.title, color = Color.White, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            if (target > 0) {
                                val d = Fmt.daysUntil(target)
                                Text(
                                    when {
                                        d > 1 -> "$d"
                                        d == 1L -> "1"
                                        d == 0L -> "🎂"
                                        else -> "🎉"
                                    },
                                    color = Color.White, style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    when {
                                        d > 1 -> "dias para a festa"
                                        d == 1L -> "dia! É amanhã!"
                                        d == 0L -> "É hoje!!"
                                        else -> "Festa realizada 💗"
                                    },
                                    color = Color.White, style = MaterialTheme.typography.titleMedium
                                )
                                Text(Fmt.dateFull(target), color = Color.White.copy(alpha = .85f), style = MaterialTheme.typography.bodyMedium)
                            } else {
                                Text("Quando vai ser? 🗓️", color = Color.White, style = MaterialTheme.typography.titleLarge)
                                TextButton(onClick = { open("settings") }) { Text("Definir a data →", color = Color.White) }
                            }
                        }
                        ProgressRing(status.progress, size = 104.dp) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${(status.progress * 100).toInt()}%", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                                Text("pronto", color = Color.White.copy(alpha = .9f), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }

        // ---------- Divisão por pessoa ----------
        item {
            Row(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(Users.BAIXINHA, Users.QUERIDO, Users.BOTH).forEach { u ->
                    Surface(
                        onClick = { open("tasks") },
                        shape = MaterialTheme.shapes.large, color = Gege.userSoft(u), modifier = Modifier.weight(1f)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(Users.emoji(u), style = MaterialTheme.typography.titleLarge)
                            Text("${status.pendingBy[u] ?: 0}", style = MaterialTheme.typography.headlineSmall, color = Gege.userColor(u))
                            Text(
                                "pendentes • ${Users.display(u)}", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // ---------- Já está certo / Falta ----------
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                SectionTitle("Status da festa", action = "Ver tarefas", onAction = { open("tasks") })
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    listOf("⏳ Falta (${status.missing.size})", "✅ Já está certo (${status.ready.size})").forEachIndexed { i, l ->
                        SegmentedButton(
                            selected = tab == i, onClick = { tab = i },
                            shape = SegmentedButtonDefaults.itemShape(i, 2),
                            colors = SegmentedButtonDefaults.colors(activeContainerColor = Gege.PinkSoft, activeContentColor = Gege.Pink),
                        ) { Text(l) }
                    }
                }
                Spacer(Modifier.height(10.dp))
                AnimatedContent(tab, label = "status") { t ->
                    val list = if (t == 0) status.missing else status.ready
                    GegeCard(Modifier.animateContentSize(), contentPadding = PaddingValues(vertical = 6.dp)) {
                        if (list.isEmpty()) {
                            Text(
                                if (t == 0) "Nada faltando! Vocês arrasaram 🎉" else "Ainda nada concluído — bora começar! 💪",
                                Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        list.take(if (t == 0) 8 else 6).forEach { CheckRow(it) { if (it.route.isNotBlank()) open(it.route) } }
                        if (list.size > 8) {
                            TextButton(onClick = { open("tasks") }, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Text("+ ${list.size - 8} itens")
                            }
                        }
                    }
                }
            }
        }

        // ---------- Por categoria ----------
        if (status.categories.isNotEmpty()) {
            item {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    SectionTitle("Progresso por área")
                    GegeCard {
                        status.categories.forEachIndexed { i, c ->
                            if (i > 0) Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(c.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                Text("${c.done}/${c.total}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(6.dp))
                            SoftProgress(c.progress, if (c.progress >= 1f) Gege.Mint else Gege.Pink)
                        }
                    }
                }
            }
        }

        // ---------- Minhas próximas tarefas ----------
        if (myTasks.isNotEmpty()) {
            item {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    SectionTitle("Suas próximas tarefas", action = "Todas", onAction = { open("tasks") })
                    GegeCard(contentPadding = PaddingValues(vertical = 6.dp)) {
                        myTasks.forEach { t ->
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.IconButton(onClick = { vm.setTaskStatus(t, TaskStatus.DONE) }) {
                                    Icon(Icons.Outlined.RadioButtonUnchecked, "Concluir", tint = Gege.Pink)
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(t.title, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        listOf(t.category, if (t.dueMillis > 0) "até ${Fmt.date(t.dueMillis)}" else "").filter { it.isNotBlank() }.joinToString(" • "),
                                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (t.priority > 0) Pill("Importante", Gege.Coral, Gege.YellowSoft)
                            }
                        }
                    }
                }
            }
        }

        // ---------- Agenda ----------
        item {
            Column(Modifier.padding(top = 8.dp)) {
                SectionTitle("Próximos compromissos", Modifier.padding(horizontal = 20.dp), action = "Agenda", onAction = { open("agenda") })
                if (upcoming.isEmpty()) {
                    GegeCard(Modifier.padding(horizontal = 20.dp), onClick = { open("agenda") }) {
                        Text("Nada marcado ainda. Toque para adicionar 📅", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(upcoming, key = { it.id }) { e ->
                            GegeCard(Modifier.width(220.dp), color = Gege.YellowSoft, onClick = { open("agenda") }) {
                                Pill(Fmt.relativeDay(e.dateMillis), Gege.Coral, Color.White)
                                Spacer(Modifier.height(8.dp))
                                Text(e.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(
                                    Fmt.time(e.dateMillis) + if (e.location.isNotBlank()) " • ${e.location}" else "",
                                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // ---------- Atalhos ----------
        item {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                SectionTitle("Atalhos")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Shortcut("💡", "Ideias", Gege.YellowSoft, Modifier.weight(1f)) { open("ideas") }
                    Shortcut("📝", "Notas", Gege.LilacSoft, Modifier.weight(1f)) { open("notes") }
                    Shortcut("👯", "Convidados", Gege.MintSoft, Modifier.weight(1f)) { open("guests") }
                    Shortcut("💰", "Gastos", Gege.SkySoft, Modifier.weight(1f)) { open("budget") }
                }
            }
        }
    }
}

@Composable
private fun CheckRow(item: CheckItem, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (item.done) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked, null,
                tint = if (item.done) Gege.Mint else Gege.Pink, modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.label, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    if (item.auto) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Outlined.AutoAwesome, "Automático", tint = Gege.Lilac, modifier = Modifier.size(14.dp))
                    }
                }
                if (item.detail.isNotBlank()) Text(
                    item.detail, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = Gege.Line)
        }
    }
}

@Composable
private fun Shortcut(emoji: String, label: String, bg: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = MaterialTheme.shapes.large, color = bg, modifier = modifier) {
        Column(Modifier.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                Text(emoji, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}
