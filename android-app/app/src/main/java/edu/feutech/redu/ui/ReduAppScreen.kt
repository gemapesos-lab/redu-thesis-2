package edu.feutech.redu.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.feutech.redu.BuildConfig
import edu.feutech.redu.data.AppSettingsEntity
import edu.feutech.redu.data.Platform
import edu.feutech.redu.data.PromptLevel
import edu.feutech.redu.data.ReduDatabase
import edu.feutech.redu.data.RiskPersonalizationEntity
import edu.feutech.redu.data.RiskLevel
import edu.feutech.redu.data.SentimentReliability
import edu.feutech.redu.data.SessionEntity
import edu.feutech.redu.data.StudyGroup
import edu.feutech.redu.export.CsvExporter
import edu.feutech.redu.risk.RiskPersonalization
import edu.feutech.redu.vlm.ModelDownloadManager
import edu.feutech.redu.vlm.ModelState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReduAppScreen(
    database: ReduDatabase,
    isAccessibilityServiceEnabled: () -> Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    context: Context,
) {
    val scope = rememberCoroutineScope()
    val settings by database.settingsDao().observe().collectAsState(initial = null)
    val sessions by database.sessionDao().observeAll().collectAsState(initial = emptyList())
    val personalizationRows by database.riskPersonalizationDao().observeAll().collectAsState(initial = emptyList())
    var studyCode by rememberSaveable { mutableStateOf("") }
    var debugOverlayEnabled by rememberSaveable { mutableStateOf(false) }
    var exportStatus by rememberSaveable { mutableStateOf<String?>(null) }
    var isExporting by rememberSaveable { mutableStateOf(false) }
    val appContext = remember(context) { context.applicationContext }
    val modelDownloadManager = remember(appContext) {
        (appContext as edu.feutech.redu.ReduApp).modelDownloadManager
    }
    val modelState by modelDownloadManager.state.collectAsState()
    var selectedDestination by rememberSaveable { mutableStateOf<ReduDestination?>(null) }
    var accessibilityEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled()) }
    val hasParticipantCode = settings?.studyCode?.isNotBlank() == true && settings?.studyCode != "UNSET"
    val setupComplete = hasParticipantCode && accessibilityEnabled
    val availableDestinations = if (setupComplete) {
        listOf(
            ReduDestination.DASHBOARD,
            ReduDestination.HISTORY,
            ReduDestination.EXPORT,
            ReduDestination.SETTINGS,
        )
    } else {
        listOf(
            ReduDestination.DASHBOARD,
            ReduDestination.SETUP,
            ReduDestination.EXPORT,
            ReduDestination.SETTINGS,
        )
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        accessibilityEnabled = isAccessibilityServiceEnabled()
    }

    LaunchedEffect(settings) {
        settings?.let {
            studyCode = it.studyCode.takeUnless { value -> value == "UNSET" }.orEmpty()
            debugOverlayEnabled = it.debugOverlayEnabled
        }
    }

    LaunchedEffect(setupComplete) {
        selectedDestination = if (setupComplete) {
            when (selectedDestination) {
                null, ReduDestination.SETUP -> ReduDestination.DASHBOARD
                else -> selectedDestination
            }
        } else {
            ReduDestination.SETUP
        }
    }

    val destination = selectedDestination
        ?.takeIf { it in availableDestinations }
        ?: if (setupComplete) ReduDestination.DASHBOARD else ReduDestination.SETUP

    MaterialTheme(colorScheme = lightColorScheme()) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    availableDestinations.forEach { item ->
                        NavigationBarItem(
                            selected = destination == item,
                            onClick = { selectedDestination = item },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        )
                    }
                }
            },
        ) { padding ->
            when (destination) {
                ReduDestination.DASHBOARD -> DashboardScreen(
                    padding = padding,
                    setupComplete = setupComplete,
                    sessions = sessions,
                    onOpenSetup = { selectedDestination = ReduDestination.SETUP },
                )

                ReduDestination.HISTORY -> HistoryScreen(
                    padding = padding,
                    sessions = sessions,
                    onClearHistory = {
                        scope.launch {
                            database.clearHistory()
                        }
                    },
                )

                ReduDestination.SETUP -> SetupScreen(
                    padding = padding,
                    studyCode = studyCode,
                    hasSavedParticipantCode = hasParticipantCode,
                    accessibilityEnabled = accessibilityEnabled,
                    onStudyCodeChange = { studyCode = it.trim() },
                    onSave = {
                        val normalizedCode = studyCode.ifBlank { "UNSET" }
                        val derivedStudyGroup = studyGroupForParticipantCode(normalizedCode)
                        scope.launch {
                            database.settingsDao().save(
                                AppSettingsEntity(
                                    createdAtMillis = settings?.createdAtMillis ?: System.currentTimeMillis(),
                                    studyCode = normalizedCode,
                                    studyGroup = derivedStudyGroup,
                                    promptsEnabled = false,
                                    debugOverlayEnabled = debugOverlayEnabled,
                                    updatedAtMillis = System.currentTimeMillis(),
                                ),
                            )
                            accessibilityEnabled = isAccessibilityServiceEnabled()
                            selectedDestination = if (accessibilityEnabled) {
                                ReduDestination.DASHBOARD
                            } else {
                                ReduDestination.SETUP
                            }
                        }
                    },
                    onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                )

                ReduDestination.EXPORT -> ExportScreen(
                    padding = padding,
                    exportStatus = exportStatus,
                    isExporting = isExporting,
                    onExport = {
                        if (isExporting) return@ExportScreen
                        isExporting = true
                        exportStatus = null
                        scope.launch {
                            try {
                                val zipFile = CsvExporter(context, database).exportAsZip()
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    zipFile,
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/zip"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, "REDU Thesis Export")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share REDU export").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                exportStatus = "Ready to share"
                            } catch (e: Exception) {
                                exportStatus = "Export failed: ${e.message}"
                            } finally {
                                isExporting = false
                            }
                        }
                    },
                )

                ReduDestination.SETTINGS -> SettingsScreen(
                    padding = padding,
                    settings = settings,
                    personalization = personalizationRows.firstOrNull {
                        it.studyCode == (settings?.studyCode ?: "UNSET") && it.studyGroup == settings?.studyGroup
                    },
                    accessibilityEnabled = accessibilityEnabled,
                    debugOverlayEnabled = debugOverlayEnabled,
                    modelState = modelState,
                    onDownloadModel = { modelDownloadManager.startDownload() },
                    onCancelModelDownload = { modelDownloadManager.cancelDownload() },
                    onDeleteModel = { modelDownloadManager.deleteModels() },
                    onStudyCodeSave = { updatedCode ->
                        val normalizedCode = updatedCode.trim().ifBlank { "UNSET" }
                        val derivedStudyGroup = studyGroupForParticipantCode(normalizedCode)
                        studyCode = updatedCode.trim()
                        scope.launch {
                            val current = database.settingsDao().get()
                            database.settingsDao().save(
                                current?.copy(
                                    studyCode = normalizedCode,
                                    studyGroup = derivedStudyGroup,
                                    promptsEnabled = false,
                                    updatedAtMillis = System.currentTimeMillis(),
                                ) ?: AppSettingsEntity(
                                    studyCode = normalizedCode,
                                    studyGroup = derivedStudyGroup,
                                    promptsEnabled = false,
                                    debugOverlayEnabled = debugOverlayEnabled,
                                    updatedAtMillis = System.currentTimeMillis(),
                                ),
                            )
                        }
                    },
                    onPromptsEnabledChange = { enabled ->
                        scope.launch {
                            val current = database.settingsDao().get()
                            val derivedStudyGroup = studyGroupForParticipantCode(studyCode)
                            database.settingsDao().save(
                                current?.let { existing ->
                                    val canEnable = existing.studyGroup == StudyGroup.INTERVENTION && enabled
                                    if (canEnable) {
                                        RiskPersonalization.lockForWeek2(
                                            database = database,
                                            studyCode = existing.studyCode.takeIf { it.isNotBlank() } ?: "UNSET",
                                            studyGroup = existing.studyGroup,
                                        )
                                    }
                                    existing.copy(
                                        promptsEnabled = canEnable,
                                        updatedAtMillis = System.currentTimeMillis(),
                                    )
                                } ?: AppSettingsEntity(
                                    studyCode = studyCode.ifBlank { "UNSET" },
                                    studyGroup = derivedStudyGroup,
                                    promptsEnabled = false,
                                    debugOverlayEnabled = debugOverlayEnabled,
                                    updatedAtMillis = System.currentTimeMillis(),
                                ),
                            )
                        }
                    },
                    onDebugOverlayChange = { enabled ->
                        debugOverlayEnabled = enabled
                        scope.launch {
                            val current = database.settingsDao().get()
                            database.settingsDao().save(
                                current?.copy(
                                    debugOverlayEnabled = enabled,
                                    updatedAtMillis = System.currentTimeMillis(),
                                ) ?: AppSettingsEntity(
                                    studyCode = studyCode.ifBlank { "UNSET" },
                                    studyGroup = studyGroupForParticipantCode(studyCode),
                                    promptsEnabled = false,
                                    debugOverlayEnabled = enabled,
                                    updatedAtMillis = System.currentTimeMillis(),
                                ),
                            )
                        }
                    },
                    onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                    onDemoIntervention = { level ->
                        android.widget.Toast.makeText(
                            context,
                            when (level) {
                                PromptLevel.L1_AWARENESS -> "You've been scrolling for a while. Consider a short pause."
                                PromptLevel.L2_PAUSE -> "Consider taking a short pause."
                                PromptLevel.L3_BREATHING -> "Pause and try a short breathing break."
                                PromptLevel.NONE -> return@SettingsScreen
                            },
                            android.widget.Toast.LENGTH_LONG,
                        ).show()
                    },
                )
            }
        }
    }
}

private enum class ReduDestination(
    val label: String,
    val icon: ImageVector,
) {
    DASHBOARD("Dashboard", Icons.Filled.Assessment),
    SETUP("Setup", Icons.Filled.Tune),
    HISTORY("History", Icons.Filled.History),
    EXPORT("Export", Icons.Filled.Dataset),
    SETTINGS("Settings", Icons.Filled.Settings),
}

@Composable
private fun DashboardScreen(
    padding: PaddingValues,
    setupComplete: Boolean,
    sessions: List<SessionEntity>,
    onOpenSetup: () -> Unit,
) {
    val summary = dashboardSummary(sessions)
    val latest = summary.latestSession
    var diagnosticsExpanded by rememberSaveable { mutableStateOf(false) }

    ScreenColumn(
        padding = padding,
        header = {
            ScreenHeader(
                title = "Dashboard",
            )
        },
    ) {
        if (!setupComplete) {
            CalloutCard(
                title = "Setup required",
                body = "Finish participant setup before REDU starts showing session summaries.",
                actionLabel = "Continue setup",
                onAction = onOpenSetup,
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("Today's sessions", summary.todaySessionCount.toString(), Modifier.weight(1f))
            MetricCard("Today's active time", summary.todayActiveMillis.formatDuration(), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("Activity pattern score", summary.latestRiskScore?.formatOne() ?: "No data", Modifier.weight(1f))
            MetricCard("Current pattern level", latest?.riskLevel?.displayName() ?: "No data", Modifier.weight(1f))
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Latest session", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (latest == null) {
                    SecondaryText("No locally saved sessions yet.")
                } else {
                    InfoRow("Platform", latest.platform.displayName())
                    InfoRow("Started", latest.startedAtMillis.formatTime())
                    InfoRow("Duration", latest.rawDurationMillis.formatDuration())
                    InfoRow("Activity pattern", "${latest.riskLevel.displayName()} (${latest.riskScore.formatOne()})")
                }
            }
        }

        if (BuildConfig.DEBUG) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Research diagnostics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = { diagnosticsExpanded = !diagnosticsExpanded }) {
                            Text(if (diagnosticsExpanded) "Hide" else "Show")
                        }
                    }
                    if (diagnosticsExpanded) {
                        InfoRow("Total sessions", sessions.size.toString())
                        InfoRow("Reliable sessions", sessions.count { it.sentimentReliability == SentimentReliability.RELIABLE }.toString())
                        InfoRow("Latest NSD", latest?.nsdPercent?.formatPercentValue() ?: "No data")
                        InfoRow("Latest OOV", latest?.oovRatio?.formatPercent() ?: "No data")
                        InfoRow("Latest dwell", latest?.meanDwellMillis?.formatDuration() ?: "No data")
                        InfoRow("Latest transitions", latest?.swipeCount?.toString() ?: "No data")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupScreen(
    padding: PaddingValues,
    studyCode: String,
    hasSavedParticipantCode: Boolean,
    accessibilityEnabled: Boolean,
    onStudyCodeChange: (String) -> Unit,
    onSave: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
) {
    val derivedGroup = studyGroupForParticipantCode(studyCode)
    ScreenColumn(
        padding = padding,
        header = {
            ScreenHeader(
                title = "Setup",
            )
        },
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Participant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = studyCode,
                    onValueChange = onStudyCodeChange,
                    label = { Text("Participant study code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                InfoRow("Assigned group", derivedGroup.name)
                Button(onClick = onSave, enabled = studyCode.isNotBlank()) {
                    Text("Save participant code")
                }
                if (hasSavedParticipantCode) {
                    SecondaryText("Participant code is saved. Enable monitoring to complete setup.")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Monitoring service", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    StatusChip(if (accessibilityEnabled) "On" else "Off", positive = accessibilityEnabled)
                }
                SecondaryText("Enable REDU Monitoring Service in Android Accessibility settings before field logging.")
                OutlinedButton(onClick = onOpenAccessibilitySettings) {
                    Text("Open accessibility settings")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryScreen(
    padding: PaddingValues,
    sessions: List<SessionEntity>,
    onClearHistory: () -> Unit,
) {
    var platformFilter by rememberSaveable { mutableStateOf(PlatformFilter.ALL) }
    var riskFilter by rememberSaveable { mutableStateOf(RiskFilter.ALL) }
    var expandedSessionId by rememberSaveable { mutableStateOf<Long?>(null) }
    var clearHistoryDialogOpen by rememberSaveable { mutableStateOf(false) }
    val filtered = filteredSessions(sessions, platformFilter, riskFilter)
    val groups = groupedSessionsByDate(filtered)

    ScreenColumn(
        padding = padding,
        header = {
            ScreenHeader(
                title = "History",
            )
        },
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactFilterMenu(
                    label = "Platform",
                    selectedLabel = platformFilter.displayName(),
                    options = PlatformFilter.entries.toList(),
                    optionLabel = { it.displayName() },
                    onOptionSelected = { platformFilter = it },
                    modifier = Modifier.weight(1f),
                )
                CompactFilterMenu(
                    label = "Pattern",
                    selectedLabel = riskFilter.displayName(),
                    options = RiskFilter.entries.toList(),
                    optionLabel = { it.displayName() },
                    onOptionSelected = { riskFilter = it },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (sessions.isNotEmpty()) {
            OutlinedButton(
                onClick = { clearHistoryDialogOpen = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Clear history")
            }
        }

        if (filtered.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("No sessions found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    SecondaryText("Sessions will appear here after REDU saves local monitoring data.")
                }
            }
        } else {
            groups.forEach { group ->
                Text(
                    group.date.formatDateHeader(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                group.sessions.forEach { session ->
                    SessionHistoryCard(
                        session = session,
                        expanded = expandedSessionId == session.id,
                        onClick = {
                            expandedSessionId = if (expandedSessionId == session.id) null else session.id
                        },
                    )
                }
            }
        }
    }

    if (clearHistoryDialogOpen) {
        AlertDialog(
            onDismissRequest = { clearHistoryDialogOpen = false },
            title = { Text("Clear history?") },
            text = {
                Text("This will permanently delete all saved sessions, prompt events, and reliability logs. Downloaded models and participant settings will be kept.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        clearHistoryDialogOpen = false
                        onClearHistory()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Clear history")
                }
            },
            dismissButton = {
                TextButton(onClick = { clearHistoryDialogOpen = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun <T> CompactFilterMenu(
    label: String,
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
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                "$label: $selectedLabel",
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option), style = MaterialTheme.typography.bodyMedium) },
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
private fun SessionHistoryCard(
    session: SessionEntity,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(session.platform.displayName(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    SecondaryText("${session.startedAtMillis.formatTime()} • ${session.rawDurationMillis.formatDuration()}")
                }
                StatusChip(session.riskLevel.displayName(), positive = session.riskLevel == RiskLevel.SAFE)
            }
            InfoRow("Activity pattern", session.riskScore.formatOne())
            InfoRow("Reliability", session.sentimentReliability.displayName())
            if (expanded) {
                InfoRow("Mean dwell", session.meanDwellMillis.formatDuration())
                InfoRow("Transitions", session.swipeCount.toString())
                InfoRow("NSD", session.nsdPercent?.formatPercentValue() ?: "Unavailable")
                InfoRow("Resolvable units", session.resolvableUnits.toString())
                InfoRow("Negative units", session.negativeUnits.toString())
                InfoRow("OOV", session.oovRatio.formatPercent())
            }
        }
    }
}

@Composable
private fun ExportScreen(
    padding: PaddingValues,
    exportStatus: String?,
    isExporting: Boolean,
    onExport: () -> Unit,
) {
    ScreenColumn(
        padding = padding,
        header = {
            ScreenHeader(
                title = "Export",
            )
        },
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Share thesis data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Button(
                    onClick = onExport,
                    enabled = !isExporting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isExporting) {
                        Text("Preparing export…")
                    } else {
                        Text("Export & Share")
                    }
                }
                if (isExporting) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        strokeCap = StrokeCap.Round,
                    )
                }
                exportStatus?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Included files", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                SecondaryText("• sessions.csv")
                SecondaryText("• daily_summaries.csv")
                SecondaryText("• prompt_events.csv")
                SecondaryText("• reliability_events.csv")
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    padding: PaddingValues,
    settings: AppSettingsEntity?,
    personalization: RiskPersonalizationEntity?,
    accessibilityEnabled: Boolean,
    debugOverlayEnabled: Boolean,
    modelState: ModelState,
    onDownloadModel: () -> Unit,
    onCancelModelDownload: () -> Unit,
    onDeleteModel: () -> Unit,
    onStudyCodeSave: (String) -> Unit,
    onPromptsEnabledChange: (Boolean) -> Unit,
    onDebugOverlayChange: (Boolean) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onDemoIntervention: (PromptLevel) -> Unit = {},
) {
    var editCodeDialogOpen by rememberSaveable { mutableStateOf(false) }
    var editedStudyCode by rememberSaveable { mutableStateOf(settings?.studyCode?.takeIf { it != "UNSET" }.orEmpty()) }
    var debugToolsExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(settings?.studyCode) {
        editedStudyCode = settings?.studyCode?.takeIf { it != "UNSET" }.orEmpty()
    }

    ScreenColumn(
        padding = padding,
        header = {
            ScreenHeader(
                title = "Settings",
            )
        },
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Study mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        SecondaryText("Participant code")
                        Text(
                            settings?.studyCode?.takeIf { it != "UNSET" } ?: "Not set",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    IconButton(onClick = { editCodeDialogOpen = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit participant code")
                    }
                }
                InfoRow("Group", settings?.studyGroup?.name ?: "Not set")
                InfoRow("Prompts", if (settings?.promptsEnabled == true) "Enabled" else "Suppressed")
                if (settings?.studyGroup == StudyGroup.INTERVENTION) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Week 2 intervention prompts", modifier = Modifier.weight(1f))
                        Switch(
                            checked = settings.promptsEnabled,
                            onCheckedChange = onPromptsEnabledChange,
                        )
                    }
                    SecondaryText("Keep off during Week 1 baseline logging; enable only when the intervention phase starts.")
                    SecondaryText(personalizationStatus(settings, personalization))
                } else {
                    SecondaryText("Control mode is logging-only, so prompts remain suppressed.")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Monitoring service", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    StatusChip(if (accessibilityEnabled) "On" else "Off", positive = accessibilityEnabled)
                }
                SecondaryText("REDU uses Android Accessibility only on supported short-form video surfaces and keeps processing on this device.")
                OutlinedButton(onClick = onOpenAccessibilitySettings) {
                    Text("Open Android settings")
                }
            }
        }

        VlmModelCard(
            modelState = modelState,
            onDownload = onDownloadModel,
            onCancelDownload = onCancelModelDownload,
            onDelete = onDeleteModel,
        )

        if (BuildConfig.DEBUG) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Developer tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = { debugToolsExpanded = !debugToolsExpanded }) {
                            Text(if (debugToolsExpanded) "Hide" else "Show")
                        }
                    }
                    if (debugToolsExpanded) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Show extraction metrics", modifier = Modifier.weight(1f))
                            Switch(
                                checked = debugOverlayEnabled,
                                onCheckedChange = onDebugOverlayChange,
                            )
                        }

                        var demoPromptLevel by remember { mutableStateOf<PromptLevel?>(null) }

                        Text("Demo intervention", style = MaterialTheme.typography.labelLarge)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onDemoIntervention(PromptLevel.L1_AWARENESS) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            ) {
                                Text("L1", style = MaterialTheme.typography.labelMedium)
                            }
                            OutlinedButton(
                                onClick = { demoPromptLevel = PromptLevel.L2_PAUSE },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            ) {
                                Text("L2", style = MaterialTheme.typography.labelMedium)
                            }
                            OutlinedButton(
                                onClick = { demoPromptLevel = PromptLevel.L3_BREATHING },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            ) {
                                Text("L3", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        SecondaryText("L1 = awareness toast, L2 = pause overlay, L3 = breathing reset")

                        demoPromptLevel?.let { level ->
                            AlertDialog(
                                onDismissRequest = { demoPromptLevel = null },
                                title = {
                                    Text(
                                        when (level) {
                                            PromptLevel.L2_PAUSE -> "Pause and reset"
                                            PromptLevel.L3_BREATHING -> "Take a short breathing break"
                                            else -> "REDU"
                                        },
                                    )
                                },
                                text = {
                                    Text(
                                        when (level) {
                                            PromptLevel.L2_PAUSE -> "You've been scrolling for an extended period. Consider taking a short pause before continuing."
                                            PromptLevel.L3_BREATHING -> "Breathe in slowly. Hold. Breathe out slowly.\n\nThis is a digital wellness pause, not a clinical exercise."
                                            else -> ""
                                        },
                                    )
                                },
                                confirmButton = {
                                    when (level) {
                                        PromptLevel.L2_PAUSE -> {
                                            TextButton(onClick = { demoPromptLevel = null }) { Text("Continue") }
                                            TextButton(onClick = { demoPromptLevel = null }) { Text("Take break") }
                                            TextButton(onClick = { demoPromptLevel = null }) { Text("Dashboard") }
                                        }
                                        PromptLevel.L3_BREATHING -> {
                                            TextButton(onClick = { demoPromptLevel = null }) { Text("Done") }
                                            TextButton(onClick = { demoPromptLevel = null }) { Text("Skip") }
                                            TextButton(onClick = { demoPromptLevel = null }) { Text("Take break") }
                                        }
                                        else -> {
                                            TextButton(onClick = { demoPromptLevel = null }) { Text("OK") }
                                        }
                                    }
                                },
                            )
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
                OutlinedTextField(
                    value = editedStudyCode,
                    onValueChange = { editedStudyCode = it.trim() },
                    label = { Text("Participant study code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onStudyCodeSave(editedStudyCode)
                        editCodeDialogOpen = false
                    },
                    enabled = editedStudyCode.isNotBlank(),
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editCodeDialogOpen = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

private fun personalizationStatus(
    settings: AppSettingsEntity?,
    personalization: RiskPersonalizationEntity?,
): String {
    if (settings?.studyGroup != StudyGroup.INTERVENTION) return ""
    if (personalization == null) return "Personalization will lock when Week 2 prompts are enabled."
    return if (personalization.hasAnyPersonalizedBounds()) {
        "Personalization locked from ${personalization.reliableBaselineSessionCount} reliable baseline sessions."
    } else {
        "Default priors retained: ${personalization.reliableBaselineSessionCount} reliable baseline sessions."
    }
}

private fun RiskPersonalizationEntity.hasAnyPersonalizedBounds(): Boolean =
    durationQ25Minutes != null || nsdQ25Percent != null

@Composable
private fun StatusChip(label: String, positive: Boolean) {
    val background = if (positive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val content = if (positive) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }
    Surface(
        color = background,
        contentColor = content,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun HistoryFilterCard(
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            content()
        }
    }
}

@Composable
private fun AccessibilityStatusCard(
    accessibilityEnabled: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Monitoring service", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                StatusChip(if (accessibilityEnabled) "On" else "Off", positive = accessibilityEnabled)
            }
            SecondaryText("Open Android settings to enable or verify REDU Monitoring Service.")
            OutlinedButton(onClick = onOpenAccessibilitySettings) {
                Text("Open Android settings")
            }
        }
    }
}

@Composable
private fun ScreenColumn(
    padding: PaddingValues,
    header: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        header()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
private fun ScreenHeader(title: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SecondaryText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CalloutCard(
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            SecondaryText(body)
            Box(Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onAction, modifier = Modifier.align(Alignment.CenterStart)) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
private fun VlmModelCard(
    modelState: ModelState,
    onDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "VLM model",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            SecondaryText("Moondream 2 (0.5B) text model (Q4_K_M)")
            SecondaryText("Moondream 2 (0.5B) multimodal projector (F16)")

            when (modelState) {
                is ModelState.NotDownloaded -> {
                    InfoRow("Status", "Not downloaded")
                    InfoRow("Total size", formatBytes(ModelDownloadManager.TOTAL_SIZE_BYTES))
                    Button(onClick = onDownload, modifier = Modifier.fillMaxWidth()) {
                        Text("Download model")
                    }
                }

                is ModelState.Downloading -> {
                    InfoRow("Status", "Downloading…")
                    LinearProgressIndicator(
                        progress = { modelState.progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        strokeCap = StrokeCap.Round,
                    )
                    Text(
                        "${(modelState.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    modelState.detail?.let { detail ->
                        Text(
                            detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    OutlinedButton(
                        onClick = onCancelDownload,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text("Cancel download")
                    }
                }

                is ModelState.Verifying -> {
                    InfoRow("Status", "Verifying…")
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        strokeCap = StrokeCap.Round,
                    )
                    modelState.detail?.let { detail ->
                        Text(
                            detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                is ModelState.Ready -> {
                    InfoRow("Status", "Ready ✓")
                    InfoRow("Total size", formatBytes(ModelDownloadManager.TOTAL_SIZE_BYTES))
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text("Delete model")
                    }
                }

                is ModelState.Error -> {
                    InfoRow("Status", "Error")
                    Text(
                        modelState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Button(onClick = onDownload, modifier = Modifier.fillMaxWidth()) {
                        Text("Retry download")
                    }
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1024) {
        String.format(Locale.US, "%.1f GB", mb / 1024.0)
    } else {
        String.format(Locale.US, "%.0f MB", mb)
    }
}

internal fun studyGroupForParticipantCode(code: String): StudyGroup =
    when (code.trim().lastOrNull()?.uppercaseChar()) {
        'Y' -> StudyGroup.CONTROL
        else -> StudyGroup.INTERVENTION
    }

private fun Long.formatDuration(): String {
    val totalSeconds = coerceAtLeast(0L) / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(Locale.US, minutes, seconds)
}

private fun Double.formatOne(): String = String.format(Locale.US, "%.1f", this)

private fun Double.formatPercent(): String = String.format(Locale.US, "%.1f%%", this * 100.0)

private fun Double.formatPercentValue(): String = String.format(Locale.US, "%.1f%%", this)

private fun Long.formatTime(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))

private fun LocalDate.formatDateHeader(): String =
    format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US))

private fun Platform.displayName(): String =
    when (this) {
        Platform.TIKTOK -> "TikTok"
        Platform.INSTAGRAM -> "Instagram"
        Platform.FACEBOOK -> "Facebook"
    }

private fun RiskLevel.displayName(): String =
    when (this) {
        RiskLevel.SAFE -> "Normal"
        RiskLevel.WARNING -> "Elevated"
        RiskLevel.CRITICAL -> "Extended"
    }

private fun SentimentReliability.displayName(): String =
    when (this) {
        SentimentReliability.RELIABLE -> "Reliable"
        SentimentReliability.SENTIMENT_UNRELIABLE -> "Limited"
    }

private fun PlatformFilter.displayName(): String =
    when (this) {
        PlatformFilter.ALL -> "All"
        PlatformFilter.TIKTOK -> "TikTok"
        PlatformFilter.INSTAGRAM -> "IG"
        PlatformFilter.FACEBOOK -> "FB"
    }

private fun RiskFilter.displayName(): String =
    when (this) {
        RiskFilter.ALL -> "All"
        RiskFilter.SAFE -> "Normal"
        RiskFilter.WARNING -> "Elevated"
        RiskFilter.CRITICAL -> "Extended"
    }
