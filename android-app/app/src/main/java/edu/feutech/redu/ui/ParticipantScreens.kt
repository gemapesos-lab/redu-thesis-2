package edu.feutech.redu.ui

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import edu.feutech.redu.BuildConfig
import edu.feutech.redu.R
import edu.feutech.redu.data.Platform
import edu.feutech.redu.data.RiskLevel
import edu.feutech.redu.data.SentimentReliability
import edu.feutech.redu.data.SessionEntity
import edu.feutech.redu.data.StudyGroup
import edu.feutech.redu.ui.sky.BlurredSkyBackdrop
import edu.feutech.redu.ui.theme.ReduInlineIconSize
import edu.feutech.redu.ui.theme.ReduPalette
import edu.feutech.redu.ui.theme.ReduTheme
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
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
        title = state.date.formatDashboardDate(),
        wash = false,
        quietTitle = true,
        backdrop = { HomeHalftoneBackdrop(Modifier.fillMaxSize()) },
    ) {
        if (!state.setupComplete) {
            item {
                ReduAttentionBanner(
                    title = "Monitoring needs attention",
                    actionLabel = "Review setup",
                    onAction = onOpenSetup,
                    modifier = Modifier.padding(bottom = 24.dp),
                )
            }
        }

        item {
            DailyOverview(summary = state.summary)
        }

        item {
            TodayFacts(
                summary = state.summary,
                onOpenScoreInfo = { scoreInfoOpen = true },
            )
        }

        item {
            ReduSectionHeader(
                title = "Your last 12 weeks",
                subtitle = "Active time by day",
            )
        }

        item {
            WeeklyActivityChart(state.dailyActivity)
        }

        if (BuildConfig.DEBUG) {
            item {
                ReduSectionHeader(title = "Research diagnostics", quiet = true)
            }
            item {
                HomeFrostedSection {
                    ReduSettingRow(
                        title = "Signal details",
                        onClick = { diagnosticsExpanded = !diagnosticsExpanded },
                        trailing = {
                            Icon(
                                painterResource(if (diagnosticsExpanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down),
                                contentDescription = if (diagnosticsExpanded) "Hide signal details" else "Show signal details",
                                modifier = Modifier.size(ReduInlineIconSize),
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
            shape = MaterialTheme.shapes.extraLarge,
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
private fun DailyOverview(summary: DashboardSummary) {
    val largeText = LocalDensity.current.fontScale >= 1.3f
    val duration = if (summary.todaySessionCount == 0) "No activity" else summary.todayActiveMillis.formatDashboardDuration()
    val showRing = summary.todaySessionCount > 0 && summary.latestRiskScore != null
    val density = LocalDensity.current
    val durationShadow = remember(density.density, density.fontScale) {
        with(density) {
            Shadow(
                color = Color(0x1C142846),
                offset = Offset(0f, 4.dp.toPx()),
                blurRadius = 16.dp.toPx(),
            )
        }
    }
    val shape = MaterialTheme.shapes.medium
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = shape,
        tonalElevation = 0.dp,
    ) {
        Box {
            BlurredSkyBackdrop(Modifier.matchParentSize())
            val copy = @Composable {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Today",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = duration,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.displaySmall.copy(shadow = durationShadow),
                    )
                    summary.peakRiskLevel?.let { level ->
                        val presentation = activityPatternFor(level)
                        ReduStatusLabel(presentation.label, presentation.tone)
                    }
                }
            }
            if (largeText || !showRing) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    copy()
                    if (showRing) ActivityScoreRing(summary.latestRiskScore ?: 0.0)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(1f)) { copy() }
                    ActivityScoreRing(summary.latestRiskScore ?: 0.0)
                }
            }
        }
    }
}

@Composable
private fun TodayFacts(
    summary: DashboardSummary,
    onOpenScoreInfo: () -> Unit,
) {
    val pattern = summary.latestSession?.riskLevel?.let { activityPatternFor(it).label } ?: "None"
    Column(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp)) {
        ReduInfoRow(
            "Sessions today",
            if (summary.todaySessionCount == 0) "None" else summary.todaySessionCount.toString(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                ReduInfoRow("Latest pattern", pattern)
            }
            if (summary.latestRiskScore != null) {
                IconButton(onClick = onOpenScoreInfo, modifier = Modifier.size(48.dp)) {
                    Icon(
                        painterResource(R.drawable.ic_info),
                        contentDescription = "What this means",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyActivityChart(activity: List<DailyActivityPoint>) {
    val totalMillis = activity.sumOf { it.activeMillis }
    val sessionCount = activity.sumOf { it.sessionCount }
    val activeDays = activity.count { it.sessionCount > 0 }
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

    HomeFrostedSection(Modifier.padding(bottom = 8.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
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

            ActivityHeatmap(
                activity = activity,
                selectedEpochDay = selectedEpochDay,
                onSelect = { selectedEpochDay = it.date.toEpochDay() },
            )

            ReduDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ReduInfoMetric(label = "Active days", value = "$activeDays of ${activity.size}")
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

private val HeatmapCellGap = 3.dp
private val HeatmapDayGutter = 14.dp
private val HeatmapCellShape = RoundedCornerShape(2.dp)
private val HeatmapDayLetters = listOf("M", "T", "W", "T", "F", "S", "S")

@Composable
private fun ActivityHeatmap(
    activity: List<DailyActivityPoint>,
    selectedEpochDay: Long?,
    onSelect: (DailyActivityPoint) -> Unit,
) {
    val columns = remember(activity) { activityWeekColumns(activity) }
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMM", Locale.US) }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columnCount = columns.size.coerceAtLeast(1)
        val cell = ((maxWidth - HeatmapDayGutter - HeatmapCellGap * columnCount) / columnCount)
            .coerceIn(8.dp, 22.dp)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(HeatmapCellGap)) {
                Row(horizontalArrangement = Arrangement.spacedBy(HeatmapCellGap)) {
                    Spacer(Modifier.width(HeatmapDayGutter).height(16.dp))
                    columns.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier.width(cell).height(16.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            heatmapMonthLabel(columns, index, monthFormatter)?.let { label ->
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Visible,
                                )
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(HeatmapCellGap)) {
                    Column(verticalArrangement = Arrangement.spacedBy(HeatmapCellGap)) {
                        HeatmapDayLetters.forEach { letter ->
                            Box(
                                modifier = Modifier.width(HeatmapDayGutter).height(cell),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = letter,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                    columns.forEach { column ->
                        Column(verticalArrangement = Arrangement.spacedBy(HeatmapCellGap)) {
                            column.forEach { point ->
                                if (point == null) {
                                    Spacer(Modifier.size(cell))
                                } else {
                                    ActivityHeatCell(
                                        point = point,
                                        cell = cell,
                                        selected = point.date.toEpochDay() == selectedEpochDay,
                                        onSelect = { onSelect(point) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            ActivityHeatLegend()
        }
    }
}

@Composable
private fun ActivityHeatCell(
    point: DailyActivityPoint,
    cell: Dp,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val dateLabel = point.date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US))
    val durationLabel = point.activeMillis.formatDashboardDuration()
    val semanticsLabel = "$dateLabel: $durationLabel, ${point.sessionCount} ${point.sessionCount.sessionLabel()}"
    Box(
        modifier = Modifier
            .size(cell)
            .clip(HeatmapCellShape)
            .semantics {
                role = Role.Button
                this.selected = selected
                contentDescription = semanticsLabel
            }
            .clickable(role = Role.Button) { onSelect() }
            .background(activityHeatColor(activityHeatLevel(point.activeMillis)))
            .then(
                if (selected) Modifier.border(1.dp, ReduPalette.Persimmon, HeatmapCellShape) else Modifier,
            ),
    )
}

@Composable
private fun ActivityHeatLegend() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Less",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        (0..4).forEach { level ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(activityHeatColor(level)),
            )
        }
        Text(
            text = "More",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun activityHeatColor(level: Int): Color = when (level.coerceIn(0, 4)) {
    0 -> ReduPalette.Figure.copy(alpha = 0.12f)
    1 -> ReduPalette.Sage.copy(alpha = 0.35f)
    2 -> ReduPalette.Sage.copy(alpha = 0.55f)
    3 -> ReduPalette.Sage.copy(alpha = 0.78f)
    else -> ReduPalette.Sage
}

private fun activityWeekColumns(activity: List<DailyActivityPoint>): List<List<DailyActivityPoint?>> {
    if (activity.isEmpty()) return emptyList()
    val byDate = activity.associateBy { it.date }
    val first = activity.minOf { it.date }
    val last = activity.maxOf { it.date }
    val start = first.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val endWeek = last.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val columns = mutableListOf<List<DailyActivityPoint?>>()
    var weekStart = start
    while (!weekStart.isAfter(endWeek)) {
        columns += (0L..6L).map { offset ->
            val date = weekStart.plusDays(offset)
            if (date.isBefore(first) || date.isAfter(last)) null else byDate[date]
        }
        weekStart = weekStart.plusWeeks(1)
    }
    return columns
}

private fun heatmapMonthLabel(
    columns: List<List<DailyActivityPoint?>>,
    index: Int,
    formatter: DateTimeFormatter,
): String? {
    val monthDate = columns[index].firstNotNullOfOrNull { it?.date } ?: return null
    if (index > 0) {
        val previous = columns[index - 1].firstNotNullOfOrNull { it?.date }
        if (previous?.year == monthDate.year && previous.month == monthDate.month) return null
    }
    return monthDate.format(formatter)
}

@Composable
private fun WeeklyActivityEmptyState() {
    HomeFrostedSection {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ReduBlobatar(
                expression = BlobatarExpression.Sleepy,
                modifier = Modifier.size(120.dp).align(Alignment.CenterHorizontally),
            )
            Text("No activity in the last 12 weeks", style = MaterialTheme.typography.titleMedium)
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
        subtitle = if (sessions.isEmpty()) null else "${filtered.size} of ${sessions.size} sessions",
        pinHeader = true,
        actions = {
            if (sessions.isNotEmpty()) {
                Box {
                    IconButton(onClick = { historyMenuExpanded = true }, modifier = Modifier.size(48.dp)) {
                        Icon(painterResource(R.drawable.ic_more), contentDescription = "History actions")
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                Text("Platform", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FilterChipRow(
                    options = PlatformFilter.entries.toList(),
                    selected = platformFilter,
                    label = { if (it == PlatformFilter.ALL) "All" else it.displayName() },
                    onSelected = { platformFilter = it },
                )
                Text("Activity pattern", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FilterChipRow(
                    options = RiskFilter.entries.toList(),
                    selected = riskFilter,
                    label = { if (it == RiskFilter.ALL) "All" else it.displayName() },
                    onSelected = { riskFilter = it },
                )
            }
        }

        if (filtered.isEmpty()) {
            item {
                ReduEmptyState(
                    title = if (sessions.isEmpty()) "No sessions yet" else "No matching sessions",
                    body = if (sessions.isNotEmpty()) {
                        "Change one of the filters to see more of your saved history."
                    } else {
                        null
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
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.extraLarge,
            title = { Text("Clear history?") },
            text = {
                Text("This permanently deletes saved sessions, prompt events, and reliability logs. Participant settings and downloaded models stay on this device.")
            },
            confirmButton = {
                ReduTextButton(
                    text = "Clear history",
                    onClick = {
                        clearHistoryDialogOpen = false
                        onClearHistory()
                    },
                    contentColor = MaterialTheme.colorScheme.error,
                )
            },
            dismissButton = {
                ReduTextButton(text = "Cancel", onClick = { clearHistoryDialogOpen = false })
            },
        )
    }
}

@Composable
private fun <T> FilterChipRow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            ReduChip(
                label = label(option),
                selected = option == selected,
                onClick = { onSelected(option) },
            )
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
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlatformAppIcon(session.platform)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(session.platform.displayName(), style = MaterialTheme.typography.titleMedium)
                ReduCaption(session.startedAtMillis.formatTimeOfDay())
            }
            Text(
                formatReadableDuration(session.rawDurationMillis),
                style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
            )
            Icon(
                painterResource(if (expanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down),
                contentDescription = if (expanded) "Hide session details" else "Show session details",
                modifier = Modifier.size(ReduInlineIconSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (expanded) {
            ActivityPatternMeter(session.riskScore)
            ReduDivider()
            ReduInfoRow("Score", "${session.riskScore.formatOneDecimal()}/100")
            ReduInfoRow("Reliability", session.sentimentReliability.displayName())
            ReduInfoRow("Pattern range", "${presentation.label} (${presentation.rangeLabel})")
            ReduInfoRow("Mean dwell", session.meanDwellMillis.formatMetricDuration())
            ReduInfoRow("Transitions", session.swipeCount.toString())
            ReduInfoRow("Negative-content density", session.nsdPercent?.formatPercentValue() ?: "Unavailable")
            ReduInfoRow("Resolved items", session.resolvableUnits.toString())
            ReduInfoRow("Negative items", session.negativeUnits.toString())
            ReduInfoRow("Unrecognized text", session.oovRatio.formatPercentRatio())
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
    var viewingStep by rememberSaveable { mutableStateOf(currentStep) }
    val focusManager = LocalFocusManager.current

    fun retreat() {
        when (viewingStep) {
            SetupStep.PARTICIPANT -> onBack?.invoke()
            SetupStep.PLATFORMS -> viewingStep = SetupStep.PARTICIPANT
            SetupStep.MONITORING -> viewingStep = SetupStep.PLATFORMS
            SetupStep.COMPLETE -> viewingStep = SetupStep.MONITORING
        }
    }

    val showBack = onBack != null || viewingStep != SetupStep.PARTICIPANT
    val helper = when (viewingStep) {
        SetupStep.PARTICIPANT -> "Your code stays on this device"
        SetupStep.PLATFORMS -> "Select at least one platform"
        SetupStep.MONITORING -> "You can pause this in Settings"
        SetupStep.COMPLETE -> null
    }
    Box(modifier = Modifier.fillMaxSize()) {
        ReduHeroWash()
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (showBack) {
                        IconButton(onClick = ::retreat, modifier = Modifier.size(48.dp)) {
                            Icon(painterResource(R.drawable.ic_back), contentDescription = "Back")
                        }
                    } else {
                        Spacer(Modifier.size(48.dp))
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        SetupProgressDots(activeIndex = viewingStep.ordinal.coerceAtMost(2))
                    }
                    Spacer(Modifier.size(48.dp))
                }
                when (viewingStep) {
                    SetupStep.PARTICIPANT -> {
                        Text("Participant", style = MaterialTheme.typography.headlineSmall)
                        ReduSection {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                ReduTextField(
                                    value = studyCode,
                                    onValueChange = onStudyCodeChange,
                                    label = "Participant study code",
                                    enabled = !participantCodeLocked,
                                )
                                if (studyCode.isNotBlank()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        ReduCaption("Assigned group")
                                        ReduChip(
                                            label = studyGroupForParticipantCode(studyCode).name.lowercase()
                                                .replaceFirstChar { it.uppercase() },
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                            contentColor = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                }
                                if (participantCodeLocked) {
                                    ReduCaption("The participant code is locked while saved sessions exist. Reset study data before assigning a different participant.")
                                }
                            }
                        }
                    }
                    SetupStep.PLATFORMS -> {
                        Text("Platforms", style = MaterialTheme.typography.headlineSmall)
                        ReduSection {
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                PlatformToggleRow(Platform.TIKTOK, trackTikTokEnabled) { onPlatformTrackingChange(Platform.TIKTOK, it) }
                                ReduDivider()
                                PlatformToggleRow(Platform.INSTAGRAM, trackInstagramEnabled) { onPlatformTrackingChange(Platform.INSTAGRAM, it) }
                                ReduDivider()
                                PlatformToggleRow(Platform.FACEBOOK, trackFacebookEnabled) { onPlatformTrackingChange(Platform.FACEBOOK, it) }
                            }
                        }
                    }
                    SetupStep.MONITORING -> {
                        Text("Monitoring permission", style = MaterialTheme.typography.headlineSmall)
                        ReduSection {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "REDU Monitoring Service",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                ReduStatusLabel(
                                    if (accessibilityEnabled) "On" else "Required",
                                    if (accessibilityEnabled) StatusTone.SUCCESS else StatusTone.ATTENTION,
                                )
                            }
                            ReduDivider(Modifier.padding(horizontal = 16.dp))
                            Column(
                                Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text("Private by design", style = MaterialTheme.typography.titleSmall)
                                ReduCaption("Raw text and temporary screen frames are processed locally and are not retained in study exports.")
                            }
                        }
                        if (accessibilityEnabled) {
                            ReduTextButton(
                                text = "Review Android settings",
                                onClick = onOpenAccessibilitySettings,
                                modifier = Modifier.fillMaxWidth(),
                                contentColor = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    SetupStep.COMPLETE -> {
                        Text("Setup ready", style = MaterialTheme.typography.headlineSmall)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                helper?.let { text ->
                    Text(
                        text,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                when (viewingStep) {
                    SetupStep.PARTICIPANT -> ReduPrimaryButton(
                        text = if (participantCodeLocked) "Continue" else if (hasSavedParticipantCode) "Save changes" else "Save participant code",
                        onClick = {
                            if (!participantCodeLocked) {
                                focusManager.clearFocus()
                                onSave()
                            }
                            viewingStep = SetupStep.PLATFORMS
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = studyCode.isNotBlank(),
                    )
                    SetupStep.PLATFORMS -> ReduPrimaryButton(
                        text = "Continue",
                        onClick = { viewingStep = SetupStep.MONITORING },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = anyPlatformEnabled,
                    )
                    SetupStep.MONITORING -> ReduPrimaryButton(
                        text = if (accessibilityEnabled) "Continue" else "Open Android settings",
                        onClick = {
                            if (accessibilityEnabled) viewingStep = SetupStep.COMPLETE else onOpenAccessibilitySettings()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SetupStep.COMPLETE -> ReduPrimaryButton(
                        text = "Go to Home",
                        onClick = onFinish,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupProgressDots(activeIndex: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val current = index == activeIndex
            Box(
                modifier = Modifier
                    .size(if (current) 8.dp else 6.dp)
                    .background(
                        color = if (index <= activeIndex) ReduPalette.Figure else ReduPalette.OutlineVariant,
                        shape = CircleShape,
                    ),
            )
        }
    }
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
    Image(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = Modifier.size(36.dp),
        contentScale = ContentScale.Fit,
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
        title = "Export",
        subtitle = null,
        onBack = onBack,
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Aggregate data only", style = MaterialTheme.typography.headlineSmall)
                ReduSecondaryText("Raw captions, comments, and screen images are never included.")
                if (state is ExportUiState.Preparing) {
                    ReduLinearProgress()
                    ReduCaption("Keep REDU open for a moment.")
                }
                when (state) {
                    ExportUiState.Idle, ExportUiState.Preparing -> Unit
                    is ExportUiState.Ready -> {
                        ReduStatusLabel("Export ready", StatusTone.SUCCESS)
                        ReduCaption("Created ${state.fileName}")
                    }
                    is ExportUiState.Error -> Text(state.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
                ReduPrimaryButton(
                    text = if (state is ExportUiState.Preparing) "Preparing export" else "Create and share ZIP",
                    onClick = onExport,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state !is ExportUiState.Preparing,
                    icon = R.drawable.ic_upload,
                )
            }
        }

        item {
            ReduSectionHeader(title = "Included datasets", quiet = true)
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
    backgroundColor = 0xFF0A0A0A,
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
    backgroundColor = 0xFF0A0A0A,
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

@Preview(name = "History empty", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun HistoryEmptyPreview() {
    ReduTheme {
        HistoryScreen(padding = PaddingValues(0.dp), sessions = emptyList(), onClearHistory = {})
    }
}

@Preview(name = "History populated", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun HistoryPopulatedPreview() {
    val session = SessionEntity(
        studyCode = "P01X",
        studyGroup = StudyGroup.INTERVENTION,
        platform = Platform.INSTAGRAM,
        startedAtMillis = Instant.parse("2026-07-11T10:00:00Z").toEpochMilli(),
        endedAtMillis = Instant.parse("2026-07-11T10:20:00Z").toEpochMilli(),
        rawDurationMillis = 1_200_000L,
        promptExcludedDurationMillis = 1_200_000L,
        meanDwellMillis = 12_000L,
        swipeCount = 40,
        resolvableUnits = 20,
        negativeUnits = 4,
        oovRatio = 0.08,
        nsdPercent = 18.0,
        riskScore = 22.0,
        riskLevel = RiskLevel.SAFE,
        sentimentReliability = SentimentReliability.RELIABLE,
    )
    ReduTheme {
        HistoryScreen(padding = PaddingValues(0.dp), sessions = listOf(session), onClearHistory = {})
    }
}

@Preview(name = "History large font", widthDp = 320, heightDp = 800, fontScale = 1.5f, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun HistoryLargeFontPreview() {
    HistoryPopulatedPreview()
}

@Preview(name = "Setup participant", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun SetupParticipantPreview() {
    ReduTheme {
        SetupScreen(
            padding = PaddingValues(0.dp),
            studyCode = "P01X",
            hasSavedParticipantCode = false,
            accessibilityEnabled = false,
            trackTikTokEnabled = false,
            trackInstagramEnabled = false,
            trackFacebookEnabled = false,
            onStudyCodeChange = {},
            onPlatformTrackingChange = { _, _ -> },
            onSave = {},
            onOpenAccessibilitySettings = {},
            onFinish = {},
            onBack = null,
        )
    }
}

@Preview(name = "Setup ready", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun SetupReadyPreview() {
    ReduTheme {
        SetupScreen(
            padding = PaddingValues(0.dp),
            studyCode = "P01X",
            hasSavedParticipantCode = true,
            accessibilityEnabled = true,
            trackTikTokEnabled = true,
            trackInstagramEnabled = false,
            trackFacebookEnabled = false,
            onStudyCodeChange = {},
            onPlatformTrackingChange = { _, _ -> },
            onSave = {},
            onOpenAccessibilitySettings = {},
            onFinish = {},
            onBack = {},
        )
    }
}

@Preview(name = "Export", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun ExportPreview() {
    ReduTheme {
        ExportScreen(padding = PaddingValues(0.dp), state = ExportUiState.Idle, onExport = {}, onBack = {})
    }
}
