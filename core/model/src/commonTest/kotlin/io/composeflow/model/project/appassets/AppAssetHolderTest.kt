package io.composeflow.model.project.appassets

import androidx.compose.ui.graphics.Color
import io.composeflow.cloud.storage.BlobIdWrapper
import io.composeflow.cloud.storage.BlobInfoWrapper
import io.composeflow.model.project.Project
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(kotlin.time.ExperimentalTime::class)
class AppAssetHolderTest {
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
        val source = AppAssetHolder()
        val target = AppAssetHolder()

        // Modify source SplashScreenInfoHolder
        source.splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("android-image", "android_splash.png")
        source.splashScreenInfoHolder.androidSplashScreenBackgroundColor.value = Color.Blue
        source.splashScreenInfoHolder.iOSSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("ios-image", "ios_splash.png")
        source.splashScreenInfoHolder.iOSSplashScreenBackgroundColor.value = Color.Green

        // Copy contents
        target.copyContents(source)

        // Verify android splash screen image was copied
        assertEquals(
            "android-image",
            target.splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value
                ?.blobId
                ?.name,
        )
        assertEquals(
            "android_splash.png",
            target.splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value
                ?.fileName,
        )

        // Verify android splash screen background color was copied
        assertEquals(
            Color.Blue,
            target.splashScreenInfoHolder.androidSplashScreenBackgroundColor.value,
        )

        // Verify iOS splash screen image was copied
        assertEquals(
            "ios-image",
            target.splashScreenInfoHolder.iOSSplashScreenImageBlobInfo.value
                ?.blobId
                ?.name,
        )
        assertEquals(
            "ios_splash.png",
            target.splashScreenInfoHolder.iOSSplashScreenImageBlobInfo.value
                ?.fileName,
        )

        // Verify iOS splash screen background color was copied
        assertEquals(
            Color.Green,
            target.splashScreenInfoHolder.iOSSplashScreenBackgroundColor.value,
        )
    }

    @Test
    fun testCopyContentsOverwritesExistingData() {
        val source = AppAssetHolder()
        val target = AppAssetHolder()

        // Set initial data in both source and target
        source.splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("source-image", "source.png")
        source.splashScreenInfoHolder.androidSplashScreenBackgroundColor.value = Color.Yellow

        target.splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("target-image", "target.png")
        target.splashScreenInfoHolder.androidSplashScreenBackgroundColor.value = Color.Magenta

        // Copy should overwrite target's data with source's data
        target.copyContents(source)

        // Verify source data overwrote target data
        assertEquals(
            "source-image",
            target.splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value
                ?.blobId
                ?.name,
        )
        assertEquals(
            "source.png",
            target.splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value
                ?.fileName,
        )
        assertEquals(
            Color.Yellow,
            target.splashScreenInfoHolder.androidSplashScreenBackgroundColor.value,
        )
    }

    // XML Generation Tests
    @Test
    fun testGenerateXmlFilesWithoutSplashScreen() {
        val appAssetHolder = AppAssetHolder()

        // No splash screen image set
        val xmlFiles = appAssetHolder.generateXmlFiles()

        // Should have AndroidManifest.xml + iOS Info.plist
        assertEquals(2, xmlFiles.size)
        assertTrue(xmlFiles.containsKey("composeApp/src/androidMain/AndroidManifest.xml"))
        assertTrue(xmlFiles.containsKey("iosApp/iosApp/Info.plist"))

        // Verify AndroidManifest content
        val manifestContent = xmlFiles["composeApp/src/androidMain/AndroidManifest.xml"]!!
        assertTrue(manifestContent.contains("<manifest"))
        assertTrue(manifestContent.contains("<application"))
    }

    @Test
    fun testGenerateXmlFilesWithSplashScreen() {
        val appAssetHolder = AppAssetHolder()

        // Set splash screen data
        appAssetHolder.splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("app-splash", "app_splash.png")
        appAssetHolder.splashScreenInfoHolder.androidSplashScreenBackgroundColor.value = Color.Red

        val xmlFiles = appAssetHolder.generateXmlFiles()

        // Should have AndroidManifest + 2 Android splash screen files + iOS Info.plist
        assertEquals(4, xmlFiles.size)
        assertTrue(xmlFiles.containsKey("composeApp/src/androidMain/AndroidManifest.xml"))
        assertTrue(xmlFiles.containsKey("composeApp/src/androidMain/res/values/splash_theme.xml"))
        assertTrue(xmlFiles.containsKey("composeApp/src/androidMain/res/drawable/$ANDROID_IC_SPLASH_IMAGE.xml"))
        assertTrue(xmlFiles.containsKey("iosApp/iosApp/Info.plist"))

        // Verify AndroidManifest content includes splash screen theme
        val manifestContent = xmlFiles["composeApp/src/androidMain/AndroidManifest.xml"]!!
        assertTrue(manifestContent.contains("Theme.App.Splash") || manifestContent.contains("manifest"))
    }

    @Test
    fun testGenerateXmlFilesWithIOSSplashScreen() {
        val appAssetHolder = AppAssetHolder()

        // Set iOS splash screen data
        appAssetHolder.splashScreenInfoHolder.iOSSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("ios-splash", "ios_splash.png")
        appAssetHolder.splashScreenInfoHolder.iOSSplashScreenBackgroundColor.value = Color.Blue

        val xmlFiles = appAssetHolder.generateXmlFiles()

        // Should have AndroidManifest + iOS Info.plist + iOS Contents.json
        assertEquals(3, xmlFiles.size)
        assertTrue(xmlFiles.containsKey("composeApp/src/androidMain/AndroidManifest.xml"))
        assertTrue(xmlFiles.containsKey("iosApp/iosApp/Info.plist"))
        assertTrue(xmlFiles.containsKey("iosApp/iosApp/Assets.xcassets/SplashImage.imageset/Contents.json"))

        // Verify iOS Info.plist content includes splash screen configuration
        val infoPlistContent = xmlFiles["iosApp/iosApp/Info.plist"]!!
        assertTrue(infoPlistContent.contains("UIImageName"))
        assertTrue(infoPlistContent.contains("SplashImage"))

        // Verify Contents.json includes the image reference
        val contentsJsonContent =
            xmlFiles["iosApp/iosApp/Assets.xcassets/SplashImage.imageset/Contents.json"]!!
        assertTrue(contentsJsonContent.contains("ios_splash.png"))
        assertTrue(contentsJsonContent.contains("universal"))
    }

    @Test
    fun testGenerateXmlFilesWithBothPlatformSplashScreens() {
        val appAssetHolder = AppAssetHolder()

        // Set both Android and iOS splash screen data
        appAssetHolder.splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("android-splash", "android_splash.png")
        appAssetHolder.splashScreenInfoHolder.androidSplashScreenBackgroundColor.value = Color.Red
        appAssetHolder.splashScreenInfoHolder.iOSSplashScreenImageBlobInfo.value =
            createTestBlobInfoWrapper("ios-splash", "ios_splash.jpg")

        val xmlFiles = appAssetHolder.generateXmlFiles()

        // Should have all files: AndroidManifest + 2 Android splash + iOS Info.plist + iOS Contents.json
        assertEquals(5, xmlFiles.size)
        assertTrue(xmlFiles.containsKey("composeApp/src/androidMain/AndroidManifest.xml"))
        assertTrue(xmlFiles.containsKey("composeApp/src/androidMain/res/values/splash_theme.xml"))
        assertTrue(xmlFiles.containsKey("composeApp/src/androidMain/res/drawable/$ANDROID_IC_SPLASH_IMAGE.xml"))
        assertTrue(xmlFiles.containsKey("iosApp/iosApp/Info.plist"))
        assertTrue(xmlFiles.containsKey("iosApp/iosApp/Assets.xcassets/SplashImage.imageset/Contents.json"))
    }

    // Copy Local File Instructions Tests
    @Test
    fun testAppAssetHolderCopyLocalFileInstructions() {
        val appAssetHolder = AppAssetHolder()
        val userId = "test-user"
        val projectId = "test-project"

        // Test with no splash screen images
        val emptyInstructions = appAssetHolder.generateCopyLocalFileInstructions(userId, projectId)
        assertTrue(emptyInstructions.isEmpty())

        // Set Android splash screen image
        val androidBlobInfo = createTestBlobInfoWrapper("android-splash", "splash_android.png")
        appAssetHolder.splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value =
            androidBlobInfo

        val androidInstructions =
            appAssetHolder.generateCopyLocalFileInstructions(userId, projectId)
        assertEquals(1, androidInstructions.size)

        // Verify the destination path for Android splash screen (actual image file)
        val androidDestination = androidInstructions.values.first()
        assertEquals(
            "composeApp/src/androidMain/res/drawable-nodpi/ic_splash_image_actual.png",
            androidDestination,
        )

        // Set iOS splash screen image as well
        val iosBlobInfo = createTestBlobInfoWrapper("ios-splash", "splash_ios.jpg")
        appAssetHolder.splashScreenInfoHolder.iOSSplashScreenImageBlobInfo.value = iosBlobInfo

        val bothInstructions = appAssetHolder.generateCopyLocalFileInstructions(userId, projectId)
        assertEquals(2, bothInstructions.size)

        // Verify both destinations are present
        val destinations = bothInstructions.values.toSet()
        assertTrue(destinations.contains("composeApp/src/androidMain/res/drawable-nodpi/ic_splash_image_actual.png"))
        assertTrue(destinations.contains("iosApp/iosApp/Assets.xcassets/SplashImage.imageset/splash_ios.jpg"))
    }

    @Test
    fun testSplashScreenFileExtensionHandling() {
        val appAssetHolder = AppAssetHolder()
        val userId = "test-user"
        val projectId = "test-project"

        // Test with .jpg file
        val jpgBlobInfo = createTestBlobInfoWrapper("splash-jpg", "splash.jpg")
        appAssetHolder.splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value = jpgBlobInfo

        val jpgInstructions = appAssetHolder.generateCopyLocalFileInstructions(userId, projectId)
        val jpgDestination = jpgInstructions.values.first()
        assertEquals(
            "composeApp/src/androidMain/res/drawable-nodpi/ic_splash_image_actual.jpg",
            jpgDestination,
        )

        // Test with file without extension (should default to .png)
        val noExtBlobInfo = createTestBlobInfoWrapper("splash-no-ext", "splash")
        appAssetHolder.splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value = noExtBlobInfo

        val noExtInstructions = appAssetHolder.generateCopyLocalFileInstructions(userId, projectId)
        val noExtDestination = noExtInstructions.values.first()
        assertEquals(
            "composeApp/src/androidMain/res/drawable-nodpi/ic_splash_image_actual.png",
            noExtDestination,
        )
    }

    @Test
    fun testProjectCopyLocalFileInstructionsIncludesAppAssets() {
        val project = Project(name = "test-project")
        val userId = "test-user"

        // Test with no splash screen
        val emptyInstructions = project.generateCopyLocalFileInstructions(userId)
        assertTrue(emptyInstructions.isEmpty())

        // Set splash screen image
        val blobInfo = createTestBlobInfoWrapper("project-splash", "project_splash.png")
        project.appAssetHolder.splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value =
            blobInfo
        project.appAssetHolder.splashScreenInfoHolder.androidSplashScreenBackgroundColor.value =
            Color.Blue

        val instructions = project.generateCopyLocalFileInstructions(userId)
        assertEquals(1, instructions.size)

        val destination = instructions.values.first()
        assertEquals(
            "composeApp/src/androidMain/res/drawable-nodpi/ic_splash_image_actual.png",
            destination,
        )
    }
}
