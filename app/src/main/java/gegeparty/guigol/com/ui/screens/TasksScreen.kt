package gegeparty.guigol.com.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.outlined.ViewKanban
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gegeparty.guigol.com.data.Collections
import gegeparty.guigol.com.data.Defaults
import gegeparty.guigol.com.data.TaskItem
import gegeparty.guigol.com.data.TaskStatus
import gegeparty.guigol.com.data.Users
import gegeparty.guigol.com.ui.Fmt
import gegeparty.guigol.com.ui.MainViewModel
import gegeparty.guigol.com.ui.components.AddFab
import gegeparty.guigol.com.ui.components.AssigneePicker
import gegeparty.guigol.com.ui.components.ChipRow
import gegeparty.guigol.com.ui.components.DateTimeField
import gegeparty.guigol.com.ui.components.EmptyState
import gegeparty.guigol.com.ui.components.FieldLabel
import gegeparty.guigol.com.ui.components.FormSheet
import gegeparty.guigol.com.ui.components.GegeCard
import gegeparty.guigol.com.ui.components.GegeField
import gegeparty.guigol.com.ui.components.Pill
import gegeparty.guigol.com.ui.components.ScreenHeader
import gegeparty.guigol.com.ui.components.SoftProgress
import gegeparty.guigol.com.ui.components.UserBadge
import gegeparty.guigol.com.ui.theme.Gege

private enum class TaskFilter(val label: String) { ALL("Todas"), MINE("Minhas"), BAIXINHA("💗 Baixinha"), QUERIDO("💙 Querido"), BOTH("💞 Nós dois"), OPEN("⏳ Faltando") }

@Composable
fun TasksScreen(vm: MainViewModel) {
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val me = vm.me
    var filter by rememberSaveable { mutableStateOf(TaskFilter.ALL) }
    var byStatus by rememberSaveable { mutableStateOf(false) }
    var editing by remember { mutableStateOf<TaskItem?>(null) }
    val listState = rememberLazyListState()
    val fabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    val filtered = tasks.filter {
        when (filter) {
            TaskFilter.ALL -> true
            TaskFilter.MINE -> it.assignee == me || it.assignee == Users.BOTH
            TaskFilter.BAIXINHA -> it.assignee == Users.BAIXINHA
            TaskFilter.QUERIDO -> it.assignee == Users.QUERIDO
            TaskFilter.BOTH -> it.assignee == Users.BOTH
            TaskFilter.OPEN -> !it.done
        }
    }
    val doneCount = tasks.count { it.done }
    val sortInGroup = compareBy<TaskItem> { it.done }.thenByDescending { it.priority }
        .thenBy { if (it.dueMillis == 0L) Long.MAX_VALUE else it.dueMillis }

    val groups: List<Pair<String, List<TaskItem>>> = if (byStatus) {
        listOf(TaskStatus.TODO, TaskStatus.DOING, TaskStatus.DONE).map { s -> TaskStatus.label(s) to filtered.filter { it.status == s }.sortedWith(sortInGroup) }
            .filter { it.second.isNotEmpty() }
    } else {
        filtered.groupBy { it.category }.toList()
            .sortedBy { (_, l) -> l.count { it.done }.toFloat() / l.size }
            .map { (k, l) -> k to l.sortedWith(sortInGroup) }
    }

    Scaffold(
        containerColor = Gege.Bg,
        floatingActionButton = { AddFab("Nova tarefa", { editing = TaskItem(assignee = Users.BOTH) }, fabExpanded) },
    ) { pad ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ScreenHeader("Tarefas", if (tasks.isEmpty()) "Divida e conquiste 💪" else "$doneCount de ${tasks.size} prontas") {
                    IconButton(onClick = { byStatus = !byStatus }) {
                        Icon(if (byStatus) Icons.Outlined.ViewAgenda else Icons.Outlined.ViewKanban, if (byStatus) "Ver por área" else "Ver por status")
                    }
                }
                if (tasks.isNotEmpty()) {
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        SoftProgress(if (tasks.isEmpty()) 0f else doneCount.toFloat() / tasks.size, Gege.Mint)
                        Spacer(Modifier.height(12.dp))
                        DivisionBar(tasks)
                        Spacer(Modifier.height(12.dp))
                        ChipRow(TaskFilter.entries, filter, { it.label }, { filter = it })
                    }
                }
            }

            if (tasks.isEmpty()) {
                item {
                    EmptyState(
                        "📋", "Nenhuma tarefa ainda",
                        "Comece com um plano pronto de festa e ajuste do jeito de vocês.",
                        action = "✨ Gerar plano sugerido", onAction = { vm.seedPlan() }
                    )
                }
            } else if (filtered.isEmpty()) {
                item { EmptyState("🎉", "Nada por aqui", "Nenhuma tarefa nesse filtro.") }
            }

            items(groups, key = { it.first }) { (name, list) ->
                val done = list.count { it.done }
                GegeCard(Modifier.padding(horizontal = 20.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Pill(
                            if (done == list.size) "✓ Tudo certo" else "$done/${list.size}",
                            if (done == list.size) Gege.Mint else Gege.InkSoft,
                            if (done == list.size) Gege.MintSoft else Gege.Bg
                        )
                    }
                    if (!byStatus) SoftProgress(done.toFloat() / list.size, if (done == list.size) Gege.Mint else Gege.Pink, Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                    list.forEach { t ->
                        TaskRow(t, onCycle = {
                            val next = when (t.status) {
                                TaskStatus.TODO -> TaskStatus.DOING
                                TaskStatus.DOING -> TaskStatus.DONE
                                else -> TaskStatus.TODO
                            }
                            vm.setTaskStatus(t, next)
                        }, onClick = { editing = t })
                    }
                }
            }
        }
    }

    editing?.let { t ->
        val existing = remember(tasks) { (Defaults.taskCategories + tasks.map { it.category }).distinct() }
        TaskSheet(t, existing, onDismiss = { editing = null }, onSave = { vm.saveTask(it); editing = null },
            onDelete = if (t.id.isNotBlank()) ({ vm.delete(Collections.TASKS, t.id); editing = null }) else null)
    }
}

@Composable
private fun DivisionBar(tasks: List<TaskItem>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(Users.BAIXINHA, Users.QUERIDO, Users.BOTH).forEach { u ->
            val mine = tasks.filter { it.assignee == u }
            val d = mine.count { it.done }
            Surface(shape = MaterialTheme.shapes.medium, color = Gege.userSoft(u), modifier = Modifier.weight(1f)) {
                Column(Modifier.padding(10.dp)) {
                    Text("${Users.emoji(u)} ${Users.display(u)}", style = MaterialTheme.typography.labelMedium, color = Gege.userColor(u), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("$d/${mine.size}", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun TaskRow(t: TaskItem, onCycle: () -> Unit, onClick: () -> Unit) {
    val alpha by animateFloatAsState(if (t.done) .55f else 1f, label = "a")
    val tint by animateColorAsState(
        when (t.status) { TaskStatus.DONE -> Gege.Mint; TaskStatus.DOING -> Gege.Yellow; else -> Gege.Pink }, label = "c"
    )
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 2.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCycle) {
                Icon(
                    when (t.status) {
                        TaskStatus.DONE -> Icons.Filled.CheckCircle
                        TaskStatus.DOING -> Icons.Outlined.Autorenew
                        else -> Icons.Outlined.RadioButtonUnchecked
                    }, TaskStatus.label(t.status), tint = tint
                )
            }
            Column(Modifier.weight(1f).alpha(alpha)) {
                Text(
                    t.title, style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (t.done) TextDecoration.LineThrough else null,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                val meta = listOfNotNull(
                    if (t.status == TaskStatus.DOING) "em andamento" else null,
                    if (t.dueMillis > 0 && !t.done) "até ${Fmt.date(t.dueMillis)}" else null,
                    if (t.done && t.completedBy.isNotBlank()) "feito por ${Users.display(t.completedBy)}" else null,
                    t.notes.takeIf { it.isNotBlank() }?.lines()?.first(),
                )
                if (meta.isNotEmpty()) Text(
                    meta.joinToString(" • "), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            if (t.priority > 0 && !t.done) {
                Text("🔥", Modifier.padding(end = 6.dp))
            }
            UserBadge(t.assignee, compact = true)
        }
    }
}

@Composable
private fun TaskSheet(
    initial: TaskItem,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (TaskItem) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var title by remember { mutableStateOf(initial.title) }
    var notes by remember { mutableStateOf(initial.notes) }
    var category by remember { mutableStateOf(initial.category) }
    var newCategory by remember { mutableStateOf("") }
    var assignee by remember { mutableStateOf(initial.assignee) }
    var status by remember { mutableStateOf(initial.status) }
    var priority by remember { mutableStateOf(initial.priority > 0) }
    var due by remember { mutableStateOf(initial.dueMillis) }

    FormSheet(
        if (initial.id.isBlank()) "Nova tarefa" else "Editar tarefa",
        onDismiss = onDismiss,
        saveEnabled = title.isNotBlank(),
        onDelete = onDelete,
        onSave = {
            onSave(
                initial.copy(
                    title = title.trim(), notes = notes.trim(),
                    category = newCategory.trim().ifBlank { category },
                    assignee = assignee, status = status, priority = if (priority) 1 else 0, dueMillis = due,
                )
            )
        },
    ) {
        GegeField(title, { title = it }, "O que precisa ser feito?")
        FieldLabel("Quem fica responsável")
        AssigneePicker(assignee, { assignee = it })
        FieldLabel("Status")
        ChipRow(TaskStatus.all, status, { TaskStatus.label(it) }, { status = it })
        FieldLabel("Área")
        ChipRow(categories, if (newCategory.isBlank()) category else null, { it }, { category = it; newCategory = "" })
        GegeField(newCategory, { newCategory = it }, "…ou crie uma nova área")
        DateTimeField("Prazo (opcional)", due, { due = it }, withTime = false, onClear = { due = 0L })
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🔥 Importante", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(priority, { priority = it }, colors = SwitchDefaults.colors(checkedTrackColor = Gege.Pink))
        }
        GegeField(notes, { notes = it }, "Observações", singleLine = false, minLines = 3)
        Spacer(Modifier.width(1.dp).size(1.dp))
    }
}
