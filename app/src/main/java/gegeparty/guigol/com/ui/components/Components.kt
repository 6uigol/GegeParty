package gegeparty.guigol.com.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import gegeparty.guigol.com.data.Users
import gegeparty.guigol.com.ui.Fmt
import gegeparty.guigol.com.ui.theme.Gege
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GegeCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .then(
                if (onClick != null || onLongClick != null)
                    Modifier.combinedClickable(onClick = { onClick?.invoke() }, onLongClick = onLongClick)
                else Modifier
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = color),
        border = BorderStroke(1.dp, Gege.Line),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
fun EmptyState(emoji: String, title: String, subtitle: String, modifier: Modifier = Modifier, action: String? = null, onAction: (() -> Unit)? = null) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(88.dp).clip(CircleShape).background(Gege.PinkSoft),
            contentAlignment = Alignment.Center
        ) { Text(emoji, style = MaterialTheme.typography.displaySmall) }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (action != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
fun UserBadge(user: String, modifier: Modifier = Modifier, compact: Boolean = false) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = Gege.userSoft(user),
    ) {
        Text(
            if (compact) Users.emoji(user) else "${Users.emoji(user)} ${Users.display(user)}",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Gege.userColor(user),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun Pill(text: String, color: Color, bg: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(50), color = bg) {
        Text(
            text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ChipRow(
    options: List<T>,
    selected: T?,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { o ->
            FilterChip(
                selected = o == selected,
                onClick = { onSelect(o) },
                label = { Text(label(o)) },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Gege.Pink,
                    selectedLabelColor = Color.White,
                ),
            )
        }
    }
}

@Composable
fun ProgressRing(progress: Float, modifier: Modifier = Modifier, size: Dp = 96.dp, stroke: Dp = 10.dp, color: Color = Color.White, track: Color = Color.White.copy(alpha = .3f), content: @Composable () -> Unit = {}) {
    val p by animateFloatAsState(progress.coerceIn(0f, 1f), tween(900), label = "ring")
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val s = Stroke(width = stroke.toPx(), cap = StrokeCap.Round)
            drawArc(track, -90f, 360f, false, style = s)
            drawArc(color, -90f, 360f * p, false, style = s)
        }
        content()
    }
}

@Composable
fun SoftProgress(progress: Float, color: Color, modifier: Modifier = Modifier) {
    val p by animateFloatAsState(progress.coerceIn(0f, 1f), tween(700), label = "bar")
    LinearProgressIndicator(
        progress = { p },
        modifier = modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
        color = color,
        trackColor = color.copy(alpha = .15f),
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
        drawStopIndicator = {},
    )
}

@Composable
fun GegeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboard: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = modifier.fillMaxWidth(), singleLine = singleLine, minLines = minLines,
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Gege.Line, focusedBorderColor = Gege.Pink,
            unfocusedContainerColor = Color.White, focusedContainerColor = Color.White,
        ),
    )
}

/** Bottom sheet de formulário padrão, com título, conteúdo rolável e botões. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormSheet(
    title: String,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean = true,
    onDelete: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var confirmDelete by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state, containerColor = Gege.Bg) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .imePadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                if (onDelete != null) {
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Outlined.Delete, "Excluir", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            content()
            Button(
                onClick = onSave, enabled = saveEnabled,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Gege.Pink),
            ) { Text("Salvar", style = MaterialTheme.typography.titleMedium) }
            Spacer(Modifier.height(16.dp))
        }
    }
    if (confirmDelete && onDelete != null) {
        ConfirmDialog("Excluir?", "Essa ação não pode ser desfeita.", onConfirm = { confirmDelete = false; onDelete() }, onDismiss = { confirmDelete = false })
    }
}

@Composable
fun ConfirmDialog(title: String, text: String, onConfirm: () -> Unit, onDismiss: () -> Unit, confirmLabel: String = "Excluir") {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel, color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
fun FieldLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** Seletor de pessoa responsável. */
@Composable
fun AssigneePicker(selected: String, onSelect: (String) -> Unit, includeBoth: Boolean = true) {
    val opts = if (includeBoth) listOf(Users.BAIXINHA, Users.QUERIDO, Users.BOTH) else Users.all
    ChipRow(opts, selected, { "${Users.emoji(it)} ${Users.display(it)}" }, onSelect)
}

/** Campo de data (e opcionalmente hora) com pickers do Material 3. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeField(
    label: String,
    millis: Long,
    onChange: (Long) -> Unit,
    withTime: Boolean = true,
    onClear: (() -> Unit)? = null,
) {
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var pickedDate by remember { mutableStateOf<LocalDate?>(null) }
    val zone = ZoneId.systemDefault()

    Surface(
        onClick = { showDate = true },
        shape = MaterialTheme.shapes.medium,
        color = Color.White,
        border = BorderStroke(1.dp, Gege.Line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = Gege.Pink)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    when {
                        millis <= 0 -> "Toque para escolher"
                        withTime -> Fmt.dateFull(millis) + " • " + Fmt.time(millis)
                        else -> Fmt.dateFull(millis)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            if (onClear != null && millis > 0) {
                TextButton(onClick = onClear) { Text("Limpar") }
            }
        }
    }

    if (showDate) {
        val initialUtc = if (millis > 0)
            Fmt.localDate(millis).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli() else null
        val ds = rememberDatePickerState(initialSelectedDateMillis = initialUtc)
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    ds.selectedDateMillis?.let { sel ->
                        val d = Instant.ofEpochMilli(sel).atZone(ZoneOffset.UTC).toLocalDate()
                        if (withTime) {
                            pickedDate = d; showTime = true
                        } else {
                            onChange(d.atTime(12, 0).atZone(zone).toInstant().toEpochMilli())
                        }
                    }
                    showDate = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("Cancelar") } },
        ) { DatePicker(state = ds) }
    }

    if (showTime && pickedDate != null) {
        val cur = if (millis > 0) Instant.ofEpochMilli(millis).atZone(zone).toLocalTime() else LocalTime.of(19, 0)
        val ts = rememberTimePickerState(cur.hour, cur.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTime = false },
            confirmButton = {
                TextButton(onClick = {
                    val dt = LocalDateTime.of(pickedDate, LocalTime.of(ts.hour, ts.minute))
                    onChange(dt.atZone(zone).toInstant().toEpochMilli())
                    showTime = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTime = false }) { Text("Cancelar") } },
            text = { TimePicker(state = ts) },
        )
    }
}

@Composable
fun StarRating(rating: Int, onChange: ((Int) -> Unit)? = null, size: Dp = 22.dp) {
    Row {
        (1..5).forEach { i ->
            Text(
                if (i <= rating) "★" else "☆",
                color = if (i <= rating) Gege.Yellow else Gege.Line,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(end = 2.dp)
                    .then(if (onChange != null) Modifier.clickable { onChange(if (rating == i) 0 else i) } else Modifier),
            )
        }
    }
}
