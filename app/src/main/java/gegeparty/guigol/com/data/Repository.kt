package gegeparty.guigol.com.data

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Acesso ao Firebase. Login usa Email/Senha por baixo, mas o usuário só digita "baixinha" ou "querido". */
object AuthRepository {
    private val auth get() = Firebase.auth

    val currentUser: String? get() = auth.currentUser?.email?.let { Users.usernameOf(it) }

    fun authState(): Flow<String?> = callbackFlow {
        val l = com.google.firebase.auth.FirebaseAuth.AuthStateListener { a ->
            trySend(a.currentUser?.email?.let { Users.usernameOf(it) })
        }
        auth.addAuthStateListener(l)
        awaitClose { auth.removeAuthStateListener(l) }
    }

    suspend fun login(username: String, password: String) {
        auth.signInWithEmailAndPassword(Users.emailOf(username), password).await()
    }

    fun logout() = auth.signOut()
}

object Repo {
    val db get() = Firebase.firestore

    fun <T : HasId> listen(
        collection: String,
        clazz: Class<T>,
        orderBy: String = "createdAt",
        direction: Query.Direction = Query.Direction.DESCENDING,
    ): Flow<List<T>> = callbackFlow {
        val reg = db.collection(collection).orderBy(orderBy, direction)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener
                trySend(snap.documents.mapNotNull { d -> d.toObject(clazz)?.also { it.id = d.id } })
            }
        awaitClose { reg.remove() }
    }

    fun listenConfig(): Flow<PartyConfig> = callbackFlow {
        val reg = db.collection(Collections.CONFIG).document(Collections.PARTY_DOC)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObject(PartyConfig::class.java) ?: PartyConfig())
            }
        awaitClose { reg.remove() }
    }

    /** Cria (id vazio) ou atualiza (id preenchido). Escritas funcionam offline e sincronizam depois. */
    fun save(collection: String, item: HasId) {
        val col = db.collection(collection)
        if (item.id.isBlank()) col.add(item) else col.document(item.id).set(item)
    }

    fun update(collection: String, id: String, fields: Map<String, Any?>) {
        db.collection(collection).document(id).update(fields)
    }

    fun delete(collection: String, id: String) {
        db.collection(collection).document(id).delete()
    }

    fun toggleLike(collection: String, id: String, user: String, liked: Boolean) {
        val v = if (liked) FieldValue.arrayRemove(user) else FieldValue.arrayUnion(user)
        db.collection(collection).document(id).update("likes", v)
    }

    fun saveConfig(config: PartyConfig) {
        db.collection(Collections.CONFIG).document(Collections.PARTY_DOC).set(config, SetOptions.merge())
    }

    fun seedStarterPlan(user: String) {
        val batch = db.batch()
        val now = System.currentTimeMillis()
        Defaults.starterPlan.forEachIndexed { i, (title, cat, prio) ->
            val ref = db.collection(Collections.TASKS).document()
            batch.set(
                ref, TaskItem(
                    title = title, category = cat, priority = prio,
                    createdBy = user, createdAt = now - i, // mantém a ordem
                )
            )
        }
        batch.commit()
    }
}
