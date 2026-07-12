package edu.feutech.redu.ui

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.feutech.redu.BuildConfig
import edu.feutech.redu.data.Platform
import edu.feutech.redu.data.RiskLevel
import edu.feutech.redu.data.SentimentReliability
import edu.feutech.redu.data.SessionEntity
import edu.feutech.redu.data.StudyGroup
import edu.feutech.redu.ui.theme.ReduTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DashboardScreen(
    padding: PaddingValues,
    state: DashboardUiState,
    onOpenSetup: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    var scoreInfoOpen by rememberSaveable { mutableStateOf(false) }
    var diagnosticsExpanded by rememberSaveable { mutableStateOf(false) }

    ReduScreen(
        padding = padding,
        title = "Today",
        subtitle = state.date.formatDashboardDate(),
        actions = {
            IconButton(onClick = { scoreInfoOpen = true }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Outlined.Info, contentDescription = "How activity patterns are calculated")
            }
        },
    ) {
        if (!state.setupComplete) {
            item {
                ReduAttentionBanner(
                    title = "Monitoring needs attention",
                    body = "Review setup so REDU can continue saving short-form video sessions.",
                    actionLabel = "Review setup",
                    onAction = onOpenSetup,
                    modifier = Modifier.padding(bottom = 24.dp),
                )
            }
        }

        item {
            DailyOverview(
                summary = state.summary,
                onOpenScoreInfo = { scoreInfoOpen = true },
            )
        }

        item {
            ReduSectionHeader(
                title = "Monitoring",
                subtitle = "Selected short-form video platforms",
                trailing = {
                    ReduStatusLabel(
                        label = if (state.accessibilityEnabled) "Service on" else "Permission needed",
                        tone = if (state.accessibilityEnabled) StatusTone.SUCCESS else StatusTone.ATTENTION,
                    )
                },
            )
        }

        item {
            MonitoringSection(
                platforms = state.platforms,
                date = state.date,
                onOpenSetup = onOpenSetup,
            )
        }

        item {
            ReduSectionHeader(title = "Latest session")
        }

        item {
            val latest = state.summary.latestSession
            if (latest == null) {
                ReduEmptyState(
                    title = "No saved sessions yet",
                    body = "Use a selected platform while monitoring is on. New sessions will appear here.",
                    actionLabel = "Check monitoring",
                    onAction = onOpenSetup,
                )
            } else {
                LatestSessionRow(session = latest, onClick = onOpenHistory)
            }
        }

        if (BuildConfig.DEBUG) {
            item {
                ReduSectionHeader(title = "Research diagnostics")
            }
            item {
                ReduSection {
                    ReduSettingRow(
                        title = "Signal details",
                        subtitle = "Aggregate values saved on this device",
                        onClick = { diagnosticsExpanded = !diagnosticsExpanded },
                        trailing = {
                            Icon(
                                if (diagnosticsExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                contentDescription = if (diagnosticsExpanded) "Hide signal details" else "Show signal details",
                            )
                        },
                    )
                    if (diagnosticsExpanded) {
                        ReduDivider(Modifier.padding(horizontal = 16.dp))
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            ReduInfoRow("Total sessions", state.totalSessionCount.toString())
                            ReduInfoRow("Reliable sessions", state.reliableSessionCount.toString())
                            ReduInfoRow("Latest NSD", state.summary.latestSession?.nsdPercent?.formatPercentValue() ?: "No data")
                            ReduInfoRow("Latest OOV", state.summary.latestSession?.oovRatio?.formatPercentRatio() ?: "No data")
                            ReduInfoRow("Latest dwell", state.summary.latestSession?.meanDwellMillis?.formatMetricDuration() ?: "No data")
                            ReduInfoRow("Latest transitions", state.summary.latestSession?.swipeCount?.toString() ?: "No data")
                        }
                    }
                }
            }
        }
    }

    if (scoreInfoOpen) {
        ModalBottomSheet(
            onDismissRequest = { scoreInfoOpen = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Activity patterns", style = MaterialTheme.typography.titleLarge)
                ReduSecondaryText(
                    "REDU combines session length, dwell time, and available negative-content signals into a 0-100 estimate.",
                )
                ActivityPatternMeter(score = state.summary.latestRiskScore ?: 0.0)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Low", style = MaterialTheme.typography.labelMedium)
                    Text("Elevated", style = MaterialTheme.typography.labelMedium)
                    Text("High", style = MaterialTheme.typography.labelMedium)
                }
                ReduDivider()
                ReduCaption(
                    "This is a non-clinical activity estimate. It does not diagnose a mental health or behavioral condition.",
                )
            }
        }
    }
}

@Composable
private fun DailyOverview(
    summary: DashboardSummary,
    onOpenScoreInfo: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (summary.todaySessionCount == 0) "No activity" else formatReadableDuration(summary.todayActiveMillis),
                    style = MaterialTheme.typography.displaySmall,
                )
                ReduSecondaryText(
                    if (summary.todaySessionCount == 0) {
                        "Nothing has been logged today"
                    } else {
                        "${summary.todaySessionCount} ${summary.todaySessionCount.sessionLabel()} today"
                    },
                )
            }
            summary.peakRiskLevel?.let { level ->
                val presentation = activityPatternFor(level)
                ReduStatusLabel("${presentation.label} pattern", presentation.tone)
            }
        }

        if (summary.todaySessionCount > 0 && summary.latestRiskScore != null) {
            ActivityPatternMeter(score = summary.latestRiskScore)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ReduCaption("Latest score ${summary.latestRiskScore.formatOneDecimal()}/100")
                TextButton(onClick = onOpenScoreInfo, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)) {
                    Text("What this means")
                }
            }
        } else {
            ReduCaption("Today begins at midnight in your device timezone.")
        }
        ReduDivider()
    }
}

@Composable
private fun MonitoringSection(
    platforms: List<PlatformMonitoringUiState>,
    date: LocalDate,
    onOpenSetup: () -> Unit,
) {
    val needsAttention = platforms.any { it.state != PlatformMonitoringState.ON }
    ReduSection(
        modifier = if (needsAttention) Modifier.clickable(role = Role.Button, onClick = onOpenSetup) else Modifier,
    ) {
        platforms.forEachIndexed { index, item ->
            MonitoringRow(item = item, date = date)
            if (index < platforms.lastIndex) ReduDivider(Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun MonitoringRow(item: PlatformMonitoringUiState, date: LocalDate) {
    val (label, tone) = when (item.state) {
        PlatformMonitoringState.ON -> "Monitoring" to StatusTone.SUCCESS
        PlatformMonitoringState.OFF -> "Off" to StatusTone.NEUTRAL
        PlatformMonitoringState.PAUSED -> "Permission needed" to StatusTone.ATTENTION
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.platform.displayName(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            ReduCaption(item.latestSession.monitoringRecency(date))
        }
        ReduStatusLabel(label, tone)
    }
}

@Composable
private fun LatestSessionRow(session: SessionEntity, onClick: () -> Unit) {
    val presentation = activityPatternFor(session.riskLevel)
    ReduSection {
        Column(
            modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(session.platform.displayName(), style = MaterialTheme.typography.titleMedium)
                    ReduSecondaryText("${session.startedAtMillis.formatTimeOfDay()} / ${formatReadableDuration(session.rawDurationMillis)}")
                }
                ReduStatusLabel(presentation.label, presentation.tone)
                Icon(Icons.Outlined.ChevronRight, contentDescription = "Open session history")
            }
            ActivityPatternMeter(session.riskScore)
            ReduCaption("Score ${session.riskScore.formatOneDecimal()}/100 / ${session.sentimentReliability.displayName()}")
        }
    }
}

@Composable
internal fun HistoryScreen(
    padding: PaddingValues,
    sessions: List<SessionEntity>,
    onClearHistory: () -> Unit,
) {
    var platformFilter by rememberSaveable { mutableStateOf(PlatformFilter.ALL) }
    var riskFilter by rememberSaveable { mutableStateOf(RiskFilter.ALL) }
    var expandedSessionId by rememberSaveable { mutableStateOf<Long?>(null) }
    var clearHistoryDialogOpen by rememberSaveable { mutableStateOf(false) }
    var historyMenuExpanded by remember { mutableStateOf(false) }
    val filtered = filteredSessions(sessions, platformFilter, riskFilter)
    val groups = groupedSessionsByDate(filtered)

    ReduScreen(
        padding = padding,
        title = "History",
        subtitle = if (sessions.isEmpty()) "No saved sessions" else "${filtered.size} of ${sessions.size} sessions",
        actions = {
            if (sessions.isNotEmpty()) {
                Box {
                    IconButton(onClick = { historyMenuExpanded = true }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "History actions")
                    }
                    DropdownMenu(
                        expanded = historyMenuExpanded,
                        onDismissRequest = { historyMenuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear history", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                historyMenuExpanded = false
                                clearHistoryDialogOpen = true
                            },
                        )
                    }
                }
            }
        },
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactFilterMenu(
                    selectedLabel = platformFilter.displayName(),
                    options = PlatformFilter.entries.toList(),
                    optionLabel = { it.displayName() },
                    onOptionSelected = { platformFilter = it },
                    modifier = Modifier.weight(1f),
                )
                CompactFilterMenu(
                    selectedLabel = riskFilter.displayName(),
                    options = RiskFilter.entries.toList(),
                    optionLabel = { it.displayName() },
                    onOptionSelected = { riskFilter = it },
                    modifier = Modifier.weight(1f),
                )
            }
            ReduDivider()
        }

        if (filtered.isEmpty()) {
            item {
                ReduEmptyState(
                    title = if (sessions.isEmpty()) "No sessions yet" else "No matching sessions",
                    body = if (sessions.isEmpty()) {
                        "History will build as REDU saves monitoring sessions on this device."
                    } else {
                        "Change one of the filters to see more of your saved history."
                    },
                )
            }
        } else {
            groups.forEach { group ->
                item(key = "header-${group.date}") {
                    ReduSectionHeader(title = group.date.formatDateHeader())
                }
                item(key = "group-${group.date}") {
                    ReduSection {
                        group.sessions.forEachIndexed { index, session ->
                            SessionHistoryRow(
                                session = session,
                                expanded = expandedSessionId == session.id,
                                onClick = {
                                    expandedSessionId = if (expandedSessionId == session.id) null else session.id
                                },
                            )
                            if (index < group.sessions.lastIndex) ReduDivider(Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }

    if (clearHistoryDialogOpen) {
        AlertDialog(
            onDismissRequest = { clearHistoryDialogOpen = false },
            title = { Text("Clear history?") },
            text = {
                Text("This permanently deletes saved sessions, prompt events, and reliability logs. Participant settings and downloaded models stay on this device.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        clearHistoryDialogOpen = false
                        onClearHistory()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Clear history") }
            },
            dismissButton = {
                TextButton(onClick = { clearHistoryDialogOpen = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun <T> CompactFilterMenu(
    selectedLabel: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Text(
                selectedLabel,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(Icons.Outlined.ExpandMore, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SessionHistoryRow(
    session: SessionEntity,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val presentation = activityPatternFor(session.riskLevel)
    Column(
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick).animateContentSize()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(session.platform.displayName(), style = MaterialTheme.typography.titleMedium)
                ReduSecondaryText("${session.startedAtMillis.formatTimeOfDay()} / ${formatReadableDuration(session.rawDurationMillis)}")
                ReduCaption("${session.sentimentReliability.displayName()} / score ${session.riskScore.formatOneDecimal()}")
            }
            ReduStatusLabel(presentation.label, presentation.tone)
            Icon(
                if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = if (expanded) "Hide session details" else "Show session details",
            )
        }
        if (expanded) {
            ActivityPatternMeter(session.riskScore)
            ReduDivider()
            ReduInfoRow("Pattern range", "${presentation.label} (${presentation.rangeLabel})")
            ReduInfoRow("Mean dwell", session.meanDwellMillis.formatMetricDuration())
            ReduInfoRow("Transitions", session.swipeCount.toString())
            ReduInfoRow("Negative-content density", session.nsdPercent?.formatPercentValue() ?: "Unavailable")
            ReduInfoRow("Resolved items", session.resolvableUnits.toString())
            ReduInfoRow("Negative items", session.negativeUnits.toString())
            ReduInfoRow("Unrecognized text", session.oovRatio.formatPercentRatio())
            ReduCaption(presentation.explanation)
        }
    }
}

@Composable
internal fun SetupScreen(
    padding: PaddingValues,
    studyCode: String,
    hasSavedParticipantCode: Boolean,
    accessibilityEnabled: Boolean,
    trackTikTokEnabled: Boolean,
    trackInstagramEnabled: Boolean,
    trackFacebookEnabled: Boolean,
    onStudyCodeChange: (String) -> Unit,
    onPlatformTrackingChange: (Platform, Boolean) -> Unit,
    onSave: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onFinish: () -> Unit,
    onBack: (() -> Unit)?,
) {
    val anyPlatformEnabled = trackTikTokEnabled || trackInstagramEnabled || trackFacebookEnabled
    val currentStep = setupStepFor(hasSavedParticipantCode, anyPlatformEnabled, accessibilityEnabled)
    val completedSteps = listOf(hasSavedParticipantCode, anyPlatformEnabled, accessibilityEnabled).count { it }
    var expandedStep by rememberSaveable { mutableStateOf<SetupStep?>(null) }
    val activeStep = expandedStep ?: currentStep

    LaunchedEffect(hasSavedParticipantCode) {
        if (!hasSavedParticipantCode) expandedStep = SetupStep.PARTICIPANT
    }

    ReduScreen(
        padding = padding,
        title = if (currentStep == SetupStep.COMPLETE) "Setup ready" else "Set up REDU",
        subtitle = if (currentStep == SetupStep.COMPLETE) "Monitoring can begin" else "Step ${completedSteps + 1} of 3",
        onBack = onBack,
    ) {
        item {
            SetupProgressSegments(completedSteps = completedSteps)
            Spacer(Modifier.height(20.dp))
        }

        item {
            ReduSection {
                SetupStepBlock(
                    number = 1,
                    title = "Participant",
                    ready = hasSavedParticipantCode,
                    active = activeStep == SetupStep.PARTICIPANT,
                    enabled = true,
                    onOpen = { expandedStep = SetupStep.PARTICIPANT },
                ) {
                    ParticipantStepContent(
                        studyCode = studyCode,
                        hasSavedParticipantCode = hasSavedParticipantCode,
                        onStudyCodeChange = onStudyCodeChange,
                        onSave = onSave,
                        onDone = { expandedStep = null },
                    )
                }
                ReduDivider(Modifier.padding(horizontal = 16.dp))
                SetupStepBlock(
                    number = 2,
                    title = "Platforms",
                    ready = anyPlatformEnabled,
                    active = activeStep == SetupStep.PLATFORMS,
                    enabled = hasSavedParticipantCode,
                    onOpen = { expandedStep = SetupStep.PLATFORMS },
                ) {
                    PlatformStepContent(
                        trackTikTokEnabled = trackTikTokEnabled,
                        trackInstagramEnabled = trackInstagramEnabled,
                        trackFacebookEnabled = trackFacebookEnabled,
                        onPlatformTrackingChange = { platform, enabled ->
                            expandedStep = SetupStep.PLATFORMS
                            onPlatformTrackingChange(platform, enabled)
                        },
                        onContinue = { expandedStep = null },
                    )
                }
                ReduDivider(Modifier.padding(horizontal = 16.dp))
                SetupStepBlock(
                    number = 3,
                    title = "Monitoring permission",
                    ready = accessibilityEnabled,
                    active = activeStep == SetupStep.MONITORING,
                    enabled = anyPlatformEnabled,
                    onOpen = { expandedStep = SetupStep.MONITORING },
                ) {
                    MonitoringPermissionContent(
                        accessibilityEnabled = accessibilityEnabled,
                        onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                    )
                }
            }
        }

        if (currentStep == SetupStep.COMPLETE) {
            item {
                ReduEmptyState(
                    title = "REDU is ready",
                    body = "Selected platforms can now be monitored while you use them. Processing stays on this device.",
                    actionLabel = "Go to Today",
                    onAction = onFinish,
                )
            }
        }
    }
}

@Composable
private fun SetupProgressSegments(completedSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier.weight(1f).height(6.dp).background(
                    color = if (index < completedSteps) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    shape = MaterialTheme.shapes.extraSmall,
                ),
            )
        }
    }
}

@Composable
private fun SetupStepBlock(
    number: Int,
    title: String,
    ready: Boolean,
    active: Boolean,
    enabled: Boolean,
    onOpen: () -> Unit,
    content: @Composable () -> Unit,
) {
    val largeText = LocalDensity.current.fontScale >= 1.5f
    Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Column(
            modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, role = Role.Button, onClick = onOpen)
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(if (largeText) 10.dp else 0.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(30.dp),
                    color = if (ready) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = if (ready) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (ready) {
                            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        } else {
                            Text(number.toString(), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Text(
                    title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!largeText) {
                    ReduStatusLabel(if (ready) "Ready" else "Required", if (ready) StatusTone.SUCCESS else StatusTone.ATTENTION)
                }
                Icon(
                    if (active) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (largeText) {
                Box(modifier = Modifier.padding(start = 42.dp)) {
                    ReduStatusLabel(if (ready) "Ready" else "Required", if (ready) StatusTone.SUCCESS else StatusTone.ATTENTION)
                }
            }
        }
        if (active) {
            ReduDivider(Modifier.padding(horizontal = 16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun ParticipantStepContent(
    studyCode: String,
    hasSavedParticipantCode: Boolean,
    onStudyCodeChange: (String) -> Unit,
    onSave: () -> Unit,
    onDone: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = studyCode,
        onValueChange = onStudyCodeChange,
        label = { Text("Participant study code") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
    )
    if (studyCode.isNotBlank()) {
        ReduInfoRow("Assigned group", studyGroupForParticipantCode(studyCode).name.lowercase().replaceFirstChar { it.uppercase() })
    }
    ReduCaption("The study code links local exports to the assigned participant without using a name or email address.")
    ReduPrimaryButton(
        text = if (hasSavedParticipantCode) "Save changes" else "Save participant code",
        onClick = {
            focusManager.clearFocus()
            onSave()
            onDone()
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = studyCode.isNotBlank(),
    )
}

@Composable
private fun PlatformStepContent(
    trackTikTokEnabled: Boolean,
    trackInstagramEnabled: Boolean,
    trackFacebookEnabled: Boolean,
    onPlatformTrackingChange: (Platform, Boolean) -> Unit,
    onContinue: () -> Unit,
) {
    ReduCaption("Choose only the platforms used for short-form video during the study.")
    PlatformToggleRow("TikTok", trackTikTokEnabled) { onPlatformTrackingChange(Platform.TIKTOK, it) }
    ReduDivider()
    PlatformToggleRow("Instagram", trackInstagramEnabled) { onPlatformTrackingChange(Platform.INSTAGRAM, it) }
    ReduDivider()
    PlatformToggleRow("Facebook", trackFacebookEnabled) { onPlatformTrackingChange(Platform.FACEBOOK, it) }
    val anyEnabled = trackTikTokEnabled || trackInstagramEnabled || trackFacebookEnabled
    ReduPrimaryButton(
        text = "Continue",
        onClick = onContinue,
        modifier = Modifier.fillMaxWidth(),
        enabled = anyEnabled,
    )
}

@Composable
internal fun PlatformToggleRow(
    label: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, role = Role.Switch) { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        ReduSwitch(checked = checked, enabled = enabled, onCheckedChange = onCheckedChange, label = "$label monitoring")
    }
}

@Composable
private fun MonitoringPermissionContent(
    accessibilityEnabled: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("REDU Monitoring Service", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            ReduCaption(if (accessibilityEnabled) "Android permission is enabled" else "Android permission is still required")
        }
        ReduStatusLabel(if (accessibilityEnabled) "On" else "Required", if (accessibilityEnabled) StatusTone.SUCCESS else StatusTone.ATTENTION)
    }
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.small,
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Private by design", style = MaterialTheme.typography.titleSmall)
            ReduCaption("Raw text and temporary screen frames are processed locally and are not retained in study exports.")
        }
    }
    ReduOutlinedButton(
        text = if (accessibilityEnabled) "Review Android settings" else "Open Android settings",
        onClick = onOpenAccessibilitySettings,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
internal fun ExportScreen(
    padding: PaddingValues,
    state: ExportUiState,
    onExport: () -> Unit,
    onBack: () -> Unit,
) {
    val datasets = remember {
        listOf(
            "Session history" to "sessions.csv",
            "Daily summaries" to "daily_summaries.csv",
            "Study periods" to "study_periods.csv",
            "Prompt events" to "prompt_events.csv",
            "Reliability events" to "reliability_events.csv",
            "Risk personalization" to "risk_personalization.csv",
        )
    }
    ReduScreen(
        padding = padding,
        title = "Export study data",
        subtitle = "Create a local ZIP for the research team",
        onBack = onBack,
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Aggregate data only", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Raw captions, comments, and screen images are never included. REDU creates the ZIP on this device before opening Android's share sheet.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (state is ExportUiState.Preparing) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            strokeCap = StrokeCap.Round,
                        )
                    }
                    ReduPrimaryButton(
                        text = if (state is ExportUiState.Preparing) "Preparing export" else "Create and share ZIP",
                        onClick = onExport,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state !is ExportUiState.Preparing,
                        icon = Icons.Outlined.FileUpload,
                    )
                    when (state) {
                        ExportUiState.Idle -> Unit
                        ExportUiState.Preparing -> ReduCaption("Packaging the saved datasets. Keep REDU open for a moment.")
                        is ExportUiState.Ready -> ReduStatusLabel("Export ready", StatusTone.SUCCESS)
                        is ExportUiState.Error -> Text(state.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        item {
            ReduSectionHeader(title = "Included datasets", subtitle = "Six CSV files")
        }
        item {
            ReduSection {
                datasets.forEachIndexed { index, (label, fileName) ->
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        ReduCaption(fileName)
                    }
                    if (index < datasets.lastIndex) ReduDivider(Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Preview(
    name = "Today populated",
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    backgroundColor = 0xFF0B0E0D,
)
@Composable
private fun DashboardPopulatedPreview() {
    val now = Instant.parse("2026-07-11T12:00:00Z").toEpochMilli()
    val session = SessionEntity(
        studyCode = "P01X",
        studyGroup = StudyGroup.INTERVENTION,
        platform = Platform.TIKTOK,
        startedAtMillis = Instant.parse("2026-07-11T10:00:00Z").toEpochMilli(),
        endedAtMillis = now,
        rawDurationMillis = 8_520_000L,
        promptExcludedDurationMillis = 8_520_000L,
        meanDwellMillis = 18_000L,
        swipeCount = 142,
        resolvableUnits = 88,
        negativeUnits = 31,
        oovRatio = 0.12,
        nsdPercent = 35.2,
        riskScore = 64.0,
        riskLevel = RiskLevel.WARNING,
        sentimentReliability = SentimentReliability.RELIABLE,
    )
    ReduTheme {
        DashboardScreen(
            padding = PaddingValues(0.dp),
            state = dashboardUiState(
                sessions = listOf(session),
                setupComplete = true,
                accessibilityEnabled = true,
                trackTikTokEnabled = true,
                trackInstagramEnabled = true,
                trackFacebookEnabled = false,
                nowMillis = now,
                zoneId = ZoneId.of("Asia/Manila"),
            ),
            onOpenSetup = {},
            onOpenHistory = {},
        )
    }
}

@Preview(
    name = "Today large font",
    widthDp = 320,
    heightDp = 800,
    fontScale = 1.3f,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    backgroundColor = 0xFF0B0E0D,
)
@Composable
private fun DashboardLargeFontPreview() {
    ReduTheme {
        DashboardScreen(
            padding = PaddingValues(0.dp),
            state = dashboardUiState(
                sessions = emptyList(),
                setupComplete = false,
                accessibilityEnabled = false,
                trackTikTokEnabled = true,
                trackInstagramEnabled = false,
                trackFacebookEnabled = false,
                nowMillis = Instant.parse("2026-07-11T12:00:00Z").toEpochMilli(),
                zoneId = ZoneId.of("Asia/Manila"),
            ),
            onOpenSetup = {},
            onOpenHistory = {},
        )
    }
}
