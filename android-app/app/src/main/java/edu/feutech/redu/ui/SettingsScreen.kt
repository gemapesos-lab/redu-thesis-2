package edu.feutech.redu.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.feutech.redu.BuildConfig
import edu.feutech.redu.data.AppSettingsEntity
import edu.feutech.redu.data.Platform
import edu.feutech.redu.data.PromptLevel
import edu.feutech.redu.data.RiskPersonalizationEntity
import edu.feutech.redu.data.StudyGroup
import edu.feutech.redu.export.CsvExporter
import edu.feutech.redu.risk.hasAnyPersonalizedBounds
import edu.feutech.redu.vlm.ModelDownloadManager
import edu.feutech.redu.vlm.ModelState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
internal fun SettingsScreen(
    padding: PaddingValues,
    settings: AppSettingsEntity?,
    personalization: RiskPersonalizationEntity?,
    accessibilityEnabled: Boolean,
    debugOverlayEnabled: Boolean,
    modelState: ModelState,
    hasExistingSessions: Boolean,
    onDownloadModel: () -> Unit,
    onCancelModelDownload: () -> Unit,
    onDeleteModel: () -> Unit,
    onPlatformTrackingChange: (Platform, Boolean) -> Unit,
    onStudyCodeSave: (String) -> Unit,
    onStudyPeriodSave: (Long?, Long?, Long?, Long?) -> Unit,
    onResetStudyData: () -> Unit,
    onPromptsEnabledChange: (Boolean) -> Unit,
    onDebugOverlayChange: (Boolean) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenSetup: () -> Unit,
    onOpenExport: () -> Unit,
    onDemoIntervention: (PromptLevel) -> Unit = {},
) {
    var studyDetailsExpanded by rememberSaveable { mutableStateOf(false) }
    var advancedExpanded by rememberSaveable { mutableStateOf(false) }
    var editCodeDialogOpen by rememberSaveable { mutableStateOf(false) }
    var resetStudyDataDialogOpen by rememberSaveable { mutableStateOf(false) }
    var editedStudyCode by rememberSaveable { mutableStateOf(settings?.studyCode?.takeIf { it != "UNSET" }.orEmpty()) }
    var week1StartDay by rememberSaveable { mutableStateOf(settings?.week1StartMillis.toStudyLocalDate()?.toEpochDay()) }
    var week1EndDay by rememberSaveable { mutableStateOf(settings?.week1EndMillis.toStudyLocalDate()?.toEpochDay()) }
    var week2StartDay by rememberSaveable { mutableStateOf(settings?.week2StartMillis.toStudyLocalDate()?.toEpochDay()) }
    var week2EndDay by rememberSaveable { mutableStateOf(settings?.week2EndMillis.toStudyLocalDate()?.toEpochDay()) }
    var studyPeriodStatus by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(settings?.studyCode) {
        editedStudyCode = settings?.studyCode?.takeIf { it != "UNSET" }.orEmpty()
    }
    LaunchedEffect(
        settings?.week1StartMillis,
        settings?.week1EndMillis,
        settings?.week2StartMillis,
        settings?.week2EndMillis,
    ) {
        week1StartDay = settings?.week1StartMillis.toStudyLocalDate()?.toEpochDay()
        week1EndDay = settings?.week1EndMillis.toStudyLocalDate()?.toEpochDay()
        week2StartDay = settings?.week2StartMillis.toStudyLocalDate()?.toEpochDay()
        week2EndDay = settings?.week2EndMillis.toStudyLocalDate()?.toEpochDay()
        studyPeriodStatus = null
    }

    ReduScreen(
        padding = padding,
        title = "Settings",
        subtitle = "Monitoring, study, and local data controls",
    ) {
        item {
            ReduSectionHeader(title = "Monitoring", subtitle = "Service and selected platforms")
        }
        item {
            ReduSection {
                ReduSettingRow(
                    title = "Monitoring service",
                    subtitle = if (accessibilityEnabled) "Android permission is active" else "Permission is required to save new sessions",
                    onClick = onOpenAccessibilitySettings,
                    trailing = {
                        ReduStatusLabel(
                            if (accessibilityEnabled) "On" else "Permission needed",
                            if (accessibilityEnabled) StatusTone.SUCCESS else StatusTone.ATTENTION,
                        )
                    },
                )
                ReduDivider(Modifier.padding(horizontal = 16.dp))
                Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    PlatformToggleRow(
                        label = "TikTok",
                        checked = settings?.trackTikTokEnabled == true,
                        onCheckedChange = { onPlatformTrackingChange(Platform.TIKTOK, it) },
                    )
                    ReduDivider()
                    PlatformToggleRow(
                        label = "Instagram",
                        checked = settings?.trackInstagramEnabled == true,
                        onCheckedChange = { onPlatformTrackingChange(Platform.INSTAGRAM, it) },
                    )
                    ReduDivider()
                    PlatformToggleRow(
                        label = "Facebook",
                        checked = settings?.trackFacebookEnabled == true,
                        onCheckedChange = { onPlatformTrackingChange(Platform.FACEBOOK, it) },
                    )
                }
                ReduDivider(Modifier.padding(horizontal = 16.dp))
                ReduSettingRow(
                    title = "Review setup",
                    subtitle = "Participant code, platforms, and permission",
                    onClick = onOpenSetup,
                )
            }
        }

        item {
            ReduSectionHeader(title = "Intervention", subtitle = "Week 2 prompt behavior")
        }
        item {
            ReduSection {
                if (settings?.studyGroup == StudyGroup.INTERVENTION) {
                    ReduSettingRow(
                        title = "Pause prompts",
                        subtitle = if (settings.promptsEnabled) {
                            "Enabled for the intervention phase"
                        } else {
                            "Suppressed during baseline logging"
                        },
                        trailing = {
                            ReduSwitch(
                                checked = settings.promptsEnabled,
                                onCheckedChange = onPromptsEnabledChange,
                                label = "Pause prompts",
                            )
                        },
                    )
                    ReduDivider(Modifier.padding(horizontal = 16.dp))
                    ReduCaption(
                        personalizationStatus(settings, personalization),
                        Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                } else {
                    ReduSettingRow(
                        title = "Logging only",
                        subtitle = "Control participants do not receive intervention prompts.",
                    )
                }
            }
        }

        item {
            ReduSectionHeader(title = "Study details", subtitle = "Participant assignment and dates")
        }
        item {
            ReduSection {
                ReduSettingRow(
                    title = "Study configuration",
                    subtitle = settings?.studyCode?.takeIf { it != "UNSET" }?.let { "Participant $it" } ?: "Participant code not set",
                    onClick = { studyDetailsExpanded = !studyDetailsExpanded },
                    trailing = {
                        Icon(
                            if (studyDetailsExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = if (studyDetailsExpanded) "Hide study details" else "Show study details",
                        )
                    },
                )
                if (studyDetailsExpanded) {
                    ReduDivider(Modifier.padding(horizontal = 16.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp).animateContentSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                ReduCaption("Participant code")
                                Text(
                                    settings?.studyCode?.takeIf { it != "UNSET" } ?: "Not set",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                            IconButton(onClick = { editCodeDialogOpen = true }, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Edit participant code")
                            }
                        }
                        ReduInfoRow("Assigned group", settings?.studyGroup?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Not set")
                        ReduInfoRow("Study timezone", CsvExporter.STUDY_ZONE.id)
                        ReduDivider()
                        Text("Study period", style = MaterialTheme.typography.titleSmall)
                        StudyDateField("Week 1 start", week1StartDay?.let(LocalDate::ofEpochDay)) {
                            week1StartDay = it?.toEpochDay()
                            studyPeriodStatus = null
                        }
                        StudyDateField("Week 1 end", week1EndDay?.let(LocalDate::ofEpochDay)) {
                            week1EndDay = it?.toEpochDay()
                            studyPeriodStatus = null
                        }
                        StudyDateField("Week 2 start", week2StartDay?.let(LocalDate::ofEpochDay)) {
                            week2StartDay = it?.toEpochDay()
                            studyPeriodStatus = null
                        }
                        StudyDateField("Week 2 end", week2EndDay?.let(LocalDate::ofEpochDay)) {
                            week2EndDay = it?.toEpochDay()
                            studyPeriodStatus = null
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ReduOutlinedButton(
                                text = "Clear",
                                onClick = {
                                    week1StartDay = null
                                    week1EndDay = null
                                    week2StartDay = null
                                    week2EndDay = null
                                    studyPeriodStatus = null
                                },
                                modifier = Modifier.weight(1f),
                            )
                            ReduPrimaryButton(
                                text = "Save dates",
                                onClick = {
                                    val parsed = parseStudyPeriodInputs(
                                        week1StartDay?.let(LocalDate::ofEpochDay)?.toString().orEmpty(),
                                        week1EndDay?.let(LocalDate::ofEpochDay)?.toString().orEmpty(),
                                        week2StartDay?.let(LocalDate::ofEpochDay)?.toString().orEmpty(),
                                        week2EndDay?.let(LocalDate::ofEpochDay)?.toString().orEmpty(),
                                    )
                                    when (parsed) {
                                        is StudyPeriodParseResult.Valid -> {
                                            onStudyPeriodSave(
                                                parsed.week1StartMillis,
                                                parsed.week1EndMillis,
                                                parsed.week2StartMillis,
                                                parsed.week2EndMillis,
                                            )
                                            studyPeriodStatus = "Study period saved"
                                        }
                                        is StudyPeriodParseResult.Invalid -> studyPeriodStatus = parsed.message
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        studyPeriodStatus?.let { status ->
                            Text(
                                status,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (status == "Study period saved") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }

        item {
            ReduSectionHeader(title = "Visual fallback", subtitle = "Optional on-device no-text model")
        }
        item {
            VlmModelSection(
                modelState = modelState,
                onDownload = onDownloadModel,
                onCancelDownload = onCancelModelDownload,
                onDelete = onDeleteModel,
            )
        }

        item {
            ReduSectionHeader(title = "Data and privacy", subtitle = "What stays on this device")
        }
        item {
            ReduSection {
                ReduSettingRow(
                    title = "Export study data",
                    subtitle = "Share six aggregate CSV datasets in one ZIP",
                    onClick = onOpenExport,
                )
                ReduDivider(Modifier.padding(horizontal = 16.dp))
                ReduSettingRow(
                    title = "Local processing",
                    subtitle = "Raw captions, comments, and temporary screen frames are not retained in exports.",
                )
            }
        }

        item {
            ReduSectionHeader(title = "Advanced")
        }
        item {
            ReduSection {
                ReduSettingRow(
                    title = "Advanced controls",
                    subtitle = "Study-data reset and developer tools",
                    onClick = { advancedExpanded = !advancedExpanded },
                    trailing = {
                        Icon(
                            if (advancedExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = if (advancedExpanded) "Hide advanced controls" else "Show advanced controls",
                        )
                    },
                )
                if (advancedExpanded) {
                    ReduDivider(Modifier.padding(horizontal = 16.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp).animateContentSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("Data management", style = MaterialTheme.typography.titleSmall)
                        ReduCaption("Use reset only when starting a new participant or intentionally clearing personalization.")
                        ReduOutlinedButton(
                            text = "Reset study data",
                            onClick = { resetStudyDataDialogOpen = true },
                            modifier = Modifier.fillMaxWidth(),
                            destructive = true,
                        )

                        if (BuildConfig.DEBUG) {
                            ReduDivider()
                            Text("Developer tools", style = MaterialTheme.typography.titleSmall)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Show extraction metrics", modifier = Modifier.weight(1f))
                                ReduSwitch(
                                    checked = debugOverlayEnabled,
                                    onCheckedChange = onDebugOverlayChange,
                                    label = "Extraction metrics overlay",
                                )
                            }
                            Text("Demo intervention", style = MaterialTheme.typography.titleSmall)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PromptDemoButton("L1", Modifier.weight(1f)) { onDemoIntervention(PromptLevel.L1_AWARENESS) }
                                PromptDemoButton("L2", Modifier.weight(1f)) { onDemoIntervention(PromptLevel.L2_PAUSE) }
                                PromptDemoButton("L3", Modifier.weight(1f)) { onDemoIntervention(PromptLevel.L3_BREATHING) }
                            }
                            ReduCaption("Requires the monitoring service to be enabled.")
                        }
                    }
                }
            }
        }
    }

    if (editCodeDialogOpen) {
        AlertDialog(
            onDismissRequest = { editCodeDialogOpen = false },
            title = { Text("Edit participant code") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editedStudyCode,
                        onValueChange = { editedStudyCode = it.trim() },
                        label = { Text("Participant study code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    )
                    if (editedStudyCode.isNotBlank()) {
                        ReduInfoRow("Assigned group", studyGroupForParticipantCode(editedStudyCode).name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                    if (hasExistingSessions) {
                        ReduCaption("Reset study data before changing the participant code.")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onStudyCodeSave(editedStudyCode)
                        editCodeDialogOpen = false
                    },
                    enabled = editedStudyCode.isNotBlank() && !hasExistingSessions,
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editCodeDialogOpen = false }) { Text("Cancel") }
            },
        )
    }

    if (resetStudyDataDialogOpen) {
        AlertDialog(
            onDismissRequest = { resetStudyDataDialogOpen = false },
            title = { Text("Reset study data?") },
            text = {
                Text("This permanently deletes sessions, prompt events, reliability logs, and personalization. Participant settings and downloaded models stay on this device.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        resetStudyDataDialogOpen = false
                        onResetStudyData()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Reset data") }
            },
            dismissButton = {
                TextButton(onClick = { resetStudyDataDialogOpen = false }) { Text("Cancel") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudyDateField(
    label: String,
    value: LocalDate?,
    onValueChange: (LocalDate?) -> Unit,
) {
    var dialogOpen by rememberSaveable { mutableStateOf(false) }
    OutlinedButton(
        onClick = { dialogOpen = true },
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value?.formatStudyDate() ?: "Choose date", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
    }

    if (dialogOpen) {
        val initialMillis = value
            ?.atStartOfDay(ZoneOffset.UTC)
            ?.toInstant()
            ?.toEpochMilli()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { dialogOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selected = pickerState.selectedDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        onValueChange(selected)
                        dialogOpen = false
                    },
                    enabled = pickerState.selectedDateMillis != null,
                ) { Text("Choose") }
            },
            dismissButton = {
                TextButton(onClick = { dialogOpen = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun VlmModelSection(
    modelState: ModelState,
    onDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onDelete: () -> Unit,
) {
    var detailsExpanded by rememberSaveable { mutableStateOf(false) }
    val (statusLabel, statusTone) = when (modelState) {
        ModelState.NotDownloaded -> "Not downloaded" to StatusTone.NEUTRAL
        is ModelState.Downloading -> "Downloading" to StatusTone.ATTENTION
        is ModelState.Verifying -> "Verifying" to StatusTone.ATTENTION
        ModelState.Ready -> "Ready" to StatusTone.SUCCESS
        is ModelState.Error -> "Needs attention" to StatusTone.ERROR
    }
    ReduSection {
        ReduSettingRow(
            title = "On-device visual model",
            subtitle = "Improves coverage for items without usable text",
            onClick = { detailsExpanded = !detailsExpanded },
            trailing = { ReduStatusLabel(statusLabel, statusTone) },
        )
        ReduDivider(Modifier.padding(horizontal = 16.dp))
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (detailsExpanded) {
                ReduCaption("Moondream 2 text model (Q4_K_M) and multimodal projector (F16)")
                ReduInfoRow("Download size", formatBytes(ModelDownloadManager.TOTAL_SIZE_BYTES))
                ReduCaption("Model files remain in REDU's private app storage and are used only on this device.")
                ReduDivider()
            }
            when (modelState) {
                ModelState.NotDownloaded -> ReduPrimaryButton(
                    text = "Download model",
                    onClick = onDownload,
                    modifier = Modifier.fillMaxWidth(),
                )
                is ModelState.Downloading -> {
                    LinearProgressIndicator(
                        progress = { modelState.progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        strokeCap = StrokeCap.Round,
                    )
                    ReduCaption("${(modelState.progress * 100).toInt()}%${modelState.detail?.let { " / $it" }.orEmpty()}")
                    ReduOutlinedButton(
                        text = "Cancel download",
                        onClick = onCancelDownload,
                        modifier = Modifier.fillMaxWidth(),
                        destructive = true,
                    )
                }
                is ModelState.Verifying -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(6.dp), strokeCap = StrokeCap.Round)
                    ReduCaption(modelState.detail ?: "Checking downloaded model files")
                }
                ModelState.Ready -> ReduOutlinedButton(
                    text = "Delete model",
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    destructive = true,
                )
                is ModelState.Error -> {
                    Text(modelState.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    ReduPrimaryButton(text = "Retry download", onClick = onDownload, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun PromptDemoButton(label: String, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

private fun personalizationStatus(
    settings: AppSettingsEntity,
    personalization: RiskPersonalizationEntity?,
): String = when {
    !settings.promptsEnabled -> "Personalization locks when intervention prompts are enabled for Week 2."
    personalization == null -> "Default activity ranges are active because no baseline profile is available."
    !personalization.hasAnyPersonalizedBounds() -> "Default activity ranges are active because baseline coverage was insufficient."
    else -> "Personalized ranges are locked from ${personalization.reliableBaselineSessionCount} reliable baseline sessions."
}
