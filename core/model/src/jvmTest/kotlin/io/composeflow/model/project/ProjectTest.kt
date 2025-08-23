package io.composeflow.model.project

import io.composeflow.cloud.storage.BlobIdWrapper
import io.composeflow.cloud.storage.BlobInfoWrapper
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.model.project.component.Component
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(kotlin.time.ExperimentalTime::class)
class ProjectTest {
    @Test
    fun testCopyProjectContents() {
        // Create source project with test data
        val source =
            Project(
                id = "source-id",
                name = "source-project",
                packageName = "com.source.test",
            )

        // Clear default screens and add our test screen
        source.screenHolder.screens.clear()
        val sourceScreen = Screen(id = "screen-1", name = "TestScreen")
        source.screenHolder.screens.add(sourceScreen)

        // Add some test components
        val sourceComponent = Component(id = "comp-1", name = "TestComponent")
        source.componentHolder.components.add(sourceComponent)

        // Create target project
        val target =
            Project(
                id = "target-id",
                name = "target-project",
                packageName = "com.target.test",
            )

        // Add initial data to target that should be cleared
        val targetScreen = Screen(id = "screen-2", name = "TargetScreen")
        target.screenHolder.screens.add(targetScreen)

        // Copy contents
        target.copyProjectContents(source)

        // Verify screens were copied
        assertEquals(1, target.screenHolder.screens.size)
        assertEquals("TestScreen", target.screenHolder.screens[0].name)
        assertEquals("screen-1", target.screenHolder.screens[0].id)

        // Verify components were copied
        assertEquals(1, target.componentHolder.components.size)
        assertEquals("TestComponent", target.componentHolder.components[0].name)
        assertEquals("comp-1", target.componentHolder.components[0].id)

        // Verify other lists were cleared (even if empty)
        assertTrue(target.apiHolder.apiDefinitions.isEmpty())
        assertTrue(target.dataTypeHolder.dataTypes.isEmpty())
        assertTrue(target.customEnumHolder.enumList.isEmpty())
        assertTrue(target.assetHolder.images.isEmpty())
        assertTrue(target.assetHolder.icons.isEmpty())
    }

    @Test
    fun testCopyProjectContentsWithComplexData() {
        val source = Project(name = "complex-source")
        val target = Project(name = "complex-target")

        // Clear default screens and add our test screens
        source.screenHolder.screens.clear()
        source.screenHolder.screens.addAll(
            listOf(
                Screen(id = "s1", name = "Screen1"),
                Screen(id = "s2", name = "Screen2"),
            ),
        )

        source.componentHolder.components.addAll(
            listOf(
                Component(id = "c1", name = "Component1"),
                Component(id = "c2", name = "Component2"),
            ),
        )

        // Clear default data and add data to target that should be replaced
        target.screenHolder.screens.clear()
        target.componentHolder.components.clear()
        target.screenHolder.screens.add(Screen(id = "old", name = "OldScreen"))
        target.componentHolder.components.add(Component(id = "old", name = "OldComponent"))

        target.copyProjectContents(source)

        // Verify exact copy
        assertEquals(2, target.screenHolder.screens.size)
        assertEquals("Screen1", target.screenHolder.screens[0].name)
        assertEquals("Screen2", target.screenHolder.screens[1].name)
        assertEquals(2, target.componentHolder.components.size)
        assertEquals("Component1", target.componentHolder.components[0].name)
        assertEquals("Component2", target.componentHolder.components[1].name)
    }

    @Test
    fun testGenerateWriteFileInstructionsIncludesAndroidManifestWithoutSplashScreen() {
        val project = Project(name = "test-project")

        // No splash screen enabled
        val writeInstructions = project.generateWriteFileInstructions()

        // Should still include AndroidManifest.xml
        assertTrue(writeInstructions.containsKey("composeApp/src/androidMain/AndroidManifest.xml"))

        // Should not include splash screen files
        assertTrue(!writeInstructions.containsKey("composeApp/src/androidMain/res/values/splash_theme.xml"))
        assertTrue(!writeInstructions.containsKey("composeApp/src/androidMain/res/drawable/ic_splash_image.xml"))

        // Verify AndroidManifest content
        val manifestContent =
            String(
                writeInstructions["composeApp/src/androidMain/AndroidManifest.xml"]!!,
                Charsets.UTF_8,
            )
        assertTrue(manifestContent.contains("<manifest"))
    }

    @Test
    fun testGenerateWriteFileInstructionsIncludesAppAssetXml() {
        val project = Project(name = "test-project")

        // Enable splash screen
        val testBlobInfo =
            BlobInfoWrapper(
                blobId =
                    BlobIdWrapper(bucket = "test", name = "splash", generation = null),
                fileName = "splash.png",
                folderName = "test",
                mediaLink = null,
                size = 0L,
            )
        project.appAssetHolder.splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value =
            testBlobInfo

        val writeInstructions = project.generateWriteFileInstructions()

        // Should include AndroidManifest and splash screen XML files
        assertTrue(writeInstructions.containsKey("composeApp/src/androidMain/AndroidManifest.xml"))
        assertTrue(writeInstructions.containsKey("composeApp/src/androidMain/res/values/splash_theme.xml"))
        assertTrue(writeInstructions.containsKey("composeApp/src/androidMain/res/drawable/ic_splash_image.xml"))

        // Verify content is properly converted to ByteArray
        val splashThemeContent =
            String(
                writeInstructions["composeApp/src/androidMain/res/values/splash_theme.xml"]!!,
                Charsets.UTF_8,
            )
        assertTrue(splashThemeContent.contains("Theme.App.Splash"))

        // Verify AndroidManifest content
        val manifestContent =
            String(
                writeInstructions["composeApp/src/androidMain/AndroidManifest.xml"]!!,
                Charsets.UTF_8,
            )
        assertTrue(manifestContent.contains("<manifest"))
    }
}
