package io.composeflow.model.project.appassets
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import io.composeflow.android.generateSplashThemeXml
import io.composeflow.cloud.storage.BlobInfoWrapper
import io.composeflow.serializer.LocationAwareColorSerializer
import io.composeflow.serializer.MutableStateSerializer
import kotlinx.serialization.Serializable

const val ANDROID_IC_SPLASH_IMAGE = "ic_splash_image"

@Serializable
data class SplashScreenInfoHolder(
    @Serializable(MutableStateSerializer::class)
    val androidSplashScreenImageBlobInfo: MutableState<BlobInfoWrapper?> = mutableStateOf(null),
    @Serializable(MutableStateSerializer::class)
    val androidSplashScreenBackgroundColor: MutableState<
        @Serializable(LocationAwareColorSerializer::class)
        Color?,
    > =
        mutableStateOf(
            null,
        ),
    @Serializable(MutableStateSerializer::class)
    val iOSSplashScreenImageBlobInfo: MutableState<BlobInfoWrapper?> = mutableStateOf(null),
    @Serializable(MutableStateSerializer::class)
    val iOSSplashScreenBackgroundColor: MutableState<
        @Serializable(LocationAwareColorSerializer::class)
        Color?,
    > =
        mutableStateOf(
            null,
        ),
) {
    fun isAndroidSplashScreenEnabled(): Boolean = androidSplashScreenImageBlobInfo.value != null

    fun isIOSSplashScreenEnabled(): Boolean = iOSSplashScreenImageBlobInfo.value != null

    /**
     * Generates XML files for Android splash screen.
     * Returns a map where:
     * - Key: destination path relative to project root
     * - Value: XML content to write
     */
    fun generateXmlFiles(): Map<String, String> {
        val result = mutableMapOf<String, String>()

        // Add Android splash screen files
        if (isAndroidSplashScreenEnabled()) {
            // Generate splash theme XML
            val splashThemeXml = generateSplashThemeXml(androidSplashScreenBackgroundColor.value)
            result["composeApp/src/androidMain/res/values/splash_theme.xml"] = splashThemeXml

            // Generate splash image drawable XML
            val splashImageXml = generateSplashImageDrawableXml()
            result["composeApp/src/androidMain/res/drawable/$ANDROID_IC_SPLASH_IMAGE.xml"] =
                splashImageXml
        }

        // Add iOS splash screen files
        if (isIOSSplashScreenEnabled()) {
            // Generate Contents.json for SplashImage.imageset
            val contentsJson = generateSplashImageContentsJson()
            result["iosApp/iosApp/Assets.xcassets/SplashImage.imageset/Contents.json"] = contentsJson
        }

        return result
    }

    private fun generateSplashImageDrawableXml(): String {
        // Generate a layer-list drawable that references the actual image file
        // The actual image file is copied to drawable-nodpi by generateCopyLocalFileInstructions
        // Note: Android drawable references don't include file extensions
        val actualImageResourceName = "${ANDROID_IC_SPLASH_IMAGE}_actual"

        return """
            <?xml version="1.0" encoding="utf-8"?>
            <layer-list xmlns:android="http://schemas.android.com/apk/res/android">
                <item android:drawable="@drawable/$actualImageResourceName" android:gravity="center" />
            </layer-list>
            """.trimIndent()
    }

    private fun generateSplashImageContentsJson(): String {
        // Get the image file name from the blob info
        val fileName = iOSSplashScreenImageBlobInfo.value?.fileName ?: "splash.png"

        return """
            {
              "images" : [
                {
                  "filename" : "$fileName",
                  "idiom" : "universal",
                  "scale" : "1x"
                }
              ],
              "info" : {
                "author" : "xcode",
                "version" : 1
              }
            }
            """.trimIndent()
    }
}

fun SplashScreenInfoHolder.copyContents(other: SplashScreenInfoHolder) {
    androidSplashScreenImageBlobInfo.value = other.androidSplashScreenImageBlobInfo.value
    androidSplashScreenBackgroundColor.value = other.androidSplashScreenBackgroundColor.value
    iOSSplashScreenImageBlobInfo.value = other.iOSSplashScreenImageBlobInfo.value
    iOSSplashScreenBackgroundColor.value = other.iOSSplashScreenBackgroundColor.value
}
