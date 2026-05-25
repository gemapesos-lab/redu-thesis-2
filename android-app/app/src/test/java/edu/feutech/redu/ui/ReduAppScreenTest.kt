package edu.feutech.redu.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ReduAppScreenTest {
    @Test
    fun availableDestinationsBeforeSetupOnlyIncludesSetup() {
        assertEquals(
            listOf(ReduDestination.SETUP),
            availableDestinationsFor(setupComplete = false),
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
            availableDestinationsFor(setupComplete = true),
        )
    }
}
