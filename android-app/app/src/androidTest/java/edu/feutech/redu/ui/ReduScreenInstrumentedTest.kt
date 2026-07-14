package edu.feutech.redu.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import edu.feutech.redu.data.AppSettingsEntity
import edu.feutech.redu.data.RiskLevel
import edu.feutech.redu.data.StudyGroup
import edu.feutech.redu.ui.theme.ReduTheme
import edu.feutech.redu.vlm.ModelState
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class ReduScreenInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dashboardShowsRepairActionWhenMonitoringPermissionIsOff() {
        composeRule.setContent {
            ReduTheme {
                DashboardScreen(
                    padding = PaddingValues(),
                    state = dashboardUiState(
                        sessions = emptyList(),
                        setupComplete = false,
                    ),
                    onOpenSetup = {},
                )
            }
        }

        composeRule.onNodeWithText("Monitoring needs attention").assertIsDisplayed()
        composeRule.onNodeWithText("Review setup").assertIsDisplayed()
        composeRule.onNodeWithText("Your last 7 days").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("No activity in the last 7 days").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithContentDescription("How activity patterns are calculated").assertDoesNotExist()
    }

    @Test
    fun dashboardKeepsContextualHelpAndMakesWeeklyDaysSelectable() {
        val monday = LocalDate.of(2026, 7, 13)
        val activity = (6L downTo 0L).map { daysAgo ->
            val date = monday.minusDays(daysAgo)
            DailyActivityPoint(
                date = date,
                activeMillis = when (date) {
                    monday.minusDays(1) -> 120_000L
                    monday -> 42_000L
                    else -> 0L
                },
                sessionCount = when (date) {
                    monday.minusDays(1) -> 1
                    monday -> 2
                    else -> 0
                },
            )
        }
        composeRule.setContent {
            ReduTheme {
                DashboardScreen(
                    padding = PaddingValues(),
                    state = DashboardUiState(
                        date = monday,
                        setupComplete = true,
                        summary = DashboardSummary(
                            todaySessionCount = 2,
                            todayActiveMillis = 42_000L,
                            latestRiskScore = 16.7,
                            peakRiskLevel = RiskLevel.SAFE,
                            latestSession = null,
                        ),
                        weeklyActivity = activity,
                        totalSessionCount = 3,
                        reliableSessionCount = 3,
                    ),
                    onOpenSetup = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("How activity patterns are calculated").assertDoesNotExist()
        composeRule.onNodeWithText("42 sec").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Sunday, July 12: 2 min, 1 session")
            .performScrollTo()
            .performClick()
            .assertIsSelected()
        composeRule.onNodeWithText("Sunday, July 12").assertIsDisplayed()
        composeRule.onNodeWithText("2 min · 1 session").assertIsDisplayed()
        composeRule.onNodeWithText("What this means").performScrollTo().performClick()
        composeRule.onNodeWithText("Activity patterns").assertIsDisplayed()
    }

    @Test
    fun firstSetupStepDoesNotRevealGroupBeforeCodeEntry() {
        composeRule.setContent {
            ReduTheme {
                SetupScreen(
                    padding = PaddingValues(),
                    studyCode = "",
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

        composeRule.onNodeWithText("Participant study code").assertIsDisplayed()
        composeRule.onNodeWithText("Assigned group").assertDoesNotExist()
        composeRule.onNodeWithText("Save participant code").assertIsNotEnabled()
    }

    @Test
    fun exportExplainsPrivacyBoundaryAndErrorInline() {
        composeRule.setContent {
            ReduTheme {
                ExportScreen(
                    padding = PaddingValues(),
                    state = ExportUiState.Error("We couldn't create the export."),
                    onExport = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Aggregate data only").assertIsDisplayed()
        composeRule.onNodeWithText("We couldn't create the export.").assertIsDisplayed()
        composeRule.onNodeWithText("Session history").assertIsDisplayed()
        composeRule.onNodeWithText("risk_personalization.csv").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun exportPreparingDisablesTheShareAction() {
        composeRule.setContent {
            ReduTheme {
                ExportScreen(
                    padding = PaddingValues(),
                    state = ExportUiState.Preparing,
                    onExport = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Preparing export").assertIsNotEnabled()
        composeRule.onNodeWithText("Packaging the saved datasets. Keep REDU open for a moment.").assertIsDisplayed()
    }

    @Test
    fun exportReadyShowsTheGeneratedZipName() {
        composeRule.setContent {
            ReduTheme {
                ExportScreen(
                    padding = PaddingValues(),
                    state = ExportUiState.Ready("redu-export-P01X-20260713.zip"),
                    onExport = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Export ready").assertIsDisplayed()
        composeRule.onNodeWithText("Created redu-export-P01X-20260713.zip").assertIsDisplayed()
    }

    @Test
    fun settingsKeepsResearchControlsSecondary() {
        composeRule.setContent {
            ReduTheme {
                SettingsScreen(
                    padding = PaddingValues(),
                    settings = AppSettingsEntity(
                        studyCode = "P01X",
                        studyGroup = StudyGroup.INTERVENTION,
                        trackTikTokEnabled = true,
                    ),
                    personalization = null,
                    accessibilityEnabled = true,
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

        composeRule.onNodeWithText("Monitoring").assertIsDisplayed()
        composeRule.onNodeWithText("Pause prompts").assertIsDisplayed()
        composeRule.onNode(hasScrollAction()).performScrollToIndex(6)
        composeRule.onNodeWithText("Study configuration").assertIsDisplayed()
        composeRule.onNode(hasScrollAction()).performScrollToIndex(7)
        composeRule.onNodeWithText("Image scanning").assertIsDisplayed()
    }
}
