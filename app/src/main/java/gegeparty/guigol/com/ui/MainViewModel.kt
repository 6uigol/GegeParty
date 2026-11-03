package gegeparty.guigol.com.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.Query
import gegeparty.guigol.com.data.AuthRepository
import gegeparty.guigol.com.data.ChatMessage
import gegeparty.guigol.com.data.Collections
import gegeparty.guigol.com.data.EventItem
import gegeparty.guigol.com.data.Expense
import gegeparty.guigol.com.data.Guest
import gegeparty.guigol.com.data.GuestStatus
import gegeparty.guigol.com.data.HasId
import gegeparty.guigol.com.data.Idea
import gegeparty.guigol.com.data.Note
import gegeparty.guigol.com.data.PartyConfig
import gegeparty.guigol.com.data.Place
import gegeparty.guigol.com.data.PlaceStatus
import gegeparty.guigol.com.data.Repo
import gegeparty.guigol.com.data.TaskItem
import gegeparty.guigol.com.data.TaskStatus
import gegeparty.guigol.com.data.Users
import gegeparty.guigol.com.notifications.AppState
import gegeparty.guigol.com.notifications.Notifier
import gegeparty.guigol.com.notifications.Prefs
import gegeparty.guigol.com.notifications.ReminderScheduler
import gegeparty.guigol.com.notifications.SyncWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Item do painel "o que já está certo / o que falta". Gerado 100% a partir dos dados. */
data class CheckItem(
    val label: String,
    val done: Boolean,
    val detail: String = "",
    val route: String = "",
    val auto: Boolean = false,
)

data class CategoryProgress(val name: String, val done: Int, val total: Int) {
    val progress get() = if (total == 0) 0f else done.toFloat() / total
}

data class PartyStatus(
    val progress: Float = 0f,
    val doneCount: Int = 0,
    val totalCount: Int = 0,
    val ready: List<CheckItem> = emptyList(),
    val missing: List<CheckItem> = emptyList(),
    val categories: List<CategoryProgress> = emptyList(),
    val pendingBy: Map<String, Int> = emptyMap(),
    val doneBy: Map<String, Int> = emptyMap(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val _user = MutableStateFlow(AuthRepository.currentUser)
    val user: StateFlow<String?> = _user.asStateFlow()
    val me: String get() = _user.value.orEmpty()

    init {
        viewModelScope.launch { AuthRepository.authState().collect { _user.value = it } }
    }

    private fun <T : HasId> live(col: String, clazz: Class<T>, order: String = "createdAt", dir: Query.Direction = Query.Direction.DESCENDING): StateFlow<List<T>> =
        _user.flatMapLatest { u -> if (u == null) flowOf(emptyList()) else Repo.listen(col, clazz, order, dir) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val tasks = live(Collections.TASKS, TaskItem::class.java)
    val events = live(Collections.EVENTS, EventItem::class.java, "dateMillis", Query.Direction.ASCENDING)
    val ideas = live(Collections.IDEAS, Idea::class.java)
    val notes = live(Collections.NOTES, Note::class.java, "updatedAt")
    val places = live(Collections.PLACES, Place::class.java)
    val guests = live(Collections.GUESTS, Guest::class.java, "name", Query.Direction.ASCENDING)
    val expenses = live(Collections.EXPENSES, Expense::class.java)
    val config: StateFlow<PartyConfig> =
        _user.flatMapLatest { u -> if (u == null) flowOf(PartyConfig()) else Repo.listenConfig() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, PartyConfig())

    val messages: StateFlow<List<ChatMessage>> =
        _user.flatMapLatest { u -> if (u == null) flowOf(emptyList()) else Repo.listenMessages() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val typing: StateFlow<Map<String, Long>> =
        _user.flatMapLatest { u -> if (u == null) flowOf(emptyMap()) else Repo.listenTyping() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val unread: StateFlow<Int> = combine(messages, _user) { msgs, u ->
        if (u == null) 0 else msgs.count { it.sender != u && u !in it.readBy }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        // Notificação instantânea de chat enquanto o app está vivo em segundo plano
        val startedAt = System.currentTimeMillis()
        viewModelScope.launch {
            var notifiedUntil = startedAt
            messages.collect { msgs ->
                val u = _user.value ?: return@collect
                val fresh = msgs.filter { it.sender != u && it.createdAt > notifiedUntil && u !in it.readBy }
                if (fresh.isEmpty()) return@collect
                notifiedUntil = fresh.maxOf { it.createdAt }
                val ctx = getApplication<Application>()
                Prefs.get(ctx).edit().putLong("last_chat_notified", notifiedUntil).apply()
                if (Prefs.notifyChat(ctx) && !(AppState.inForeground && AppState.onChat)) {
                    Notifier.show(
                        ctx, Notifier.CH_CHAT, 555,
                        "💬 ${Users.display(Users.other(u))}",
                        fresh.takeLast(5).joinToString("\n") { it.text.ifBlank { it.attachTitle } }, "chat"
                    )
                }
            }
        }
    }

    init {
        // Lembretes locais sempre em dia com a agenda compartilhada
        viewModelScope.launch {
            events.collect { if (_user.value != null) ReminderScheduler.sync(getApplication(), it) }
        }
    }

    val status: StateFlow<PartyStatus> = combine(
        tasks, places, guests, expenses, config, events
    ) { arr -> computeStatus(arr) }.stateIn(viewModelScope, SharingStarted.Eagerly, PartyStatus())

    @Suppress("UNCHECKED_CAST")
    private fun computeStatus(arr: Array<*>): PartyStatus {
        val tasks = arr[0] as List<TaskItem>
        val places = arr[1] as List<Place>
        val guests = arr[2] as List<Guest>
        val expenses = arr[3] as List<Expense>
        val cfg = arr[4] as PartyConfig
        val events = arr[5] as List<EventItem>

        val autos = buildList {
            add(
                CheckItem(
                    "Data da festa definida", cfg.partyMillis > 0,
                    if (cfg.partyMillis > 0) Fmt.dateFull(cfg.partyMillis) else "Defina em Ajustes", "settings", true
                )
            )
            val chosen = places.filter { it.status == PlaceStatus.SELECTED }
            add(
                CheckItem(
                    "Local escolhido", chosen.isNotEmpty(),
                    chosen.joinToString { it.name }.ifBlank { "${places.count { it.status == PlaceStatus.OPTION }} opções em análise" },
                    "places", true
                )
            )
            if (guests.isNotEmpty()) {
                val notInvited = guests.count { it.status == GuestStatus.PENDING }
                add(CheckItem("Todos os convites enviados", notInvited == 0, if (notInvited == 0) "${guests.size} convidados" else "Faltam $notInvited convites", "guests", true))
                val conf = guests.filter { it.status == GuestStatus.CONFIRMED }
                val waiting = guests.count { it.status == GuestStatus.INVITED }
                add(
                    CheckItem(
                        "Confirmações recebidas", waiting == 0 && conf.isNotEmpty(),
                        "${conf.size + conf.sumOf { it.plusOnes }} pessoas confirmadas" + if (waiting > 0) " • $waiting aguardando" else "",
                        "guests", true
                    )
                )
            } else {
                add(CheckItem("Lista de convidados", false, "Nenhum convidado ainda", "guests", true))
            }
            if (cfg.budget > 0 || expenses.isNotEmpty()) {
                val total = expenses.sumOf { it.amount }
                val unpaid = expenses.filter { !it.paid }
                add(
                    CheckItem(
                        "Gastos pagos", unpaid.isEmpty() && expenses.isNotEmpty(),
                        if (unpaid.isEmpty()) "Total ${Fmt.money(total)}" else "${unpaid.size} a pagar • ${Fmt.money(unpaid.sumOf { it.amount })}",
                        "budget", true
                    )
                )
                if (cfg.budget > 0) add(
                    CheckItem("Dentro do orçamento", total <= cfg.budget, "${Fmt.money(total)} de ${Fmt.money(cfg.budget)}", "budget", true)
                )
            }
        }

        val taskChecks = tasks.map {
            CheckItem(
                it.title, it.done,
                listOf(it.category, "${Users.emoji(it.assignee)} ${Users.display(it.assignee)}",
                    if (it.status == TaskStatus.DOING) "em andamento" else "").filter { s -> s.isNotBlank() }.joinToString(" • "),
                "tasks"
            )
        }

        val all = autos + taskChecks
        val done = all.count { it.done }
        val cats = tasks.groupBy { it.category }
            .map { (k, v) -> CategoryProgress(k, v.count { it.done }, v.size) }
            .sortedBy { it.progress }

        val pendingBy = tasks.filter { !it.done }.groupingBy { it.assignee }.eachCount()
        val doneBy = tasks.filter { it.done }.groupingBy { it.completedBy.ifBlank { it.assignee } }.eachCount()

        return PartyStatus(
            progress = if (all.isEmpty()) 0f else done.toFloat() / all.size,
            doneCount = done, totalCount = all.size,
            ready = all.filter { it.done },
            missing = all.filter { !it.done },
            categories = cats,
            pendingBy = pendingBy, doneBy = doneBy,
        )
    }

    // ---------- Ações ----------
    private fun now() = System.currentTimeMillis()

    fun login(user: String, pass: String, onResult: (String?) -> Unit) = viewModelScope.launch {
        val u = user.trim().lowercase()
        if (u !in Users.all) { onResult("Usuário não encontrado 💔"); return@launch }
        try {
            AuthRepository.login(u, pass)
            SyncWorker.markSeenNow(getApplication())
            onResult(null)
        } catch (e: Exception) {
            onResult("Senha incorreta ou sem internet")
        }
    }

    fun logout() {
        ReminderScheduler.cancelAll(getApplication())
        AuthRepository.logout()
    }

    fun saveTask(t: TaskItem) {
        val wasDone = tasks.value.firstOrNull { it.id == t.id }?.done == true
        val item = when {
            t.id.isBlank() -> t.copy(createdBy = me, createdAt = now())
            else -> t
        }.let {
            if (it.done && !wasDone) it.copy(completedBy = me, completedAt = now())
            else if (!it.done) it.copy(completedBy = "", completedAt = 0L) else it
        }
        Repo.save(Collections.TASKS, item)
    }

    fun setTaskStatus(t: TaskItem, status: String) {
        val fields = mutableMapOf<String, Any?>("status" to status)
        if (status == TaskStatus.DONE) { fields["completedBy"] = me; fields["completedAt"] = now() }
        else { fields["completedBy"] = ""; fields["completedAt"] = 0L }
        Repo.update(Collections.TASKS, t.id, fields)
    }

    fun seedPlan() = Repo.seedStarterPlan(me)

    fun saveEvent(e: EventItem) = Repo.save(Collections.EVENTS, if (e.id.isBlank()) e.copy(createdBy = me, createdAt = now()) else e)
    fun saveIdea(i: Idea) = Repo.save(Collections.IDEAS, if (i.id.isBlank()) i.copy(createdBy = me, createdAt = now()) else i)
    fun saveNote(n: Note) = Repo.save(
        Collections.NOTES,
        if (n.id.isBlank()) n.copy(createdBy = me, createdAt = now(), updatedAt = now()) else n.copy(updatedAt = now())
    )
    fun savePlace(p: Place) = Repo.save(Collections.PLACES, if (p.id.isBlank()) p.copy(createdBy = me, createdAt = now()) else p)
    fun saveGuest(g: Guest) = Repo.save(Collections.GUESTS, if (g.id.isBlank()) g.copy(createdBy = me, createdAt = now()) else g)
    fun saveExpense(x: Expense) = Repo.save(Collections.EXPENSES, if (x.id.isBlank()) x.copy(createdBy = me, createdAt = now()) else x)
    fun saveConfig(c: PartyConfig) = Repo.saveConfig(c)

    // ---------- Chat ----------
    private var lastTypingSent = 0L

    fun sendMessage(text: String, attachTitle: String = "", attachRoute: String = "") {
        if (text.isBlank() && attachTitle.isBlank()) return
        Repo.sendMessage(ChatMessage(text = text.trim(), sender = me, createdAt = now(), readBy = listOf(me), attachTitle = attachTitle, attachRoute = attachRoute))
        Repo.setTyping(me, false); lastTypingSent = 0L
    }

    fun react(m: ChatMessage, emoji: String) = Repo.react(m.id, me, if (m.reactions[me] == emoji) null else emoji)

    fun markAllRead() {
        val ids = messages.value.filter { it.sender != me && me !in it.readBy }.map { it.id }
        Repo.markRead(ids, me)
    }

    fun onTyping(text: String) {
        val t = now()
        if (text.isBlank()) { if (lastTypingSent > 0) { Repo.setTyping(me, false); lastTypingSent = 0L }; return }
        if (t - lastTypingSent > 3_000) { Repo.setTyping(me, true); lastTypingSent = t }
    }

    fun update(col: String, id: String, vararg fields: Pair<String, Any?>) = Repo.update(col, id, fields.toMap())
    fun delete(col: String, id: String) = Repo.delete(col, id)
    fun toggleLike(col: String, id: String, likes: List<String>) = Repo.toggleLike(col, id, me, me in likes)
}

