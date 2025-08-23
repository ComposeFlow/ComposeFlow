package io.composeflow.model.project.theme

import io.composeflow.font.FontFamilyWrapper
import io.composeflow.font.FontWeightWrapper
import io.composeflow.model.enumwrapper.TextStyleWrapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FontHolderTest {
    @Test
    fun testCopyContents() {
        val source = FontHolder()

        // Modify source properties
        source.primaryFontFamily = FontFamilyWrapper.Caveat
        source.secondaryFontFamily = FontFamilyWrapper.DancingScript

        // Add text style overrides
        source.textStyleOverrides[TextStyleWrapper.DisplayLarge] =
            TextStyleOverride(
                fontSize = 32,
                letterSpacing = 1.5f,
                fontWeight = FontWeightWrapper.Bold,
                fontFamilyCandidate = FontFamilyCandidate.Secondary,
            )
        source.textStyleOverrides[TextStyleWrapper.BodyMedium] =
            TextStyleOverride(
                fontSize = 16,
                fontWeight = FontWeightWrapper.Medium,
            )

        val target = FontHolder()

        // Set different initial values that should be overwritten
        target.primaryFontFamily = FontFamilyWrapper.Lato
        target.secondaryFontFamily = FontFamilyWrapper.Montserrat
        target.textStyleOverrides[TextStyleWrapper.TitleLarge] = TextStyleOverride(fontSize = 20)

        target.copyContents(source)

        // Verify primary and secondary font families were copied
        assertEquals(FontFamilyWrapper.Caveat, target.primaryFontFamily)
        assertEquals(FontFamilyWrapper.DancingScript, target.secondaryFontFamily)

        // Verify text style overrides were copied and old ones cleared
        assertEquals(2, target.textStyleOverrides.size)
        assertTrue(target.textStyleOverrides.containsKey(TextStyleWrapper.DisplayLarge))
        assertTrue(target.textStyleOverrides.containsKey(TextStyleWrapper.BodyMedium))

        // Verify specific override details
        val displayLargeOverride = target.textStyleOverrides[TextStyleWrapper.DisplayLarge]!!
        assertEquals(32, displayLargeOverride.fontSize)
        assertEquals(1.5f, displayLargeOverride.letterSpacing)
        assertEquals(FontWeightWrapper.Bold, displayLargeOverride.fontWeight)
        assertEquals(FontFamilyCandidate.Secondary, displayLargeOverride.fontFamilyCandidate)

        val bodyMediumOverride = target.textStyleOverrides[TextStyleWrapper.BodyMedium]!!
        assertEquals(16, bodyMediumOverride.fontSize)
        assertEquals(FontWeightWrapper.Medium, bodyMediumOverride.fontWeight)
    }

    @Test
    fun testCopyContentsWithEmptyOverrides() {
        val source = FontHolder()
        source.primaryFontFamily = FontFamilyWrapper.Roboto
        source.secondaryFontFamily = FontFamilyWrapper.Ubuntu
        // textStyleOverrides is empty by default

        val target = FontHolder()
        target.primaryFontFamily = FontFamilyWrapper.Oswald
        target.textStyleOverrides[TextStyleWrapper.HeadlineLarge] = TextStyleOverride(fontSize = 24)

        target.copyContents(source)

        assertEquals(FontFamilyWrapper.Roboto, target.primaryFontFamily)
        assertEquals(FontFamilyWrapper.Ubuntu, target.secondaryFontFamily)
        assertTrue(target.textStyleOverrides.isEmpty())
    }

    @Test
    fun testCopyContentsCompleteOverwrite() {
        val source = FontHolder()
        source.primaryFontFamily = FontFamilyWrapper.Ubuntu
        source.secondaryFontFamily = FontFamilyWrapper.PTSans
        source.textStyleOverrides[TextStyleWrapper.LabelSmall] =
            TextStyleOverride(
                fontSize = 12,
                letterSpacing = 0.5f,
            )

        val target = FontHolder()
        target.primaryFontFamily = FontFamilyWrapper.Raleway
        target.secondaryFontFamily = FontFamilyWrapper.Lato
        target.textStyleOverrides[TextStyleWrapper.DisplayMedium] = TextStyleOverride(fontSize = 28)
        target.textStyleOverrides[TextStyleWrapper.BodyLarge] = TextStyleOverride(fontSize = 18)

        target.copyContents(source)

        // Verify complete overwrite
        assertEquals(FontFamilyWrapper.Ubuntu, target.primaryFontFamily)
        assertEquals(FontFamilyWrapper.PTSans, target.secondaryFontFamily)
        assertEquals(1, target.textStyleOverrides.size)
        assertTrue(target.textStyleOverrides.containsKey(TextStyleWrapper.LabelSmall))

        val labelSmallOverride = target.textStyleOverrides[TextStyleWrapper.LabelSmall]!!
        assertEquals(12, labelSmallOverride.fontSize)
        assertEquals(0.5f, labelSmallOverride.letterSpacing)
    }
}
