package gegeparty.guigol.com.data

import com.google.firebase.firestore.Exclude

/** Tudo que vai para o Firestore tem um id (o id do documento, não é gravado como campo). */
interface HasId {
    var id: String
}

object Users {
    const val BAIXINHA = "baixinha"
    const val QUERIDO = "querido"
    const val BOTH = "ambos"
    const val EMAIL_DOMAIN = "gegeparty.app"

    val all = listOf(BAIXINHA, QUERIDO)

    fun emailOf(username: String) = "${username.trim().lowercase()}@$EMAIL_DOMAIN"
    fun usernameOf(email: String?) = email?.substringBefore("@")?.lowercase().orEmpty()

    fun display(user: String) = when (user) {
        BAIXINHA -> "Baixinha"
        QUERIDO -> "Querido"
        BOTH -> "Nós dois"
        else -> user.replaceFirstChar { it.uppercase() }
    }

    fun emoji(user: String) = when (user) {
        BAIXINHA -> "💗"
        QUERIDO -> "💙"
        BOTH -> "💞"
        else -> "✨"
    }

    fun other(user: String) = if (user == BAIXINHA) QUERIDO else BAIXINHA
}

object Collections {
    const val TASKS = "tasks"
    const val EVENTS = "events"
    const val IDEAS = "ideas"
    const val NOTES = "notes"
    const val PLACES = "places"
    const val GUESTS = "guests"
    const val EXPENSES = "expenses"
    const val CONFIG = "config"
    const val PARTY_DOC = "party"
}

// ---------- Tarefas ----------
object TaskStatus {
    const val TODO = "pendente"
    const val DOING = "andamento"
    const val DONE = "pronto"
    val all = listOf(TODO, DOING, DONE)
    fun label(s: String) = when (s) {
        TODO -> "A fazer"
        DOING -> "Em andamento"
        DONE -> "Pronto"
        else -> s
    }
}

data class TaskItem(
    @get:Exclude override var id: String = "",
    var title: String = "",
    var notes: String = "",
    var category: String = "Outros",
    var assignee: String = Users.BOTH,
    var status: String = TaskStatus.TODO,
    var priority: Int = 0, // 0 normal, 1 importante
    var dueMillis: Long = 0L,
    var createdBy: String = "",
    var createdAt: Long = 0L,
    var completedBy: String = "",
    var completedAt: Long = 0L,
) : HasId {
    @get:Exclude val done: Boolean get() = status == TaskStatus.DONE
}

// ---------- Agenda ----------
data class EventItem(
    @get:Exclude override var id: String = "",
    var title: String = "",
    var description: String = "",
    var location: String = "",
    var dateMillis: Long = 0L,
    var reminderMinutes: Int = 60, // -1 = sem lembrete
    var createdBy: String = "",
    var createdAt: Long = 0L,
) : HasId

// ---------- Ideias ----------
data class Idea(
    @get:Exclude override var id: String = "",
    var title: String = "",
    var description: String = "",
    var category: String = "Geral",
    var likes: List<String> = emptyList(),
    var approved: Boolean = false,
    var createdBy: String = "",
    var createdAt: Long = 0L,
) : HasId

// ---------- Notas ----------
data class Note(
    @get:Exclude override var id: String = "",
    var title: String = "",
    var content: String = "",
    var color: Int = 0,
    var pinned: Boolean = false,
    var createdBy: String = "",
    var createdAt: Long = 0L,
    var updatedAt: Long = 0L,
) : HasId

// ---------- Lugares ----------
object PlaceStatus {
    const val OPTION = "opcao"
    const val SELECTED = "selecionado"
    const val DISCARDED = "descartado"
    val all = listOf(OPTION, SELECTED, DISCARDED)
    fun label(s: String) = when (s) {
        OPTION -> "Opção"
        SELECTED -> "Escolhido"
        DISCARDED -> "Descartado"
        else -> s
    }
}

data class Place(
    @get:Exclude override var id: String = "",
    var name: String = "",
    var address: String = "",
    var category: String = "Restaurante",
    var price: String = "$$",
    var link: String = "",
    var phone: String = "",
    var notes: String = "",
    var rating: Int = 0,
    var status: String = PlaceStatus.OPTION,
    var likes: List<String> = emptyList(),
    var createdBy: String = "",
    var createdAt: Long = 0L,
) : HasId

// ---------- Convidados ----------
object GuestStatus {
    const val PENDING = "pendente"
    const val INVITED = "convidado"
    const val CONFIRMED = "confirmado"
    const val DECLINED = "nao_vai"
    val all = listOf(PENDING, INVITED, CONFIRMED, DECLINED)
    fun label(s: String) = when (s) {
        PENDING -> "Falta convidar"
        INVITED -> "Convidado"
        CONFIRMED -> "Confirmado"
        DECLINED -> "Não vai"
        else -> s
    }
}

data class Guest(
    @get:Exclude override var id: String = "",
    var name: String = "",
    var phone: String = "",
    var group: String = "Amigos",
    var status: String = GuestStatus.PENDING,
    var plusOnes: Int = 0,
    var notes: String = "",
    var createdBy: String = "",
    var createdAt: Long = 0L,
) : HasId

// ---------- Orçamento ----------
data class Expense(
    @get:Exclude override var id: String = "",
    var title: String = "",
    var category: String = "Outros",
    var amount: Double = 0.0,
    var paid: Boolean = false,
    var paidBy: String = Users.BOTH,
    var createdBy: String = "",
    var createdAt: Long = 0L,
) : HasId

// ---------- Configuração da festa ----------
data class PartyConfig(
    var title: String = "Aniversário da Baixinha",
    var birthdayMillis: Long = 0L,
    var partyMillis: Long = 0L,
    var budget: Double = 0.0,
    var theme: String = "",
)

object Defaults {
    val taskCategories = listOf(
        "Local", "Convidados", "Comida & Bebida", "Bolo & Doces", "Decoração",
        "Música", "Look & Beleza", "Fotos", "Presentes", "Transporte", "Outros",
    )
    val ideaCategories = listOf("Geral", "Tema", "Decoração", "Comida", "Brincadeiras", "Look", "Surpresa")
    val placeCategories = listOf("Restaurante", "Bar", "Salão", "Casa", "Ao ar livre", "Viagem", "Outro")
    val guestGroups = listOf("Família", "Amigos", "Trabalho", "Faculdade", "Outros")
    val expenseCategories = listOf("Local", "Comida", "Bebida", "Bolo", "Decoração", "Look", "Presentes", "Outros")

    /** Plano inicial sugerido (pode ser gerado com um toque na tela de tarefas). */
    val starterPlan: List<Triple<String, String, Int>> = listOf(
        Triple("Definir a data e o horário", "Local", 1),
        Triple("Escolher o local da festa", "Local", 1),
        Triple("Reservar/confirmar o local", "Local", 1),
        Triple("Fazer a lista de convidados", "Convidados", 1),
        Triple("Enviar os convites", "Convidados", 0),
        Triple("Cobrar confirmações", "Convidados", 0),
        Triple("Definir cardápio", "Comida & Bebida", 0),
        Triple("Comprar bebidas", "Comida & Bebida", 0),
        Triple("Encomendar o bolo", "Bolo & Doces", 1),
        Triple("Encomendar docinhos", "Bolo & Doces", 0),
        Triple("Escolher tema/paleta", "Decoração", 0),
        Triple("Comprar balões e decoração", "Decoração", 0),
        Triple("Montar playlist", "Música", 0),
        Triple("Escolher o look", "Look & Beleza", 0),
        Triple("Agendar cabelo/maquiagem", "Look & Beleza", 0),
        Triple("Combinar fotos (celular/fotógrafo)", "Fotos", 0),
    )
}
