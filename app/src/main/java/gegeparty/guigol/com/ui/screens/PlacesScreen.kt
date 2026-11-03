package gegeparty.guigol.com.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gegeparty.guigol.com.data.Collections
import gegeparty.guigol.com.data.Defaults
import gegeparty.guigol.com.data.Place
import gegeparty.guigol.com.data.PlaceStatus
import gegeparty.guigol.com.data.Users
import gegeparty.guigol.com.ui.MainViewModel
import gegeparty.guigol.com.ui.components.AddFab
import gegeparty.guigol.com.ui.components.ChipRow
import gegeparty.guigol.com.ui.components.EmptyState
import gegeparty.guigol.com.ui.components.FieldLabel
import gegeparty.guigol.com.ui.components.FormSheet
import gegeparty.guigol.com.ui.components.GegeCard
import gegeparty.guigol.com.ui.components.GegeField
import gegeparty.guigol.com.ui.components.Pill
import gegeparty.guigol.com.ui.components.ScreenHeader
import gegeparty.guigol.com.ui.components.StarRating
import gegeparty.guigol.com.ui.theme.Gege

@Composable
fun PlacesScreen(vm: MainViewModel, onBack: (() -> Unit)? = null) {
    val places by vm.places.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf("todos") }
    var editing by remember { mutableStateOf<Place?>(null) }
    val listState = rememberLazyListState()
    val fab by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
    val ctx = LocalContext.current

    val statusOrder = mapOf(PlaceStatus.SELECTED to 0, PlaceStatus.OPTION to 1, PlaceStatus.DISCARDED to 2)
    val list = places.filter { filter == "todos" || it.status == filter }
        .sortedWith(compareBy<Place> { statusOrder[it.status] ?: 3 }.thenByDescending { it.likes.size }.thenByDescending { it.rating })

    fun launch(uri: String) = runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri))) }

    Scaffold(containerColor = Gege.Bg, contentWindowInsets = WindowInsets(0, 0, 0, 0), floatingActionButton = { AddFab("Novo lugar", { editing = Place() }, fab) }) { pad ->
        LazyColumn(
            state = listState, modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                val sel = places.count { it.status == PlaceStatus.SELECTED }
                ScreenHeader("Lugares", if (sel > 0) "$sel escolhido(s) • ${places.size} no total" else "Compare opções e escolham juntos", onBack)
                if (places.isNotEmpty()) ChipRow(
                    listOf("todos") + PlaceStatus.all, filter,
                    { if (it == "todos") "Todos" else PlaceStatus.label(it) }, { filter = it },
                    Modifier.padding(horizontal = 20.dp)
                )
            }
            if (places.isEmpty()) item {
                EmptyState("📍", "Nenhum lugar ainda", "Salve restaurantes, salões e bares. Curtam juntos e escolham o favorito!", action = "Adicionar lugar", onAction = { editing = Place() })
            }
            items(list, key = { it.id }) { p ->
                val me = vm.me
                val liked = me in p.likes
                val both = Users.all.all { it in p.likes }
                GegeCard(
                    Modifier.padding(horizontal = 20.dp).alpha(if (p.status == PlaceStatus.DISCARDED) .55f else 1f),
                    color = if (p.status == PlaceStatus.SELECTED) Gege.MintSoft else MaterialTheme.colorScheme.surface,
                    onClick = { editing = p },
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Pill(p.category, Gege.Coral, Gege.YellowSoft)
                                if (p.price.isNotBlank()) Pill(p.price, Gege.InkSoft, Gege.Bg)
                                if (p.status == PlaceStatus.SELECTED) Pill("✓ Escolhido", Gege.Mint, androidx.compose.ui.graphics.Color.White)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(p.name, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            if (p.address.isNotBlank()) Text(p.address, style = MaterialTheme.typography.bodyMedium, color = Gege.InkSoft, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            if (p.rating > 0) StarRating(p.rating)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { vm.toggleLike(Collections.PLACES, p.id, p.likes) }) {
                                Icon(if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, "Curtir", tint = Gege.Pink)
                            }
                            Text(p.likes.joinToString("") { Users.emoji(it) }, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    if (both) Text("💞 Vocês dois amaram!", style = MaterialTheme.typography.labelLarge, color = Gege.Pink, modifier = Modifier.padding(top = 6.dp))
                    if (p.notes.isNotBlank()) Text(p.notes, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (p.address.isNotBlank()) SmallAction(Icons.Outlined.Map, "Mapa") { launch("geo:0,0?q=" + Uri.encode(p.address)) }
                        if (p.link.isNotBlank()) SmallAction(Icons.Outlined.Link, "Link") { launch(if (p.link.startsWith("http")) p.link else "https://${p.link}") }
                        if (p.phone.isNotBlank()) SmallAction(Icons.Outlined.Call, "Ligar") { launch("tel:${p.phone}") }
                        SmallAction(null, "💬") { vm.sendMessage("O que acha desse lugar?", "📍 ${p.name}", "places") }
                        Spacer(Modifier.weight(1f))
                        when (p.status) {
                            PlaceStatus.SELECTED -> SmallAction(null, "Desfazer") { vm.update(Collections.PLACES, p.id, "status" to PlaceStatus.OPTION) }
                            PlaceStatus.DISCARDED -> SmallAction(null, "Reconsiderar") { vm.update(Collections.PLACES, p.id, "status" to PlaceStatus.OPTION) }
                            else -> {
                                SmallAction(null, "✕") { vm.update(Collections.PLACES, p.id, "status" to PlaceStatus.DISCARDED) }
                                SmallAction(null, "Escolher ✓") { vm.update(Collections.PLACES, p.id, "status" to PlaceStatus.SELECTED) }
                            }
                        }
                    }
                }
            }
        }
    }

    editing?.let { p ->
        PlaceSheet(p, onDismiss = { editing = null }, onSave = { vm.savePlace(it); editing = null },
            onDelete = if (p.id.isNotBlank()) ({ vm.delete(Collections.PLACES, p.id); editing = null }) else null)
    }
}

@Composable
private fun SmallAction(icon: androidx.compose.ui.graphics.vector.ImageVector?, label: String, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick, label = { Text(label) },
        leadingIcon = icon?.let { { Icon(it, null, Modifier.width(18.dp)) } },
        shape = MaterialTheme.shapes.small,
        colors = AssistChipDefaults.assistChipColors(leadingIconContentColor = Gege.Pink),
    )
}

@Composable
private fun PlaceSheet(initial: Place, onDismiss: () -> Unit, onSave: (Place) -> Unit, onDelete: (() -> Unit)?) {
    var name by remember { mutableStateOf(initial.name) }
    var address by remember { mutableStateOf(initial.address) }
    var category by remember { mutableStateOf(initial.category) }
    var price by remember { mutableStateOf(initial.price) }
    var link by remember { mutableStateOf(initial.link) }
    var phone by remember { mutableStateOf(initial.phone) }
    var notes by remember { mutableStateOf(initial.notes) }
    var rating by remember { mutableStateOf(initial.rating) }
    var status by remember { mutableStateOf(initial.status) }

    FormSheet(
        if (initial.id.isBlank()) "Novo lugar" else "Editar lugar", onDismiss = onDismiss,
        saveEnabled = name.isNotBlank(), onDelete = onDelete,
        onSave = {
            onSave(initial.copy(name = name.trim(), address = address.trim(), category = category, price = price,
                link = link.trim(), phone = phone.trim(), notes = notes.trim(), rating = rating, status = status))
        },
    ) {
        GegeField(name, { name = it }, "Nome do lugar")
        FieldLabel("Tipo")
        ChipRow(Defaults.placeCategories, category, { it }, { category = it })
        FieldLabel("Preço")
        ChipRow(listOf("$", "$$", "$$$", "$$$$"), price, { it }, { price = it })
        FieldLabel("Nota")
        StarRating(rating, { rating = it })
        FieldLabel("Status")
        ChipRow(PlaceStatus.all, status, { PlaceStatus.label(it) }, { status = it })
        GegeField(address, { address = it }, "Endereço")
        GegeField(link, { link = it }, "Link (Instagram, site, cardápio…)", keyboard = KeyboardType.Uri)
        GegeField(phone, { phone = it }, "Telefone", keyboard = KeyboardType.Phone)
        GegeField(notes, { notes = it }, "Prós, contras, capacidade…", singleLine = false, minLines = 3)
    }
}
