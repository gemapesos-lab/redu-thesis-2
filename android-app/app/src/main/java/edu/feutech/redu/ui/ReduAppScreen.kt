package edu.feutech.redu.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import edu.feutech.redu.ReduApp
import edu.feutech.redu.capture.ReduAccessibilityService
import edu.feutech.redu.data.AppSettingsEntity
import edu.feutech.redu.data.Platform
import edu.feutech.redu.data.PromptLevel
import edu.feutech.redu.data.ReduDatabase
import edu.feutech.redu.data.StudyGroup
import edu.feutech.redu.export.CsvExporter
import edu.feutech.redu.risk.RiskPersonalization
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@Composable
fun ReduAppScreen(
    database: ReduDatabase,
    isAccessibilityServiceEnabled: () -> Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    context: Context,
) {
    val scope = rememberCoroutineScope()
    var accessibilityEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        accessibilityEnabled = isAccessibilityServiceEnabled()
    }

    val appDataFlow = remember(database) {
        combine(
            database.settingsDao().observe(),
            database.sessionDao().observeAll(),
            database.riskPersonalizationDao().observeAll(),
        ) { settings, sessions, personalizationRows ->
            AppDataUiState.Ready(settings, sessions, personalizationRows) as AppDataUiState
        }
    }
    val appData by appDataFlow.collectAsState(initial = AppDataUiState.Loading)
    val ready = appData as? AppDataUiState.Ready
    if (ready == null) {
        ReduLoadingScreen()
        return
    }

    val settings = ready.settings
    val sessions = ready.sessions
    val personalizationRows = ready.personalizationRows
    val appContext = remember(context) { context.applicationContext }
    val modelDownloadManager = remember(appContext) {
        (appContext as ReduApp).modelDownloadManager
    }
    val modelState by modelDownloadManager.state.collectAsState()
    var studyCode by rememberSaveable { mutableStateOf("") }
    var debugOverlayEnabled by rememberSaveable { mutableStateOf(false) }
    var exportState by remember { mutableStateOf<ExportUiState>(ExportUiState.Idle) }
    var selectedDestination by rememberSaveable { mutableStateOf<ReduDestination?>(null) }
    var lastPrimaryDestination by rememberSaveable { mutableStateOf(ReduDestination.DASHBOARD) }

    val hasParticipantCode = settings?.studyCode?.isNotBlank() == true && settings.studyCode != "UNSET"
    val trackTikTokEnabled = settings?.trackTikTokEnabled == true
    val trackInstagramEnabled = settings?.trackInstagramEnabled == true
    val trackFacebookEnabled = settings?.trackFacebookEnabled == true
    val anyPlatformEnabled = trackTikTokEnabled || trackInstagramEnabled || trackFacebookEnabled
    val setupComplete = hasParticipantCode && accessibilityEnabled && anyPlatformEnabled
    val participantCodeLocked = isParticipantCodeLocked(hasSessions = sessions.isNotEmpty())
    val showMainShell = canShowMainShell(setupComplete, sessions.isNotEmpty())
    val availableDestinations = availableDestinationsFor(setupComplete, sessions.isNotEmpty())
    var previouslyShowedMainShell by rememberSaveable { mutableStateOf(showMainShell) }

    LaunchedEffect(settings) {
        settings?.let {
            studyCode = it.studyCode.takeUnless { value -> value == "UNSET" }.orEmpty()
            debugOverlayEnabled = it.debugOverlayEnabled
        }
    }

    LaunchedEffect(showMainShell) {
        selectedDestination = when {
            !showMainShell -> ReduDestination.SETUP
            selectedDestination == null -> ReduDestination.DASHBOARD
            !previouslyShowedMainShell && selectedDestination == ReduDestination.SETUP -> ReduDestination.DASHBOARD
            else -> selectedDestination
        }
        previouslyShowedMainShell = showMainShell
    }

    val destination = selectedDestination
        ?.takeIf { it in availableDestinations }
        ?: if (showMainShell) ReduDestination.DASHBOARD else ReduDestination.SETUP

    fun selectPrimary(destination: ReduDestination) {
        if (!destination.primary) return
        lastPrimaryDestination = destination
        selectedDestination = destination
    }

    fun openSecondary(secondary: ReduDestination) {
        if (destination.primary) {
            lastPrimaryDestination = destination
        }
        selectedDestination = secondary
    }

    fun closeSecondary() {
        selectedDestination = lastPrimaryDestination.takeIf { it.primary } ?: ReduDestination.DASHBOARD
    }

    fun savePlatformTracking(platform: Platform, enabled: Boolean) {
        scope.launch {
            val current = database.settingsDao().get() ?: AppSettingsEntity(
                studyCode = studyCode.ifBlank { "UNSET" },
                studyGroup = studyGroupForParticipantCode(studyCode),
                promptsEnabled = false,
                debugOverlayEnabled = debugOverlayEnabled,
            )
            database.settingsDao().save(
                current.withPlatformTracking(
                    platform = platform,
                    enabled = enabled,
                    updatedAtMillis = System.currentTimeMillis(),
                ),
            )
        }
    }

    fun saveParticipantCode(updatedCode: String) {
        if (participantCodeLocked) return
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
            accessibilityEnabled = isAccessibilityServiceEnabled()
        }
    }

    BackHandler(enabled = showMainShell && !destination.primary) {
        closeSecondary()
    }

    val dashboardState = remember(sessions, setupComplete) {
        dashboardUiState(
            sessions = sessions,
            setupComplete = setupComplete,
        )
    }

    AdaptiveNavigationScaffold(
        primaryDestinations = primaryDestinations(),
        selectedDestination = destination,
        showNavigation = showMainShell && destination.primary,
        onDestinationSelected = ::selectPrimary,
    ) { padding ->
        when (destination) {
            ReduDestination.DASHBOARD -> DashboardScreen(
                padding = padding,
                state = dashboardState,
                onOpenSetup = { openSecondary(ReduDestination.SETUP) },
            )

            ReduDestination.HISTORY -> HistoryScreen(
                padding = padding,
                sessions = sessions,
                onClearHistory = {
                    scope.launch {
                        context.sendClearActiveCaptureStateBroadcast()
                        database.clearHistory()
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
                hasExistingSessions = sessions.isNotEmpty(),
                onDownloadModel = modelDownloadManager::startDownload,
                onCancelModelDownload = modelDownloadManager::cancelDownload,
                onDeleteModel = modelDownloadManager::deleteModels,
                onPlatformTrackingChange = ::savePlatformTracking,
                onStudyCodeSave = ::saveParticipantCode,
                onStudyPeriodSave = { week1StartMillis, week1EndMillis, week2StartMillis, week2EndMillis ->
                    scope.launch {
                        val current = database.settingsDao().get()
                        database.settingsDao().save(
                            current?.copy(
                                week1StartMillis = week1StartMillis,
                                week1EndMillis = week1EndMillis,
                                week2StartMillis = week2StartMillis,
                                week2EndMillis = week2EndMillis,
                                updatedAtMillis = System.currentTimeMillis(),
                            ) ?: AppSettingsEntity(
                                studyCode = studyCode.ifBlank { "UNSET" },
                                studyGroup = studyGroupForParticipantCode(studyCode),
                                promptsEnabled = false,
                                debugOverlayEnabled = debugOverlayEnabled,
                                week1StartMillis = week1StartMillis,
                                week1EndMillis = week1EndMillis,
                                week2StartMillis = week2StartMillis,
                                week2EndMillis = week2EndMillis,
                                updatedAtMillis = System.currentTimeMillis(),
                            ),
                        )
                    }
                },
                onResetStudyData = {
                    scope.launch {
                        context.sendClearActiveCaptureStateBroadcast()
                        database.resetStudyData()
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
                onOpenExport = { openSecondary(ReduDestination.EXPORT) },
                onDemoIntervention = { level ->
                    if (level != PromptLevel.NONE) {
                        context.sendBroadcast(
                            Intent(ReduAccessibilityService.ACTION_DEMO_PROMPT).apply {
                                setPackage(context.packageName)
                                putExtra(ReduAccessibilityService.EXTRA_PROMPT_LEVEL, level.name)
                            },
                        )
                    }
                },
            )

            ReduDestination.SETUP -> SetupScreen(
                padding = padding,
                studyCode = studyCode,
                hasSavedParticipantCode = hasParticipantCode,
                participantCodeLocked = participantCodeLocked,
                accessibilityEnabled = accessibilityEnabled,
                trackTikTokEnabled = trackTikTokEnabled,
                trackInstagramEnabled = trackInstagramEnabled,
                trackFacebookEnabled = trackFacebookEnabled,
                onStudyCodeChange = { studyCode = it.trim() },
                onPlatformTrackingChange = ::savePlatformTracking,
                onSave = { saveParticipantCode(studyCode) },
                onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                onFinish = { selectPrimary(ReduDestination.DASHBOARD) },
                onBack = if (showMainShell) ({ closeSecondary() }) else null,
            )

            ReduDestination.EXPORT -> ExportScreen(
                padding = padding,
                state = exportState,
                onExport = {
                    if (exportState is ExportUiState.Preparing) return@ExportScreen
                    exportState = ExportUiState.Preparing
                    scope.launch {
                        try {
                            val zipFile = CsvExporter(context, database).exportAsZip()
                            val uri = FileProvider.getUriForFile(
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
                            context.startActivity(
                                Intent.createChooser(shareIntent, "Share REDU export")
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                            exportState = ExportUiState.Ready(zipFile.name)
                        } catch (_: Exception) {
                            exportState = ExportUiState.Error(
                                "We couldn't create the export. Check available storage and try again.",
                            )
                        }
                    }
                },
                onBack = ::closeSecondary,
            )
        }
    }
}

internal enum class ReduDestination(
    val label: String,
    val icon: ImageVector,
    val primary: Boolean,
) {
    DASHBOARD("Today", Icons.Outlined.Home, true),
    HISTORY("History", Icons.Outlined.History, true),
    SETTINGS("Settings", Icons.Outlined.Settings, true),
    SETUP("Setup", Icons.Outlined.Tune, false),
    EXPORT("Export", Icons.Outlined.FileUpload, false),
}

internal fun primaryDestinations(): List<ReduDestination> = listOf(
    ReduDestination.DASHBOARD,
    ReduDestination.HISTORY,
    ReduDestination.SETTINGS,
)

internal fun isParticipantCodeLocked(
    hasSessions: Boolean,
): Boolean = hasSessions

internal fun availableDestinationsFor(
    setupComplete: Boolean,
    hasSessions: Boolean,
): List<ReduDestination> = if (canShowMainShell(setupComplete, hasSessions)) {
    primaryDestinations() + listOf(ReduDestination.SETUP, ReduDestination.EXPORT)
} else {
    listOf(ReduDestination.SETUP)
}

private fun AppSettingsEntity.withPlatformTracking(
    platform: Platform,
    enabled: Boolean,
    updatedAtMillis: Long,
): AppSettingsEntity = when (platform) {
    Platform.TIKTOK -> copy(trackTikTokEnabled = enabled, updatedAtMillis = updatedAtMillis)
    Platform.INSTAGRAM -> copy(trackInstagramEnabled = enabled, updatedAtMillis = updatedAtMillis)
    Platform.FACEBOOK -> copy(trackFacebookEnabled = enabled, updatedAtMillis = updatedAtMillis)
}

private fun Context.sendClearActiveCaptureStateBroadcast() {
    sendBroadcast(
        Intent(ReduAccessibilityService.ACTION_CLEAR_ACTIVE_CAPTURE_STATE).apply {
            setPackage(packageName)
        },
    )
}
