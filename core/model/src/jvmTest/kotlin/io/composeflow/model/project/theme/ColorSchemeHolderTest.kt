package io.composeflow.model.project.theme

import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import io.composeflow.model.color.ColorSchemeWrapper
import kotlin.test.Test
import kotlin.test.assertEquals

class ColorSchemeHolderTest {
    @Test
    fun testCopyContents() {
        val source = ColorSchemeHolder()

        // Modify source properties
        source.sourceColor = Color.Red
        source.paletteStyle = PaletteStyle.Vibrant

        // Create custom color schemes
        val customLightScheme =
            ColorSchemeWrapper.fromColorScheme(
                androidx.compose.material3.lightColorScheme(
                    primary = Color.Blue,
                    secondary = Color.Green,
                ),
            )
        val customDarkScheme =
            ColorSchemeWrapper.fromColorScheme(
                androidx.compose.material3.darkColorScheme(
                    primary = Color.Cyan,
                    secondary = Color.Magenta,
                ),
            )

        source.lightColorScheme.value = customLightScheme
        source.darkColorScheme.value = customDarkScheme

        val target = ColorSchemeHolder()

        // Set different initial values that should be overwritten
        target.sourceColor = Color.Yellow
        target.paletteStyle = PaletteStyle.Neutral

        target.copyContents(source)

        // Verify all properties were copied
        assertEquals(Color.Red, target.sourceColor)
        assertEquals(PaletteStyle.Vibrant, target.paletteStyle)
        assertEquals(customLightScheme, target.lightColorScheme.value)
        assertEquals(customDarkScheme, target.darkColorScheme.value)
    }

    @Test
    fun testCopyContentsWithNullSourceColor() {
        val source = ColorSchemeHolder()
        source.sourceColor = null
        source.paletteStyle = PaletteStyle.Expressive

        val target = ColorSchemeHolder()
        target.sourceColor = Color.Black
        target.paletteStyle = PaletteStyle.TonalSpot

        target.copyContents(source)

        assertEquals(null, target.sourceColor)
        assertEquals(PaletteStyle.Expressive, target.paletteStyle)
    }

    @Test
    fun testCopyContentsCompleteOverwrite() {
        val source = ColorSchemeHolder()
        source.sourceColor = Color(0xFF123456)
        source.paletteStyle = PaletteStyle.Rainbow

        val sourceLightScheme =
            ColorSchemeWrapper.fromColorScheme(
                androidx.compose.material3.lightColorScheme(primary = Color.Red),
            )
        val sourceDarkScheme =
            ColorSchemeWrapper.fromColorScheme(
                androidx.compose.material3.darkColorScheme(primary = Color.Blue),
            )
        source.lightColorScheme.value = sourceLightScheme
        source.darkColorScheme.value = sourceDarkScheme

        val target = ColorSchemeHolder()
        target.sourceColor = Color(0xFFABCDEF)
        target.paletteStyle = PaletteStyle.Monochrome

        val originalLightScheme = target.lightColorScheme.value
        val originalDarkScheme = target.darkColorScheme.value

        target.copyContents(source)

        // Verify complete overwrite
        assertEquals(Color(0xFF123456), target.sourceColor)
        assertEquals(PaletteStyle.Rainbow, target.paletteStyle)
        assertEquals(sourceLightScheme, target.lightColorScheme.value)
        assertEquals(sourceDarkScheme, target.darkColorScheme.value)

        // Verify the schemes were actually changed
        assert(target.lightColorScheme.value != originalLightScheme)
        assert(target.darkColorScheme.value != originalDarkScheme)
    }
}
