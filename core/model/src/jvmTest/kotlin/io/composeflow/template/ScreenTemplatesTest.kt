package io.composeflow.template

import io.composeflow.model.project.appscreen.screen.replaceIdsToIncreaseUniqueness
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlin.test.Test

class ScreenTemplatesTest {
    @Test
    fun restoreMessages_verifyNoCrash() {
        val messagesScreen = ScreenTemplates.messagesScreen
        assertEquals("Messages", messagesScreen.screen.name)
    }

    @Test
    fun restoreLogin_verifyNoCrash() {
        val loginScreen = ScreenTemplates.loginScreen
        assertEquals("Login", loginScreen.screen.name)
    }

    @Test
    fun restoreSettings_verifyNoCrash() {
        val settingsScreen = ScreenTemplates.settingsScreen
        assertEquals("Settings", settingsScreen.screen.name)
    }

    @Test
    fun prefixYaml_verifyIdsArePrefixed() {
        val messageScreen = ScreenTemplates.messagesScreen
        val replaced = messageScreen.screen.replaceIdsToIncreaseUniqueness()
        assertTrue(replaced.id.contains(":"))
    }
}
