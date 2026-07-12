package edu.feutech.redu.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import edu.feutech.redu.data.AppSettingsEntity
import edu.feutech.redu.data.StudyGroup
import edu.feutech.redu.ui.theme.ReduTheme
import edu.feutech.redu.vlm.ModelState
import org.junit.Rule
import org.junit.Test

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
                        accessibilityEnabled = false,
                        trackTikTokEnabled = true,
                        trackInstagramEnabled = false,
                        trackFacebookEnabled = false,
                    ),
                    onOpenSetup = {},
                    onOpenHistory = {},
                )
            }
        }

        composeRule.onNodeWithText("Monitoring needs attention").assertIsDisplayed()
        composeRule.onNodeWithText("Review setup").assertIsDisplayed()
        composeRule.onNodeWithText("No saved sessions yet").performScrollTo().assertIsDisplayed()
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
                    onOpenSetup = {},
                    onOpenExport = {},
                )
            }
        }

        composeRule.onNodeWithText("Monitoring").assertIsDisplayed()
        composeRule.onNodeWithText("Pause prompts").assertIsDisplayed()
        composeRule.onNode(hasScrollAction()).performScrollToIndex(6)
        composeRule.onNodeWithText("Study configuration").assertIsDisplayed()
        composeRule.onNode(hasScrollAction()).performScrollToIndex(7)
        composeRule.onNodeWithText("Visual fallback").assertIsDisplayed()
    }
}
