package io.composeflow.testing

actual fun isTest(): Boolean =
    try {
        Class.forName("androidx.compose.ui.test.junit4.ComposeTestRule")
        true
    } catch (e: ClassNotFoundException) {
        false
    }