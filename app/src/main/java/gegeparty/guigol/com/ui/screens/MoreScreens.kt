package gegeparty.guigol.com.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gegeparty.guigol.com.data.Collections
import gegeparty.guigol.com.data.Defaults
import gegeparty.guigol.com.data.Expense
import gegeparty.guigol.com.data.Guest
import gegeparty.guigol.com.data.GuestStatus
import gegeparty.guigol.com.data.Idea
import gegeparty.guigol.com.data.Note
import gegeparty.guigol.com.data.PartyConfig
import gegeparty.guigol.com.data.Users
import gegeparty.guigol.com.notifications.Prefs
import gegeparty.guigol.com.ui.Fmt
import gegeparty.guigol.com.ui.MainViewModel
import gegeparty.guigol.com.ui.components.AddFab
import gegeparty.guigol.com.ui.components.AssigneePicker
import gegeparty.guigol.com.ui.components.ChipRow
import gegeparty.guigol.com.ui.components.ConfirmDialog
import gegeparty.guigol.com.ui.components.DateTimeField
import gegeparty.guigol.com.ui.components.EmptyState
import gegeparty.guigol.com.ui.components.FieldLabel
import gegeparty.guigol.com.ui.components.FormSheet
import gegeparty.guigol.com.ui.components.GegeCard
import gegeparty.guigol.com.ui.components.GegeField
import gegeparty.guigol.com.ui.components.Pill
import gegeparty.guigol.com.ui.components.ScreenHeader
import gegeparty.guigol.com.ui.components.SectionTitle
import gegeparty.guigol.com.ui.components.SoftProgress
import gegeparty.guigol.com.ui.components.UserBadge
import gegeparty.guigol.com.ui.theme.Gege

// =====================================================================
// MAIS
// =====================================================================
@Composable
fun MoreScreen(vm: MainViewModel, open: (String) -> Unit) {
    val ideas by vm.ideas.collectAsStateWithLifecycle()
    val notes by vm.notes.collectAsStateWithLifecycle()
    val guests by vm.guests.collectAsStateWithLifecycle()
    val expenses by vm.expenses.collectAsStateWithLifecycle()
    val cfg by vm.config.collectAsStateWithLifecycle()
    val placesState by vm.places.collectAsStateWithLifecycle()

    data class Entry(val emoji: String, val title: String, val sub: String, val bg: Color, val route: String)
    val confirmed = guests.filter { it.status == GuestStatus.CONFIRMED }
    val entries = listOf(
        Entry("📍", "Lugares", "${placesState.size} opções • ${placesState.count { it.status == gegeparty.guigol.com.data.PlaceStatus.SELECTED }} escolhido(s)", Gege.PinkSoft, "places"),
        Entry("💡", "Ideias", "${ideas.size} ideias • ${ideas.count { it.approved }} aprovadas", Gege.YellowSoft, "ideas"),
        Entry("📝", "Bloco de notas", "${notes.size} notas", Gege.LilacSoft, "notes"),
        Entry("👯", "Convidados", "${confirmed.size + confirmed.sumOf { it.plusOnes }} confirmados de ${guests.size + guests.sumOf { it.plusOnes }}", Gege.MintSoft, "guests"),
        Entry("💰", "Orçamento", Fmt.money(expenses.sumOf { it.amount }) + if (cfg.budget > 0) " de ${Fmt.money(cfg.budget)}" else "", Gege.SkySoft, "budget"),
        Entry("⚙️", "Ajustes da festa", "Data, nome, orçamento, notificações", Gege.Bg, "settings"),
    )

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader("Mais", "Tudo da festa num lugar só") }
        items(entries) { e ->
            GegeCard(Modifier.padding(horizontal = 20.dp), onClick = { open(e.route) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(52.dp).clip(CircleShape).background(e.bg), contentAlignment = Alignment.Center) {
                        Text(e.emoji, style = MaterialTheme.typography.headlineSmall)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(e.title, style = MaterialTheme.typography.titleMedium)
                        Text(e.sub, style = MaterialTheme.typography.bodyMedium, color = Gege.InkSoft)
                    }
                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = Gege.InkSoft)
                }
            }
        }
    }
}

// =====================================================================
// IDEIAS
// =====================================================================
@Composable
fun IdeasScreen(vm: MainViewModel, onBack: () -> Unit) {
    val ideas by vm.ideas.collectAsStateWithLifecycle()
    var cat by rememberSaveable { mutableStateOf("Todas") }
    var editing by remember { mutableStateOf<Idea?>(null) }
    val cats = listOf("Todas", "⭐ Aprovadas") + (Defaults.ideaCategories + ideas.map { it.category }).distinct()
    val list = ideas.filter {
        when (cat) { "Todas" -> true; "⭐ Aprovadas" -> it.approved; else -> it.category == cat }
    }.sortedWith(compareByDescending<Idea> { it.approved }.thenByDescending { it.likes.size })

    Scaffold(containerColor = Gege.Bg, contentWindowInsets = WindowInsets(0, 0, 0, 0), floatingActionButton = { AddFab("Nova ideia", { editing = Idea(category = if (cat in Defaults.ideaCategories) cat else "Geral") }) }) { pad ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 10.dp,
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Column {
                    ScreenHeader("Ideias", "Joga tudo aqui, depois escolhemos ✨", onBack)
                    ChipRow(cats, cat, { it }, { cat = it }, Modifier.padding(horizontal = 4.dp))
                }
            }
            if (list.isEmpty()) item(span = StaggeredGridItemSpan.FullLine) {
                EmptyState("💡", "Sem ideias aqui", "Tema, brincadeiras, look, surpresas… anota tudo!")
            }
            items(list, key = { it.id }) { idea ->
                val liked = vm.me in idea.likes
                GegeCard(
                    color = if (idea.approved) Gege.YellowSoft else Color.White,
                    onClick = { editing = idea },
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Pill(idea.category, Gege.Lilac, Gege.LilacSoft, Modifier.weight(1f, fill = false))
                        Spacer(Modifier.weight(1f))
                        if (idea.approved) Text("⭐")
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(idea.title, style = MaterialTheme.typography.titleMedium)
                    if (idea.description.isNotBlank()) Text(idea.description, style = MaterialTheme.typography.bodyMedium, color = Gege.InkSoft, maxLines = 6, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserBadge(idea.createdBy, compact = true)
                        Spacer(Modifier.weight(1f))
                        Text(idea.likes.joinToString("") { Users.emoji(it) }, style = MaterialTheme.typography.labelMedium)
                        IconButton(onClick = { vm.sendMessage("Olha essa ideia!", "💡 ${idea.title}", "ideas") }, modifier = Modifier.size(36.dp)) {
                            Text("💬")
                        }
                        IconButton(onClick = { vm.toggleLike(Collections.IDEAS, idea.id, idea.likes) }, modifier = Modifier.size(36.dp)) {
                            Icon(if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, "Curtir", tint = Gege.Pink, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }

    editing?.let { i ->
        var title by remember(i) { mutableStateOf(i.title) }
        var desc by remember(i) { mutableStateOf(i.description) }
        var category by remember(i) { mutableStateOf(i.category) }
        var approved by remember(i) { mutableStateOf(i.approved) }
        FormSheet(
            if (i.id.isBlank()) "Nova ideia" else "Editar ideia", onDismiss = { editing = null },
            saveEnabled = title.isNotBlank(),
            onDelete = if (i.id.isNotBlank()) ({ vm.delete(Collections.IDEAS, i.id); editing = null }) else null,
            onSave = { vm.saveIdea(i.copy(title = title.trim(), description = desc.trim(), category = category, approved = approved)); editing = null },
        ) {
            GegeField(title, { title = it }, "Ideia")
            FieldLabel("Categoria")
            ChipRow(Defaults.ideaCategories, category, { it }, { category = it })
            GegeField(desc, { desc = it }, "Detalhes, links, inspirações", singleLine = false, minLines = 4)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⭐ Aprovada (vamos fazer!)", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(approved, { approved = it }, colors = SwitchDefaults.colors(checkedTrackColor = Gege.Pink))
            }
        }
    }
}

// =====================================================================
// NOTAS
// =====================================================================
@Composable
fun NotesScreen(vm: MainViewModel, onBack: () -> Unit) {
    val notes by vm.notes.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Note?>(null) }
    val sorted = notes.sortedWith(compareByDescending<Note> { it.pinned }.thenByDescending { it.updatedAt })

    Scaffold(containerColor = Gege.Bg, contentWindowInsets = WindowInsets(0, 0, 0, 0), floatingActionButton = { AddFab("Nova nota", { editing = Note() }) }) { pad ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 10.dp,
        ) {
            item(span = StaggeredGridItemSpan.FullLine) { ScreenHeader("Bloco de notas", "Listas, recados, senhas do Wi-Fi do salão…", onBack) }
            if (sorted.isEmpty()) item(span = StaggeredGridItemSpan.FullLine) {
                EmptyState("📝", "Nenhuma nota", "Use para listas de compras, textos do convite, qualquer coisa.")
            }
            items(sorted, key = { it.id }) { n ->
                GegeCard(color = Gege.NoteColors[n.color.coerceIn(0, Gege.NoteColors.lastIndex)], onClick = { editing = n }, contentPadding = PaddingValues(14.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(n.title.ifBlank { "Sem título" }, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        if (n.pinned) Icon(Icons.Filled.PushPin, "Fixada", tint = Gege.Pink, modifier = Modifier.size(16.dp))
                    }
                    if (n.content.isNotBlank()) Text(n.content, style = MaterialTheme.typography.bodyMedium, maxLines = 10, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(8.dp))
                    Text("${Users.emoji(n.createdBy)} ${Fmt.ago(n.updatedAt)}", style = MaterialTheme.typography.labelSmall, color = Gege.InkSoft)
                }
            }
        }
    }

    editing?.let { n ->
        var title by remember(n) { mutableStateOf(n.title) }
        var content by remember(n) { mutableStateOf(n.content) }
        var color by remember(n) { mutableStateOf(n.color) }
        var pinned by remember(n) { mutableStateOf(n.pinned) }
        FormSheet(
            if (n.id.isBlank()) "Nova nota" else "Nota", onDismiss = { editing = null },
            saveEnabled = title.isNotBlank() || content.isNotBlank(),
            onDelete = if (n.id.isNotBlank()) ({ vm.delete(Collections.NOTES, n.id); editing = null }) else null,
            onSave = { vm.saveNote(n.copy(title = title.trim(), content = content.trimEnd(), color = color, pinned = pinned)); editing = null },
        ) {
            GegeField(title, { title = it }, "Título")
            GegeField(content, { content = it }, "Escreva aqui…", singleLine = false, minLines = 8)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Gege.NoteColors.forEachIndexed { i, c ->
                    Box(
                        Modifier.size(34.dp).clip(CircleShape).background(c)
                            .border(if (i == color) 3.dp else 1.dp, if (i == color) Gege.Pink else Gege.Line, CircleShape)
                            .clickable { color = i }
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { pinned = !pinned }) {
                    Icon(Icons.Filled.PushPin, "Fixar", tint = if (pinned) Gege.Pink else Gege.Line)
                }
            }
        }
    }
}

// =====================================================================
// CONVIDADOS
// =====================================================================
@Composable
fun GuestsScreen(vm: MainViewModel, onBack: () -> Unit) {
    val guests by vm.guests.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf("todos") }
    var editing by remember { mutableStateOf<Guest?>(null) }
    val ctx = LocalContext.current

    fun people(l: List<Guest>) = l.size + l.sumOf { it.plusOnes }
    val confirmed = guests.filter { it.status == GuestStatus.CONFIRMED }
    val list = guests.filter { filter == "todos" || it.status == filter }

    Scaffold(containerColor = Gege.Bg, contentWindowInsets = WindowInsets(0, 0, 0, 0), floatingActionButton = { AddFab("Convidado", { editing = Guest() }) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                ScreenHeader("Convidados", "${people(confirmed)} confirmados de ${people(guests)} pessoas", onBack)
                if (guests.isNotEmpty()) {
                    Row(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Triple(GuestStatus.PENDING, Gege.Coral, Gege.YellowSoft),
                            Triple(GuestStatus.INVITED, Gege.Sky, Gege.SkySoft),
                            Triple(GuestStatus.CONFIRMED, Gege.Mint, Gege.MintSoft),
                            Triple(GuestStatus.DECLINED, Gege.InkSoft, Gege.Line),
                        ).forEach { (s, c, bg) ->
                            Surface(onClick = { filter = if (filter == s) "todos" else s }, shape = MaterialTheme.shapes.medium, color = bg, modifier = Modifier.weight(1f),
                                border = if (filter == s) androidx.compose.foundation.BorderStroke(2.dp, c) else null) {
                                Column(Modifier.padding(10.dp)) {
                                    Text("${people(guests.filter { it.status == s })}", style = MaterialTheme.typography.titleLarge, color = c)
                                    Text(GuestStatus.label(s), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
            if (guests.isEmpty()) item { EmptyState("👯", "Lista vazia", "Adicione quem vocês querem chamar e acompanhem as confirmações.", action = "Adicionar convidado", onAction = { editing = Guest() }) }
            list.groupBy { it.group }.toSortedMap().forEach { (group, gl) ->
                item(key = "g$group") {
                    Text("$group • ${people(gl)}", style = MaterialTheme.typography.titleSmall, color = Gege.Pink, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
                }
                items(gl, key = { it.id }) { g ->
                    GegeCard(Modifier.padding(horizontal = 20.dp), onClick = { editing = g }, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).clip(CircleShape).background(Gege.PinkSoft), contentAlignment = Alignment.Center) {
                                Text(g.name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium, color = Gege.Pink)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(g.name + if (g.plusOnes > 0) "  +${g.plusOnes}" else "", style = MaterialTheme.typography.bodyLarge)
                                if (g.notes.isNotBlank()) Text(g.notes, style = MaterialTheme.typography.labelMedium, color = Gege.InkSoft, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            // Toque no status avança: falta convidar → convidado → confirmado
                            val next = when (g.status) {
                                GuestStatus.PENDING -> GuestStatus.INVITED
                                GuestStatus.INVITED -> GuestStatus.CONFIRMED
                                GuestStatus.CONFIRMED -> GuestStatus.DECLINED
                                else -> GuestStatus.PENDING
                            }
                            val (c, bg) = when (g.status) {
                                GuestStatus.CONFIRMED -> Gege.Mint to Gege.MintSoft
                                GuestStatus.INVITED -> Gege.Sky to Gege.SkySoft
                                GuestStatus.DECLINED -> Gege.InkSoft to Gege.Line
                                else -> Gege.Coral to Gege.YellowSoft
                            }
                            Surface(onClick = { vm.update(Collections.GUESTS, g.id, "status" to next) }, shape = MaterialTheme.shapes.small, color = bg) {
                                Text(GuestStatus.label(g.status), color = c, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    editing?.let { g ->
        var name by remember(g) { mutableStateOf(g.name) }
        var phone by remember(g) { mutableStateOf(g.phone) }
        var group by remember(g) { mutableStateOf(g.group) }
        var status by remember(g) { mutableStateOf(g.status) }
        var plus by remember(g) { mutableStateOf(g.plusOnes) }
        var notes by remember(g) { mutableStateOf(g.notes) }
        FormSheet(
            if (g.id.isBlank()) "Novo convidado" else "Convidado", onDismiss = { editing = null },
            saveEnabled = name.isNotBlank(),
            onDelete = if (g.id.isNotBlank()) ({ vm.delete(Collections.GUESTS, g.id); editing = null }) else null,
            onSave = { vm.saveGuest(g.copy(name = name.trim(), phone = phone.trim(), group = group, status = status, plusOnes = plus, notes = notes.trim())); editing = null },
        ) {
            GegeField(name, { name = it }, "Nome")
            FieldLabel("Grupo")
            ChipRow((Defaults.guestGroups + guests.map { it.group }).distinct(), group, { it }, { group = it })
            FieldLabel("Status")
            ChipRow(GuestStatus.all, status, { GuestStatus.label(it) }, { status = it })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Acompanhantes", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = { if (plus > 0) plus-- }) { Text("–", style = MaterialTheme.typography.titleLarge) }
                Text("$plus", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { plus++ }) { Text("+", style = MaterialTheme.typography.titleLarge) }
            }
            GegeField(phone, { phone = it }, "WhatsApp / telefone", keyboard = KeyboardType.Phone)
            if (phone.isNotBlank()) TextButton(onClick = {
                val digits = phone.filter { it.isDigit() }.let { if (it.length <= 11) "55$it" else it }
                runCatching { ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://wa.me/$digits"))) }
            }) { Text("💬 Abrir conversa no WhatsApp") }
            GegeField(notes, { notes = it }, "Observações (restrição alimentar, etc.)", singleLine = false, minLines = 2)
        }
    }
}

// =====================================================================
// ORÇAMENTO
// =====================================================================
@Composable
fun BudgetScreen(vm: MainViewModel, onBack: () -> Unit) {
    val expenses by vm.expenses.collectAsStateWithLifecycle()
    val cfg by vm.config.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Expense?>(null) }
    val total = expenses.sumOf { it.amount }
    val paid = expenses.filter { it.paid }.sumOf { it.amount }

    Scaffold(containerColor = Gege.Bg, contentWindowInsets = WindowInsets(0, 0, 0, 0), floatingActionButton = { AddFab("Gasto", { editing = Expense() }) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                ScreenHeader("Orçamento", "Quanto vai custar essa alegria 💸", onBack)
                GegeCard(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), color = Gege.SkySoft) {
                    Text("Total previsto", style = MaterialTheme.typography.labelLarge, color = Gege.InkSoft)
                    Text(Fmt.money(total), style = MaterialTheme.typography.headlineMedium)
                    if (cfg.budget > 0) {
                        Spacer(Modifier.height(8.dp))
                        SoftProgress((total / cfg.budget).toFloat(), if (total > cfg.budget) MaterialTheme.colorScheme.error else Gege.Sky)
                        Text(
                            if (total > cfg.budget) "Passou ${Fmt.money(total - cfg.budget)} do orçamento de ${Fmt.money(cfg.budget)}"
                            else "Sobram ${Fmt.money(cfg.budget - total)} de ${Fmt.money(cfg.budget)}",
                            style = MaterialTheme.typography.labelMedium, color = Gege.InkSoft, modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Column(Modifier.weight(1f)) {
                            Text("Pago", style = MaterialTheme.typography.labelMedium, color = Gege.InkSoft)
                            Text(Fmt.money(paid), style = MaterialTheme.typography.titleMedium, color = Gege.Mint)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("A pagar", style = MaterialTheme.typography.labelMedium, color = Gege.InkSoft)
                            Text(Fmt.money(total - paid), style = MaterialTheme.typography.titleMedium, color = Gege.Coral)
                        }
                    }
                }
            }
            if (expenses.isEmpty()) item { EmptyState("💰", "Nenhum gasto", "Anote bolo, local, decoração… e acompanhe o que já foi pago.") }
            expenses.groupBy { it.category }.forEach { (cat, l) ->
                item(key = "c$cat") {
                    SectionTitle("$cat • ${Fmt.money(l.sumOf { it.amount })}", Modifier.padding(horizontal = 20.dp))
                }
                items(l, key = { it.id }) { x ->
                    GegeCard(Modifier.padding(horizontal = 20.dp), onClick = { editing = x }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(x.paid, { vm.update(Collections.EXPENSES, x.id, "paid" to it) }, colors = CheckboxDefaults.colors(checkedColor = Gege.Mint))
                            Column(Modifier.weight(1f)) {
                                Text(x.title, style = MaterialTheme.typography.bodyLarge, textDecoration = if (x.paid) TextDecoration.LineThrough else null)
                                Text("pago por ${Users.display(x.paidBy)}", style = MaterialTheme.typography.labelSmall, color = Gege.InkSoft)
                            }
                            Text(Fmt.money(x.amount), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
                        }
                    }
                }
            }
        }
    }

    editing?.let { x ->
        var title by remember(x) { mutableStateOf(x.title) }
        var amount by remember(x) { mutableStateOf(Fmt.moneyInput(x.amount)) }
        var category by remember(x) { mutableStateOf(x.category) }
        var paidFlag by remember(x) { mutableStateOf(x.paid) }
        var paidBy by remember(x) { mutableStateOf(x.paidBy) }
        FormSheet(
            if (x.id.isBlank()) "Novo gasto" else "Gasto", onDismiss = { editing = null },
            saveEnabled = title.isNotBlank(),
            onDelete = if (x.id.isNotBlank()) ({ vm.delete(Collections.EXPENSES, x.id); editing = null }) else null,
            onSave = { vm.saveExpense(x.copy(title = title.trim(), amount = Fmt.parseMoney(amount), category = category, paid = paidFlag, paidBy = paidBy)); editing = null },
        ) {
            GegeField(title, { title = it }, "Descrição")
            GegeField(amount, { amount = it }, "Valor (R$)", keyboard = KeyboardType.Decimal)
            FieldLabel("Categoria")
            ChipRow(Defaults.expenseCategories, category, { it }, { category = it })
            FieldLabel("Quem paga")
            AssigneePicker(paidBy, { paidBy = it })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Payments, null, tint = Gege.Mint)
                Text("  Já está pago", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(paidFlag, { paidFlag = it }, colors = SwitchDefaults.colors(checkedTrackColor = Gege.Mint))
            }
        }
    }
}

// =====================================================================
// AJUSTES
// =====================================================================
@Composable
fun SettingsScreen(vm: MainViewModel, onBack: () -> Unit) {
    val cfg by vm.config.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    var title by remember { mutableStateOf(cfg.title) }
    var theme by remember { mutableStateOf(cfg.theme) }
    var budget by remember { mutableStateOf(Fmt.moneyInput(cfg.budget)) }
    var news by remember { mutableStateOf(Prefs.notifyNews(ctx)) }
    var countdown by remember { mutableStateOf(Prefs.notifyCountdown(ctx)) }
    var chat by remember { mutableStateOf(Prefs.notifyChat(ctx)) }
    var confirmLogout by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }

    // Preenche os campos quando a config chega do Firestore
    LaunchedEffect(cfg) {
        if (!loaded && cfg != PartyConfig()) {
            title = cfg.title; theme = cfg.theme; budget = Fmt.moneyInput(cfg.budget); loaded = true
        }
    }

    fun save(c: PartyConfig = cfg) = vm.saveConfig(c.copy(title = title.trim().ifBlank { PartyConfig().title }, theme = theme.trim(), budget = Fmt.parseMoney(budget)))

    LazyColumn(contentPadding = PaddingValues(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader("Ajustes da festa", "Compartilhado entre vocês dois", onBack) }
        item {
            GegeCard(Modifier.padding(horizontal = 20.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GegeField(title, { title = it }, "Nome da festa")
                    GegeField(theme, { theme = it }, "Tema (ex.: Anos 2000, Tropical…)")
                    DateTimeField("Data e hora da festa", cfg.partyMillis, { save(cfg.copy(partyMillis = it)) })
                    DateTimeField("Aniversário (dia)", cfg.birthdayMillis, { save(cfg.copy(birthdayMillis = it)) }, withTime = false)
                    GegeField(budget, { budget = it }, "Orçamento total (R$)", keyboard = KeyboardType.Decimal)
                    androidx.compose.material3.Button(
                        onClick = { save() }, modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Gege.Pink)
                    ) { Text("Salvar") }
                }
            }
        }
        item {
            GegeCard(Modifier.padding(horizontal = 20.dp)) {
                Text("Notificações", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                SettingSwitch("Avisar quando ${Users.display(Users.other(vm.me))} adicionar algo", news) { news = it; Prefs.setNotifyNews(ctx, it) }
                SettingSwitch("Mensagens do chat", chat) { chat = it; Prefs.setNotifyChat(ctx, it) }
                SettingSwitch("Contagem regressiva diária", countdown) { countdown = it; Prefs.setNotifyCountdown(ctx, it) }
                Text("Lembretes da agenda são configurados em cada evento.", style = MaterialTheme.typography.labelMedium, color = Gege.InkSoft)
            }
        }
        item {
            GegeCard(Modifier.padding(horizontal = 20.dp), onClick = { confirmLogout = true }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Outlined.Logout, null, tint = MaterialTheme.colorScheme.error)
                    Text("  Sair (${Users.display(vm.me)})", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        item {
            Text("GegeParty 1.0 • feito com 💗", style = MaterialTheme.typography.labelMedium, color = Gege.InkSoft,
                modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }

    if (confirmLogout) ConfirmDialog("Sair da conta?", "Você pode entrar de novo quando quiser.", { confirmLogout = false; vm.logout() }, { confirmLogout = false }, "Sair")
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked, onChange, colors = SwitchDefaults.colors(checkedTrackColor = Gege.Pink))
    }
}
