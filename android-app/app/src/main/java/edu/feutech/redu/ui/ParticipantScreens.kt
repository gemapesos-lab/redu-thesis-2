package edu.feutech.redu.ui

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.outlined.History
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.feutech.redu.BuildConfig
import edu.feutech.redu.R
import edu.feutech.redu.data.Platform
import edu.feutech.redu.data.RiskLevel
import edu.feutech.redu.data.SentimentReliability
import edu.feutech.redu.data.SessionEntity
import edu.feutech.redu.data.StudyGroup
import edu.feutech.redu.export.CsvExporter
import edu.feutech.redu.ui.theme.ReduTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DashboardScreen(
    padding: PaddingValues,
    state: DashboardUiState,
    onOpenSetup: () -> Unit,
) {
    var scoreInfoOpen by rememberSaveable { mutableStateOf(false) }
    var diagnosticsExpanded by rememberSaveable { mutableStateOf(false) }

    ReduScreen(
        padding = padding,
        title = "Today",
        subtitle = state.date.formatDashboardDate(),
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
            ReduSectionHeader(title = "Your last 7 days", subtitle = "Active time by day")
        }

        item {
            WeeklyActivityChart(state.weeklyActivity)
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
                    text = if (summary.todaySessionCount == 0) "No activity" else summary.todayActiveMillis.formatDashboardDuration(),
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
            ReduCaption("Today follows the study timezone (${CsvExporter.STUDY_ZONE.id}).")
        }
        ReduDivider()
    }
}

@Composable
private fun WeeklyActivityChart(activity: List<DailyActivityPoint>) {
    val totalMillis = activity.sumOf { it.activeMillis }
    val sessionCount = activity.sumOf { it.sessionCount }
    val activeDays = activity.count { it.sessionCount > 0 }
    val maxMillis = activity.maxOfOrNull { it.activeMillis }?.coerceAtLeast(1L) ?: 1L
    val dates = activity.map { it.date.toEpochDay() }
    var selectedEpochDay by rememberSaveable(dates) {
        mutableStateOf(activity.lastOrNull()?.date?.toEpochDay())
    }
    LaunchedEffect(dates) {
        if (selectedEpochDay !in dates) selectedEpochDay = activity.lastOrNull()?.date?.toEpochDay()
    }
    val selectedPoint = activity.firstOrNull { it.date.toEpochDay() == selectedEpochDay }
        ?: activity.lastOrNull()

    if (activity.none { it.sessionCount > 0 }) {
        WeeklyActivityEmptyState()
        return
    }

    ReduSection {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(totalMillis.formatDashboardDuration(), style = MaterialTheme.typography.titleLarge)
                    ReduCaption("Total active time")
                }
                ReduCaption("$sessionCount ${sessionCount.sessionLabel()}")
            }

            selectedPoint?.let { point ->
                WeeklySelectedDaySummary(point)
            }

            WeeklyActivityBars(
                activity = activity,
                maxMillis = maxMillis,
                selectedEpochDay = selectedEpochDay,
                onSelect = { selectedEpochDay = it.date.toEpochDay() },
            )

            ReduDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ReduInfoMetric(label = "Active days", value = "$activeDays of 7")
                ReduInfoMetric(
                    label = "Average session",
                    value = (totalMillis / sessionCount).formatDashboardDuration(),
                    alignEnd = true,
                )
            }
        }
    }
}

@Composable
private fun WeeklySelectedDaySummary(point: DailyActivityPoint) {
    val largeText = LocalDensity.current.fontScale >= 1.3f
    val dateLabel = point.date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US))
    val activityLabel = "${point.activeMillis.formatDashboardDuration()} · ${point.sessionCount} ${point.sessionCount.sessionLabel()}"
    Surface(
        modifier = Modifier.fillMaxWidth().semantics {
            contentDescription = "$dateLabel. $activityLabel"
        },
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.small,
    ) {
        if (largeText) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(dateLabel, style = MaterialTheme.typography.titleSmall)
                ReduCaption(activityLabel)
            }
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(dateLabel, style = MaterialTheme.typography.titleSmall)
                ReduCaption(activityLabel)
            }
        }
    }
}

@Composable
private fun WeeklyActivityBars(
    activity: List<DailyActivityPoint>,
    maxMillis: Long,
    selectedEpochDay: Long?,
    onSelect: (DailyActivityPoint) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth().height(112.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        activity.forEach { point ->
            val selected = point.date.toEpochDay() == selectedEpochDay
            val dateLabel = point.date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US))
            val durationLabel = point.activeMillis.formatDashboardDuration()
            val semanticsLabel = "$dateLabel: $durationLabel, ${point.sessionCount} ${point.sessionCount.sessionLabel()}"
            val labelColor by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(180),
                label = "weekly day label",
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(112.dp)
                    .semantics {
                        role = Role.Button
                        this.selected = selected
                        contentDescription = semanticsLabel
                    }
                    .clickable(role = Role.Button) { onSelect(point) }
                    .padding(horizontal = 1.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                BoxWithConstraints(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    val proportionalHeight = maxHeight * (point.activeMillis.toFloat() / maxMillis.toFloat())
                    val targetHeight = if (point.activeMillis == 0L) 0.dp else proportionalHeight.coerceAtLeast(6.dp)
                    val barHeight by animateDpAsState(targetHeight, tween(220), label = "weekly activity bar")
                    val barColor by animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.48f),
                        animationSpec = tween(180),
                        label = "weekly activity color",
                    )
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .width(20.dp)
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.extraSmall),
                    )
                    if (point.activeMillis > 0L) {
                        Box(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .width(20.dp)
                                .height(barHeight)
                                .background(barColor, MaterialTheme.shapes.extraSmall),
                        )
                    }
                }
                Text(
                    point.date.format(DateTimeFormatter.ofPattern("EEE", Locale.US)).take(2),
                    style = MaterialTheme.typography.labelMedium,
                    color = labelColor,
                    textAlign = TextAlign.Center,
                )
                Box(
                    Modifier
                        .width(18.dp)
                        .height(2.dp)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            MaterialTheme.shapes.extraSmall,
                        ),
                )
            }
        }
    }
}

@Composable
private fun WeeklyActivityEmptyState() {
    ReduSection {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text("No activity in the last 7 days", style = MaterialTheme.typography.titleMedium)
            ReduSecondaryText("Your seven-day activity will appear here after REDU saves a session.")
        }
    }
}

private fun Long.formatDashboardDuration(): String = formatMetricDuration()

@Composable
private fun ReduInfoMetric(label: String, value: String, alignEnd: Boolean = false) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        ReduCaption(label)
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
            BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                val stackFilters = maxWidth < 360.dp || LocalDensity.current.fontScale >= 1.5f
                if (stackFilters) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CompactFilterMenu(
                            selectedLabel = platformFilter.displayName(),
                            options = PlatformFilter.entries.toList(),
                            optionLabel = { it.displayName() },
                            onOptionSelected = { platformFilter = it },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        CompactFilterMenu(
                            selectedLabel = riskFilter.displayName(),
                            options = RiskFilter.entries.toList(),
                            optionLabel = { it.displayName() },
                            onOptionSelected = { riskFilter = it },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                }
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
                    icon = Icons.Outlined.History,
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
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
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
    participantCodeLocked: Boolean = false,
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
                        participantCodeLocked = participantCodeLocked,
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
    participantCodeLocked: Boolean,
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
        enabled = !participantCodeLocked,
        shape = MaterialTheme.shapes.medium,
    )
    if (studyCode.isNotBlank()) {
        ReduInfoRow("Assigned group", studyGroupForParticipantCode(studyCode).name.lowercase().replaceFirstChar { it.uppercase() })
    }
    ReduCaption(
        if (participantCodeLocked) {
            "The participant code is locked while saved sessions exist. Reset study data before assigning a different participant."
        } else {
            "The study code links local exports to the assigned participant without using a name or email address."
        },
    )
    if (!participantCodeLocked) {
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
    PlatformToggleRow(Platform.TIKTOK, trackTikTokEnabled) { onPlatformTrackingChange(Platform.TIKTOK, it) }
    ReduDivider()
    PlatformToggleRow(Platform.INSTAGRAM, trackInstagramEnabled) { onPlatformTrackingChange(Platform.INSTAGRAM, it) }
    ReduDivider()
    PlatformToggleRow(Platform.FACEBOOK, trackFacebookEnabled) { onPlatformTrackingChange(Platform.FACEBOOK, it) }
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
    platform: Platform,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    val label = platform.displayName()
    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, role = Role.Switch) { onCheckedChange(!checked) }
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlatformAppIcon(platform)
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
        ReduSwitch(checked = checked, enabled = enabled, onCheckedChange = onCheckedChange, label = "$label monitoring")
    }
}

@Composable
private fun PlatformAppIcon(platform: Platform) {
    val iconRes = when (platform) {
        Platform.TIKTOK -> R.drawable.ic_tiktok
        Platform.INSTAGRAM -> R.drawable.ic_instagram
        Platform.FACEBOOK -> R.drawable.ic_facebook
    }
    Surface(
        modifier = Modifier.size(34.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(19.dp),
                tint = Color.Unspecified,
            )
        }
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
                        is ExportUiState.Ready -> {
                            ReduStatusLabel("Export ready", StatusTone.SUCCESS)
                            ReduCaption("Created ${state.fileName}")
                        }
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
                nowMillis = now,
                zoneId = ZoneId.of("Asia/Manila"),
            ),
            onOpenSetup = {},
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
                nowMillis = Instant.parse("2026-07-11T12:00:00Z").toEpochMilli(),
                zoneId = ZoneId.of("Asia/Manila"),
            ),
            onOpenSetup = {},
        )
    }
}
