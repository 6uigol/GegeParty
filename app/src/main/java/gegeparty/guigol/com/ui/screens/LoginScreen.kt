package gegeparty.guigol.com.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import gegeparty.guigol.com.data.Users
import gegeparty.guigol.com.ui.MainViewModel
import gegeparty.guigol.com.ui.theme.Gege

@Composable
fun LoginScreen(vm: MainViewModel) {
    var user by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val focus = LocalFocusManager.current

    val bounce = rememberInfiniteTransition(label = "b")
    val y by bounce.animateFloat(0f, -14f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "y")

    fun submit() {
        if (user.isBlank() || pass.isBlank() || loading) return
        focus.clearFocus(); loading = true; error = null
        vm.login(user, pass) { err -> loading = false; error = err }
    }

    Box(Modifier.fillMaxSize().background(Gege.Hero)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.height(48.dp))
            Text("🎈", style = MaterialTheme.typography.displayLarge, modifier = Modifier.graphicsLayer { translationY = y })
            Text("GegeParty", style = MaterialTheme.typography.displaySmall, color = Color.White)
            Text(
                "A festa mais organizada do mundo 💗", color = Color.White.copy(alpha = .9f),
                style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            Surface(shape = MaterialTheme.shapes.extraLarge, color = Color.White, shadowElevation = 12.dp) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Quem está entrando?", style = MaterialTheme.typography.titleLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Users.all.forEach { u ->
                            val sel = user.trim().lowercase() == u
                            Surface(
                                onClick = { user = u; error = null },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.large,
                                color = if (sel) Gege.userColor(u) else Gege.userSoft(u),
                            ) {
                                Column(Modifier.padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = if (sel) .25f else 1f)),
                                        contentAlignment = Alignment.Center
                                    ) { Text(Users.emoji(u), style = MaterialTheme.typography.headlineSmall) }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        Users.display(u), style = MaterialTheme.typography.titleMedium,
                                        color = if (sel) Color.White else Gege.userColor(u)
                                    )
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = pass, onValueChange = { pass = it; error = null },
                        label = { Text("Senha") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        trailingIcon = {
                            IconButton(onClick = { show = !show }) {
                                Icon(if (show) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, "Mostrar senha")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gege.Pink, unfocusedBorderColor = Gege.Line),
                    )
                    AnimatedVisibility(error != null) {
                        Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(
                        onClick = ::submit,
                        enabled = user.isNotBlank() && pass.isNotBlank() && !loading,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = Gege.Pink),
                    ) {
                        if (loading) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                        else Text("Entrar na festa", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}
