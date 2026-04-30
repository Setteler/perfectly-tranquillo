package com.methoda.tranquillo.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.data.AppViewModel
import com.methoda.tranquillo.data.PrefsStore
import com.methoda.tranquillo.ui.theme.Dimens
import com.methoda.tranquillo.ui.theme.Palettes

@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val name by viewModel.userName.collectAsState()
    val paletteId by viewModel.palette.collectAsState()
    val notifMode by viewModel.notifMode.collectAsState()
    val sound by viewModel.soundEnabled.collectAsState()
    val ambient by viewModel.ambientSound.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = Dimens.ScreenHorizontalPadding,
                vertical = Dimens.ScreenVerticalPadding
            )
    ) {
        SettingsHeader()
        Spacer(Modifier.height(14.dp))

        SettingsSection(title = "About you") {
            UserNameField(value = name, onCommit = viewModel::setUserName)
        }
        Spacer(Modifier.height(14.dp))

        SettingsSection(title = "Appearance") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (p in Palettes.all) {
                    PaletteCard(
                        palette = p,
                        selected = p.id == paletteId,
                        onClick = { viewModel.setPalette(p.id) }
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        SettingsSection(title = "Notifications") {
            NotifModeRow(
                label = "Silent",
                hint = "no reminders fire",
                selected = notifMode == PrefsStore.NOTIF_SILENT,
                onClick = { viewModel.setNotifMode(PrefsStore.NOTIF_SILENT) }
            )
            NotifModeRow(
                label = "Sound",
                hint = "default chime + vibration",
                selected = notifMode == PrefsStore.NOTIF_SOUND,
                onClick = { viewModel.setNotifMode(PrefsStore.NOTIF_SOUND) }
            )
            NotifModeRow(
                label = "Vibrate",
                hint = "vibration only (system permitting)",
                selected = notifMode == PrefsStore.NOTIF_VIBRATE,
                onClick = { viewModel.setNotifMode(PrefsStore.NOTIF_VIBRATE) }
            )
            Spacer(Modifier.height(12.dp))
            ActionRow(
                label = "Send a sample reminder",
                hint = "see how it looks",
                onClick = { viewModel.fireDemoReminder() }
            )
        }
        Spacer(Modifier.height(14.dp))

        SettingsSection(title = "Sound") {
            ToggleRow(
                label = "Ambient sound",
                hint = "loops a calm background while the app is open",
                checked = sound,
                onChange = viewModel::setSoundEnabled
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "PICK A SOUND",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            for (option in AmbientOptions) {
                AmbientRow(
                    label = option.label,
                    hint = option.hint,
                    selected = ambient == option.id,
                    onClick = { viewModel.setAmbientSound(option.id) }
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "audio plays once you've added the sound files (see project /res/raw)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        Spacer(Modifier.height(14.dp))

        SettingsSection(title = "Data") {
            ActionRow(
                label = "Reset today",
                hint = "clears today's mandala fills + habit ticks",
                onClick = { showResetDialog = true }
            )
            Spacer(Modifier.height(8.dp))
            ActionRow(
                label = "Clear all",
                hint = "wipes everything, restores default habits",
                onClick = { showClearAllDialog = true },
                destructive = true
            )
        }

        Spacer(Modifier.height(Dimens.ScreenBottomClearance))
    }

    if (showResetDialog) {
        ConfirmDialog(
            title = "Reset today?",
            body = "This clears today's mandala entries, habit ticks, and action stones earned today. Past days are kept.",
            confirmLabel = "Reset",
            onConfirm = {
                viewModel.resetToday()
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }
    if (showClearAllDialog) {
        ConfirmDialog(
            title = "Clear all data?",
            body = "Wipes every mandala entry, habit, and shell. The default daily habits will be re-seeded. This can't be undone.",
            confirmLabel = "Clear",
            onConfirm = {
                viewModel.clearAll()
                showClearAllDialog = false
            },
            onDismiss = { showClearAllDialog = false }
        )
    }
}

@Composable
private fun SettingsHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "TUNE THE SEA",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Settings",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun UserNameField(
    value: String,
    onCommit: (String) -> Unit
) {
    var draft by remember(value) { mutableStateOf(value) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "WHAT SHOULD WE CALL YOU",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BasicTextField(
                value = draft,
                onValueChange = { draft = it },
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .onFocusChanged { focus ->
                        if (!focus.isFocused && draft != value) {
                            onCommit(draft)
                        }
                    }
            )
        }
    }
}

@Composable
private fun NotifModeRow(
    label: String,
    hint: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
                )
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionRow(
    label: String,
    hint: String,
    onClick: () -> Unit,
    destructive: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                color = if (destructive) MaterialTheme.colorScheme.tertiary
                       else MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    hint: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}
