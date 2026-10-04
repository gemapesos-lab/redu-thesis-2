package edu.feutech.redu.ui

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.feutech.redu.BuildConfig
import edu.feutech.redu.R
import edu.feutech.redu.ui.theme.ReduButtonMinHeight
import edu.feutech.redu.ui.theme.ReduInlineIconSize
import edu.feutech.redu.ui.theme.ReduPalette
import edu.feutech.redu.ui.theme.ReduPill
import edu.feutech.redu.ui.theme.ReduTheme
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
        pinHeader = true,
    ) {
        item {
            ReduSectionHeader(title = "Monitoring", quiet = true)
        }
        item {
            ReduSection {
                ReduSettingRow(
                    title = "Monitoring service",
                    subtitle = if (accessibilityEnabled) null else "Permission is required to save new sessions",
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
                        platform = Platform.TIKTOK,
                        checked = settings?.trackTikTokEnabled == true,
                        onCheckedChange = { onPlatformTrackingChange(Platform.TIKTOK, it) },
                    )
                    ReduDivider()
                    PlatformToggleRow(
                        platform = Platform.INSTAGRAM,
                        checked = settings?.trackInstagramEnabled == true,
                        onCheckedChange = { onPlatformTrackingChange(Platform.INSTAGRAM, it) },
                    )
                    ReduDivider()
                    PlatformToggleRow(
                        platform = Platform.FACEBOOK,
                        checked = settings?.trackFacebookEnabled == true,
                        onCheckedChange = { onPlatformTrackingChange(Platform.FACEBOOK, it) },
                    )
                }
            }
        }

        item {
            ReduSectionHeader(title = "Intervention", subtitle = "Week 2 prompt behavior", quiet = true)
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
            ReduSectionHeader(title = "Study details", quiet = true)
        }
        item {
            ReduSection {
                ReduSettingRow(
                    title = "Study configuration",
                    subtitle = settings?.studyCode?.takeIf { it != "UNSET" }?.let { "Participant $it" } ?: "Participant code not set",
                    onClick = { studyDetailsExpanded = !studyDetailsExpanded },
                    trailing = {
                        Icon(
                            painterResource(if (studyDetailsExpanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down),
                            contentDescription = if (studyDetailsExpanded) "Hide study details" else "Show study details",
                            modifier = Modifier.size(ReduInlineIconSize),
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
                                Icon(painterResource(R.drawable.ic_edit), contentDescription = "Edit participant code")
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
                                color = if (status == "Study period saved") ReduPalette.Sage else MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }

        item {
            ReduSectionHeader(title = "Image scanning", quiet = true)
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
            ReduSectionHeader(title = "Data and privacy", quiet = true)
        }
        item {
            ReduSection {
                ReduSettingRow(
                    title = "Export study data",
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
            ReduSectionHeader(title = "Advanced", quiet = true)
        }
        item {
            ReduSection {
                ReduSettingRow(
                    title = "Advanced controls",
                    subtitle = "Study-data reset and developer tools",
                    onClick = { advancedExpanded = !advancedExpanded },
                    trailing = {
                        Icon(
                            painterResource(if (advancedExpanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down),
                            contentDescription = if (advancedExpanded) "Hide advanced controls" else "Show advanced controls",
                            modifier = Modifier.size(ReduInlineIconSize),
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
                                ReduOutlinedButton("L1", { onDemoIntervention(PromptLevel.L1_AWARENESS) }, Modifier.weight(1f))
                                ReduOutlinedButton("L2", { onDemoIntervention(PromptLevel.L2_PAUSE) }, Modifier.weight(1f))
                                ReduOutlinedButton("L3", { onDemoIntervention(PromptLevel.L3_BREATHING) }, Modifier.weight(1f))
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
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.extraLarge,
            title = { Text("Edit participant code") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReduTextField(
                        value = editedStudyCode,
                        onValueChange = { editedStudyCode = it.trim() },
                        label = "Participant study code",
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
                ReduTextButton(
                    text = "Save",
                    onClick = {
                        onStudyCodeSave(editedStudyCode)
                        editCodeDialogOpen = false
                    },
                    enabled = editedStudyCode.isNotBlank() && !hasExistingSessions,
                    contentColor = MaterialTheme.colorScheme.primary,
                )
            },
            dismissButton = {
                ReduTextButton(text = "Cancel", onClick = { editCodeDialogOpen = false })
            },
        )
    }

    if (resetStudyDataDialogOpen) {
        AlertDialog(
            onDismissRequest = { resetStudyDataDialogOpen = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.extraLarge,
            title = { Text("Reset study data?") },
            text = {
                Text("This permanently deletes sessions, prompt events, reliability logs, and personalization. Participant settings and downloaded models stay on this device.")
            },
            confirmButton = {
                ReduTextButton(
                    text = "Reset data",
                    onClick = {
                        resetStudyDataDialogOpen = false
                        onResetStudyData()
                    },
                    contentColor = MaterialTheme.colorScheme.error,
                )
            },
            dismissButton = {
                ReduTextButton(text = "Cancel", onClick = { resetStudyDataDialogOpen = false })
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
    Button(
        onClick = { dialogOpen = true },
        modifier = Modifier.fillMaxWidth().heightIn(min = ReduButtonMinHeight),
        shape = ReduPill,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            disabledElevation = 0.dp,
        ),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value?.formatStudyDate() ?: "Choose date", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge)
        }
        Icon(painterResource(R.drawable.ic_calendar), contentDescription = null, modifier = Modifier.size(ReduInlineIconSize))
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
                ReduTextButton(
                    text = "Choose",
                    onClick = {
                        val selected = pickerState.selectedDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        onValueChange(selected)
                        dialogOpen = false
                    },
                    enabled = pickerState.selectedDateMillis != null,
                    contentColor = MaterialTheme.colorScheme.primary,
                )
            },
            dismissButton = {
                ReduTextButton(text = "Cancel", onClick = { dialogOpen = false })
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
                ReduDivider()
            }
            when (modelState) {
                ModelState.NotDownloaded -> ReduOutlinedButton(
                    text = "Download model",
                    onClick = onDownload,
                    modifier = Modifier.fillMaxWidth(),
                )
                is ModelState.Downloading -> {
                    ReduLinearProgress(progress = { modelState.progress })
                    ReduCaption("${(modelState.progress * 100).toInt()}%${modelState.detail?.let { " / $it" }.orEmpty()}")
                    ReduOutlinedButton(
                        text = "Cancel download",
                        onClick = onCancelDownload,
                        modifier = Modifier.fillMaxWidth(),
                        destructive = true,
                    )
                }
                is ModelState.Verifying -> {
                    ReduLinearProgress()
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

private fun personalizationStatus(
    settings: AppSettingsEntity,
    personalization: RiskPersonalizationEntity?,
): String = when {
    !settings.promptsEnabled -> "Personalization locks when intervention prompts are enabled for Week 2."
    personalization == null -> "Default activity ranges are active because no baseline profile is available."
    !personalization.hasAnyPersonalizedBounds() -> "Default activity ranges are active because baseline coverage was insufficient."
    else -> "Personalized ranges are locked from ${personalization.reliableBaselineSessionCount} reliable baseline sessions."
}

@Preview(name = "Settings", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF0A0A0A)
@Preview(name = "Settings large font", widthDp = 320, heightDp = 900, fontScale = 1.5f, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun SettingsPreview() {
    ReduTheme {
        SettingsScreen(
            padding = PaddingValues(0.dp),
            settings = null,
            personalization = null,
            accessibilityEnabled = false,
            debugOverlayEnabled = false,
            modelState = ModelState.NotDownloaded,
            hasExistingSessions = false,
            onDownloadModel = {},
            onCancelModelDownload = {},
            onDeleteModel = {},
            onPlatformTrackingChange = { _, _ -> },
            onStudyCodeSave = {},
            onStudyPeriodSave = { _, _, _, _ -> },
            onResetStudyData = {},
            onPromptsEnabledChange = {},
            onDebugOverlayChange = {},
            onOpenAccessibilitySettings = {},
            onOpenExport = {},
        )
    }
}
