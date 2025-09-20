package io.composeflow.ui.uibuilder

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.spyk
import sun.awt.HeadlessToolkit
import java.awt.HeadlessException
import java.awt.Toolkit

/**
 * When obtaining String Resources for Compose Multiplatform on desktop, Compose gets the screen
 * resolution from AWT Toolkit to determine the Resource's Configuration.
 * https://github.com/JetBrains/compose-multiplatform/blob/b0eb0916c24729d7eb08cd09f3a1bd275a9a7402/components/resources/library/src/desktopMain/kotlin/org/jetbrains/compose/resources/ResourceEnvironment.desktop.kt#L12
 *
 * In a headless environment like CI, the Default Toolkit is [HeadlessToolkit],
 * and [HeadlessToolkit.getScreenResolution] throws a [HeadlessException].
 * https://github.com/openjdk/jdk/blob/b03b6f54c5f538146c3088c4dc2cea70ba70d07a/src/java.desktop/share/classes/sun/awt/HeadlessToolkit.java#L119-L123
 *
 * To avoid this exception, mock [HeadlessToolkit.getScreenResolution] and return the mocked Toolkit
 * from [Toolkit.getDefaultToolkit].
 *
 * To use this mocking, add following JVM arguments to work around JDK's strong encapsulation of
 * standard modules.
 * ```
 * --add-opens java.desktop/java.awt=ALL-UNNAMED
 * --add-opens java.desktop/sun.awt=ALL-UNNAMED
 * ```
 *
 * In Gradle, you can add these arguments to the test task as follows:
 * ```kotlin
 * tasks.withType<Test>().configureEach {
 *     jvmArgs(
 *         "--add-opens", "java.desktop/java.awt=ALL-UNNAMED",
 *         "--add-opens", "java.desktop/sun.awt=ALL-UNNAMED",
 *     )
 * }
 * ```
 *
 * @param screenResolution The desired screen resolution to be returned by the mocked Toolkit. Default is 96 DPI.
 */
fun mockAwtToolkitForComposeResources(screenResolution: Int = 96) {
    val toolkit = spyk(Toolkit.getDefaultToolkit())
    every { toolkit.screenResolution } returns screenResolution
    mockkStatic(Toolkit::class)
    every { Toolkit.getDefaultToolkit() } returns toolkit
}
