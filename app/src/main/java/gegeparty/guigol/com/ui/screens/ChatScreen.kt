package gegeparty.guigol.com.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gegeparty.guigol.com.data.ChatMessage
import gegeparty.guigol.com.data.Collections
import gegeparty.guigol.com.data.Users
import gegeparty.guigol.com.ui.Fmt
import gegeparty.guigol.com.ui.MainViewModel
import gegeparty.guigol.com.ui.theme.Gege
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val quickReactions = listOf("❤️", "😂", "😍", "🥳", "👍", "😮", "😢")
private val emojiPalette = listOf(
    "💗", "💙", "💞", "😍", "🥰", "😘", "😂", "🤣", "😊", "😉", "🥺", "😎",
    "🤩", "😮", "😢", "😡", "🙏", "👍", "👏", "🙌", "💪", "🤝", "✨", "🔥",
    "🎉", "🥳", "🎂", "🎈", "🎁", "🍰", "🧁", "🍾", "🥂", "🍕", "🍔", "🍫",
    "💃", "🕺", "🎶", "📸", "💄", "👗", "👠", "💐", "🌹", "🌈", "☀️", "⭐",
    "📍", "📅", "✅", "❌", "⏰", "💰", "🤔", "🙈", "😴", "💤", "🤫", "👀",
)

private fun isOnlyEmoji(s: String): Boolean {
    val t = s.trim()
    if (t.isEmpty() || t.length > 16) return false
    return t.none { it.isLetterOrDigit() || it.isWhitespace() || it in ".,!?;:'\"()-" }
}

@Composable
fun ChatScreen(vm: MainViewModel, open: (String) -> Unit) {
    val messages by vm.messages.collectAsStateWithLifecycle()
    val typingMap by vm.typing.collectAsStateWithLifecycle()
    val me = vm.me
    val other = Users.other(me)
    var text by rememberSaveable { mutableStateOf("") }
    var showEmojis by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Relógio para o "digitando…" expirar sozinho
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { delay(1000); now = System.currentTimeMillis() } }
    val otherTyping = (typingMap[other] ?: 0L).let { it > 0 && now - it < 6_000 }

    androidx.compose.runtime.DisposableEffect(Unit) {
        gegeparty.guigol.com.notifications.AppState.onChat = true
        onDispose { gegeparty.guigol.com.notifications.AppState.onChat = false; vm.onTyping("") }
    }

    // Marca como lidas enquanto a tela está aberta
    LaunchedEffect(messages.size) {
        vm.markAllRead()
        if (listState.firstVisibleItemIndex <= 2) listState.animateScrollToItem(0)
    }

    val reversed = messages.asReversed()

    fun send(t: String = text) {
        if (t.isBlank()) return
        vm.sendMessage(t)
        if (t == text) text = ""
        scope.launch { listState.animateScrollToItem(0) }
    }

    Column(Modifier.fillMaxSize().background(Gege.Bg).imePadding()) {
        // ---------- Topo ----------
        Surface(color = Color.White, shadowElevation = 2.dp) {
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(Gege.userSoft(other)),
                    contentAlignment = Alignment.Center
                ) { Text(Users.emoji(other), fontSize = 22.sp) }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(Users.display(other), style = MaterialTheme.typography.titleMedium)
                    AnimatedVisibility(otherTyping) {
                        Text("digitando…", style = MaterialTheme.typography.labelMedium, color = Gege.Pink)
                    }
                    AnimatedVisibility(!otherTyping) {
                        Text("Chat da festa 🎈", style = MaterialTheme.typography.labelMedium, color = Gege.InkSoft)
                    }
                }
            }
        }

        // ---------- Mensagens ----------
        Box(Modifier.weight(1f)) {
            if (messages.isEmpty()) {
                Column(
                    Modifier.fillMaxSize().padding(32.dp),
                    verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("💌", fontSize = 56.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Comecem a conversa!", style = MaterialTheme.typography.titleLarge)
                    Text("Toque num emoji para mandar rapidinho", color = Gege.InkSoft)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("💗", "🥳", "🎂", "😘").forEach { e ->
                            Surface(onClick = { send(e) }, shape = CircleShape, color = Color.White) {
                                Text(e, fontSize = 28.sp, modifier = Modifier.padding(12.dp))
                            }
                        }
                    }
                }
            }
            LazyColumn(
                state = listState, reverseLayout = true,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            ) {
                itemsIndexed(reversed, key = { _, m -> m.id }) { i, m ->
                    val older = reversed.getOrNull(i + 1)
                    val newer = reversed.getOrNull(i - 1)
                    val newDay = older == null || Fmt.localDate(older.createdAt) != Fmt.localDate(m.createdAt)
                    val groupedWithNewer = newer != null && newer.sender == m.sender &&
                        newer.createdAt - m.createdAt < 120_000 && Fmt.localDate(newer.createdAt) == Fmt.localDate(m.createdAt)
                    Column {
                        if (newDay) DayChip(m.createdAt)
                        Bubble(
                            m = m, mine = m.sender == me, me = me, other = other,
                            isLastInGroup = !groupedWithNewer,
                            onReact = { vm.react(m, it) },
                            onDelete = { vm.delete(Collections.MESSAGES, m.id) },
                            onOpen = open,
                        )
                        Spacer(Modifier.height(if (groupedWithNewer) 2.dp else 8.dp))
                    }
                }
            }
        }

        // ---------- Barra de emojis ----------
        AnimatedVisibility(showEmojis, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Surface(color = Color.White) {
                Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    emojiPalette.chunked(12).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            row.forEach { e ->
                                Text(
                                    e, fontSize = 24.sp,
                                    modifier = Modifier.clip(CircleShape).clickable { text += e }.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ---------- Entrada ----------
        Surface(color = Color.White, shadowElevation = 8.dp) {
            Column {
                if (!showEmojis) Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(start = 12.dp, end = 12.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("💗", "😂", "🥳", "👍", "🎉", "😍", "🙏", "🔥").forEach { e ->
                        Surface(onClick = { send(e) }, shape = RoundedCornerShape(50), color = Gege.Bg) {
                            Text(e, fontSize = 20.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.Bottom) {
                    IconButton(onClick = { showEmojis = !showEmojis }) {
                        Icon(if (showEmojis) Icons.Outlined.Keyboard else Icons.Outlined.EmojiEmotions, "Emojis", tint = Gege.Pink)
                    }
                    TextField(
                        value = text,
                        onValueChange = { text = it; vm.onTyping(it) },
                        placeholder = { Text("Mensagem…") },
                        modifier = Modifier.weight(1f),
                        maxLines = 5,
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Gege.Bg, unfocusedContainerColor = Gege.Bg,
                            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                        ),
                    )
                    Spacer(Modifier.size(6.dp))
                    AnimatedVisibility(text.isNotBlank(), enter = scaleIn() + fadeIn(), exit = fadeOut()) {
                        Surface(onClick = { send() }, shape = CircleShape, color = Gege.Pink, modifier = Modifier.size(52.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.Send, "Enviar", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayChip(ms: Long) {
    Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(50), color = Color.White) {
            Text(
                Fmt.relativeDay(ms).let { if (it == Fmt.date(ms)) Fmt.dateFull(ms) else it },
                style = MaterialTheme.typography.labelMedium, color = Gege.InkSoft,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Bubble(
    m: ChatMessage, mine: Boolean, me: String, other: String, isLastInGroup: Boolean,
    onReact: (String) -> Unit, onDelete: () -> Unit, onOpen: (String) -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val ctx = LocalContext.current
    val big = m.attachTitle.isBlank() && isOnlyEmoji(m.text)

    val shape = RoundedCornerShape(
        topStart = 20.dp, topEnd = 20.dp,
        bottomStart = if (!mine && isLastInGroup) 4.dp else 20.dp,
        bottomEnd = if (mine && isLastInGroup) 4.dp else 20.dp,
    )

    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
        Column(horizontalAlignment = if (mine) Alignment.End else Alignment.Start, modifier = Modifier.widthIn(max = 300.dp)) {
            Box {
                val clickMod = Modifier.combinedClickable(
                    onClick = { if (m.attachRoute.isNotBlank()) onOpen(m.attachRoute) },
                    onDoubleClick = { onReact("❤️") },
                    onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); menu = true },
                )
                if (big) {
                    Text(m.text, fontSize = 44.sp, modifier = Modifier.clip(RoundedCornerShape(12.dp)).then(clickMod))
                } else {
                    Box(
                        Modifier.clip(shape)
                            .background(
                                if (mine) Brush.linearGradient(listOf(Gege.Pink, Gege.Coral))
                                else Brush.linearGradient(listOf(Color.White, Color.White))
                            )
                            .then(clickMod)
                            .animateContentSize()
                    ) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 9.dp)) {
                            if (m.attachTitle.isNotBlank()) {
                                Surface(shape = RoundedCornerShape(12.dp), color = if (mine) Color.White.copy(alpha = .22f) else Gege.YellowSoft) {
                                    Text(
                                        m.attachTitle, style = MaterialTheme.typography.labelLarge,
                                        color = if (mine) Color.White else Gege.Ink,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                                if (m.text.isNotBlank()) Spacer(Modifier.height(6.dp))
                            }
                            if (m.text.isNotBlank()) Text(
                                m.text, style = MaterialTheme.typography.bodyLarge,
                                color = if (mine) Color.White else Gege.Ink
                            )
                        }
                    }
                }

                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }, shape = RoundedCornerShape(20.dp), containerColor = Color.White) {
                    Row(Modifier.padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        quickReactions.forEach { e ->
                            Text(
                                e, fontSize = 26.sp,
                                modifier = Modifier.clip(CircleShape)
                                    .background(if (m.reactions[me] == e) Gege.PinkSoft else Color.Transparent)
                                    .clickable { onReact(e); menu = false }.padding(6.dp)
                            )
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 4.dp), color = Gege.Line)
                    if (m.text.isNotBlank()) DropdownMenuItem(
                        text = { Text("Copiar") }, leadingIcon = { Icon(Icons.Outlined.ContentCopy, null) },
                        onClick = {
                            (ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                                .setPrimaryClip(ClipData.newPlainText("mensagem", m.text))
                            menu = false
                        }
                    )
                    if (mine) DropdownMenuItem(
                        text = { Text("Apagar", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { onDelete(); menu = false }
                    )
                }
            }

            // Reações
            if (m.reactions.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(50), color = Color.White, shadowElevation = 1.dp,
                    modifier = Modifier.padding(top = 2.dp, start = 6.dp, end = 6.dp).clickable { menu = true }
                ) {
                    Text(
                        m.reactions.values.groupingBy { it }.eachCount().entries
                            .joinToString(" ") { (e, n) -> if (n > 1) "$e$n" else e },
                        fontSize = 15.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Hora + lido
            if (isLastInGroup) {
                Row(Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(Fmt.time(m.createdAt), style = MaterialTheme.typography.labelSmall, color = Gege.InkSoft)
                    if (mine) {
                        val read = other in m.readBy
                        Text(
                            if (read) " ✓✓" else " ✓", style = MaterialTheme.typography.labelSmall,
                            color = if (read) Gege.Pink else Gege.InkSoft
                        )
                    }
                }
            }
        }
    }
}
