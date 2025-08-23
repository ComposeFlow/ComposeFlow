package io.composeflow.model.project.appassets

import androidx.compose.ui.graphics.Color
import io.composeflow.cloud.storage.BlobIdWrapper
import io.composeflow.cloud.storage.BlobInfoWrapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SplashScreenInfoHolderTest {
    private fun createTestBlobInfoWrapper(
        id: String,
        fileName: String,
    ): BlobInfoWrapper =
        BlobInfoWrapper(
            blobId = BlobIdWrapper(bucket = "test-bucket", name = id, generation = null),
            fileName = fileName,
            folderName = "test",
            mediaLink = null,
            size = 0L,
        )

    @Test
    fun testCopyContents() {
        val source = SplashScreenInfoHolder()
        val target = SplashScreenInfoHolder()

        // Modify source with test data
        source.androidSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("android-image", "android_splash.png")
        source.androidSplashScreenBackgroundColor.value = Color.Blue
        source.iOSSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("ios-image", "ios_splash.png")
        source.iOSSplashScreenBackgroundColor.value = Color.Green

        // Copy contents
        target.copyContents(source)

        // Verify android splash screen image was copied
        assertEquals(
            "android-image",
            target.androidSplashScreenImageBlobInfo.value
                ?.blobId
                ?.name,
        )
        assertEquals("android_splash.png", target.androidSplashScreenImageBlobInfo.value?.fileName)

        // Verify android splash screen background color was copied
        assertEquals(Color.Blue, target.androidSplashScreenBackgroundColor.value)

        // Verify iOS splash screen image was copied
        assertEquals(
            "ios-image",
            target.iOSSplashScreenImageBlobInfo.value
                ?.blobId
                ?.name,
        )
        assertEquals("ios_splash.png", target.iOSSplashScreenImageBlobInfo.value?.fileName)

        // Verify iOS splash screen background color was copied
        assertEquals(Color.Green, target.iOSSplashScreenBackgroundColor.value)
    }

    @Test
    fun testCopyContentsOverwritesExistingData() {
        val source = SplashScreenInfoHolder()
        val target = SplashScreenInfoHolder()

        // Set initial data in both source and target
        source.androidSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("source-image", "source.png")
        source.androidSplashScreenBackgroundColor.value = Color.Yellow
        source.iOSSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("source-ios", "source_ios.png")
        source.iOSSplashScreenBackgroundColor.value = Color.Magenta

        target.androidSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("target-image", "target.png")
        target.androidSplashScreenBackgroundColor.value = Color.Black
        target.iOSSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("target-ios", "target_ios.png")
        target.iOSSplashScreenBackgroundColor.value = Color.Gray

        // Copy should overwrite target's data with source's data
        target.copyContents(source)

        // Verify source data overwrote target data
        assertEquals(
            "source-image",
            target.androidSplashScreenImageBlobInfo.value
                ?.blobId
                ?.name,
        )
        assertEquals("source.png", target.androidSplashScreenImageBlobInfo.value?.fileName)
        assertEquals(Color.Yellow, target.androidSplashScreenBackgroundColor.value)
        assertEquals(
            "source-ios",
            target.iOSSplashScreenImageBlobInfo.value
                ?.blobId
                ?.name,
        )
        assertEquals("source_ios.png", target.iOSSplashScreenImageBlobInfo.value?.fileName)
        assertEquals(Color.Magenta, target.iOSSplashScreenBackgroundColor.value)
    }

    // Helper Tests
    @Test
    fun testIsIOSSplashScreenEnabled() {
        val splashScreenInfoHolder = SplashScreenInfoHolder()

        // Initially disabled
        assertFalse(splashScreenInfoHolder.isIOSSplashScreenEnabled())

        // Set iOS splash screen image
        splashScreenInfoHolder.iOSSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("ios-splash", "splash.png")

        // Now enabled
        assertTrue(splashScreenInfoHolder.isIOSSplashScreenEnabled())
    }

    // XML Generation Tests
    @Test
    fun testGenerateXmlFilesWhenDisabled() {
        val splashScreenInfoHolder = SplashScreenInfoHolder()

        // No image set, so splash screen is disabled
        val xmlFiles = splashScreenInfoHolder.generateXmlFiles()

        assertTrue(xmlFiles.isEmpty())
    }

    @Test
    fun testGenerateXmlFilesWhenEnabled() {
        val splashScreenInfoHolder = SplashScreenInfoHolder()

        // Set splash screen image to enable it
        splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("splash-image", "splash.png")
        splashScreenInfoHolder.androidSplashScreenBackgroundColor.value = Color.Blue

        val xmlFiles = splashScreenInfoHolder.generateXmlFiles()

        assertEquals(2, xmlFiles.size)

        // Check splash theme file
        assertTrue(xmlFiles.containsKey("composeApp/src/androidMain/res/values/splash_theme.xml"))
        val splashThemeXml = xmlFiles["composeApp/src/androidMain/res/values/splash_theme.xml"]!!
        assertTrue(splashThemeXml.contains("Theme.App.Splash"))
        assertTrue(splashThemeXml.contains("Theme.SplashScreen"))
        assertTrue(splashThemeXml.contains(ANDROID_IC_SPLASH_IMAGE))

        // Check splash image drawable file
        assertTrue(xmlFiles.containsKey("composeApp/src/androidMain/res/drawable/$ANDROID_IC_SPLASH_IMAGE.xml"))
        val drawableXml =
            xmlFiles["composeApp/src/androidMain/res/drawable/$ANDROID_IC_SPLASH_IMAGE.xml"]!!
        assertTrue(drawableXml.contains("<?xml version=\"1.0\" encoding=\"utf-8\"?>"))
        assertTrue(drawableXml.contains("<layer-list"))
        assertTrue(drawableXml.contains("@drawable/${ANDROID_IC_SPLASH_IMAGE}_actual"))
        assertTrue(drawableXml.contains("android:gravity=\"center\""))
    }

    @Test
    fun testGenerateXmlFilesWithoutBackgroundColor() {
        val splashScreenInfoHolder = SplashScreenInfoHolder()

        // Set splash screen image but no background color
        splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("splash-image", "splash.png")
        // androidSplashScreenBackgroundColor remains null

        val xmlFiles = splashScreenInfoHolder.generateXmlFiles()

        assertEquals(2, xmlFiles.size)

        // Splash theme should still be generated even without background color
        val splashThemeXml = xmlFiles["composeApp/src/androidMain/res/values/splash_theme.xml"]!!
        assertTrue(splashThemeXml.contains("Theme.App.Splash"))
    }

    @Test
    fun testGenerateXmlFilesWithIOSSplashScreen() {
        val splashScreenInfoHolder = SplashScreenInfoHolder()

        // Set iOS splash screen image
        splashScreenInfoHolder.iOSSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("ios-splash", "ios_splash.png")

        val xmlFiles = splashScreenInfoHolder.generateXmlFiles()

        assertEquals(1, xmlFiles.size)

        // Check iOS Contents.json file
        assertTrue(xmlFiles.containsKey("iosApp/iosApp/Assets.xcassets/SplashImage.imageset/Contents.json"))
        val contentsJson =
            xmlFiles["iosApp/iosApp/Assets.xcassets/SplashImage.imageset/Contents.json"]!!
        assertTrue(contentsJson.contains("ios_splash.png"))
        assertTrue(contentsJson.contains("universal"))
        assertTrue(contentsJson.contains("filename"))
    }

    @Test
    fun testGenerateXmlFilesWithBothPlatforms() {
        val splashScreenInfoHolder = SplashScreenInfoHolder()

        // Set both Android and iOS splash screen images
        splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("android-splash", "android.png")
        splashScreenInfoHolder.iOSSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("ios-splash", "ios.jpg")

        val xmlFiles = splashScreenInfoHolder.generateXmlFiles()

        assertEquals(3, xmlFiles.size)

        // Check Android files
        assertTrue(xmlFiles.containsKey("composeApp/src/androidMain/res/values/splash_theme.xml"))
        assertTrue(xmlFiles.containsKey("composeApp/src/androidMain/res/drawable/$ANDROID_IC_SPLASH_IMAGE.xml"))

        // Check iOS file
        assertTrue(xmlFiles.containsKey("iosApp/iosApp/Assets.xcassets/SplashImage.imageset/Contents.json"))
        val contentsJson =
            xmlFiles["iosApp/iosApp/Assets.xcassets/SplashImage.imageset/Contents.json"]!!
        assertTrue(contentsJson.contains("ios.jpg"))
    }
}
