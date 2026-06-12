package edu.feutech.redu.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ReduAppScreenTest {
    @Test
    fun availableDestinationsBeforeSetupOnlyIncludesSetup() {
        assertEquals(
            listOf(ReduDestination.SETUP),
            availableDestinationsFor(setupComplete = false, hasSessions = false),
        )
    }

    @Test
    fun availableDestinationsBeforeSetupKeepsExportReachableWhenSessionsExist() {
        assertEquals(
            listOf(
                ReduDestination.SETUP,
                ReduDestination.HISTORY,
                ReduDestination.EXPORT,
                ReduDestination.SETTINGS,
            ),
            availableDestinationsFor(setupComplete = false, hasSessions = true),
        )
    }

    @Test
    fun availableDestinationsAfterSetupIncludesMainScreens() {
        assertEquals(
            listOf(
                ReduDestination.DASHBOARD,
                ReduDestination.HISTORY,
                ReduDestination.EXPORT,
                ReduDestination.SETTINGS,
            ),
            availableDestinationsFor(setupComplete = true, hasSessions = false),
        )
    }

    @Test
    fun exportIncludedFilesIncludesRiskPersonalization() {
        assertEquals(
            listOf(
                "sessions.csv",
                "daily_summaries.csv",
                "study_periods.csv",
                "prompt_events.csv",
                "reliability_events.csv",
                "risk_personalization.csv",
            ),
            exportIncludedFiles(),
        )
    }
}
