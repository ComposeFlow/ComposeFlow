package io.composeflow.model.project.theme

import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import io.composeflow.font.FontFamilyWrapper
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeHolderTest {
    @Test
    fun testCopyContents() {
        val source = ThemeHolder()

        // Modify source ColorSchemeHolder
        source.colorSchemeHolder.sourceColor = Color.Red
        source.colorSchemeHolder.paletteStyle = PaletteStyle.Vibrant

        // Modify source FontHolder
        source.fontHolder.primaryFontFamily = FontFamilyWrapper.Caveat
        source.fontHolder.secondaryFontFamily = FontFamilyWrapper.DancingScript

        val target = ThemeHolder()
        target.copyContents(source)

        // Verify ColorSchemeHolder was copied
        assertEquals(Color.Red, target.colorSchemeHolder.sourceColor)
        assertEquals(PaletteStyle.Vibrant, target.colorSchemeHolder.paletteStyle)

        // Verify FontHolder was copied
        assertEquals(FontFamilyWrapper.Caveat, target.fontHolder.primaryFontFamily)
        assertEquals(FontFamilyWrapper.DancingScript, target.fontHolder.secondaryFontFamily)
    }

    @Test
    fun testCopyContentsOverwritesExistingData() {
        val source = ThemeHolder()
        source.colorSchemeHolder.sourceColor = Color.Blue
        source.fontHolder.primaryFontFamily = FontFamilyWrapper.DancingScript

        val target = ThemeHolder()
        // Set initial values that should be overwritten
        target.colorSchemeHolder.sourceColor = Color.Green
        target.fontHolder.primaryFontFamily = FontFamilyWrapper.Montserrat

        target.copyContents(source)

        // Verify old values were overwritten
        assertEquals(Color.Blue, target.colorSchemeHolder.sourceColor)
        assertEquals(FontFamilyWrapper.DancingScript, target.fontHolder.primaryFontFamily)
    }
}
