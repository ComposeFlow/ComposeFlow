package io.composeflow.model.parameter.wrapper

import androidx.compose.ui.graphics.Color
import io.composeflow.serializer.asString
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import junit.framework.TestCase.assertEquals
import kotlinx.serialization.encodeToString
import kotlin.test.Test

class ColorWrapperTest {
    @Test
    fun withThemeColor_verify_restoredAsSame() {
        val colorWrapper = ColorWrapper(themeColor = Material3ColorWrapper.Surface)
        val decoded = encodeToString(colorWrapper)
        assertEquals(colorWrapper, decodeFromStringWithFallback<ColorWrapper>(decoded))
    }

    @Test
    fun withoutThemeColorWithAlpha_verify_restoredAsSame() {
        val colorWrapper = ColorWrapper(themeColor = null, color = Color(0xFF8A123F))
        val decoded = encodeToString(colorWrapper)
        assertEquals(colorWrapper, decodeFromStringWithFallback<ColorWrapper>(decoded))
    }

    @Test
    fun withoutThemeColorWithAlpha_nonFullOpaque_verify_restoredAsSame() {
        val colorWrapper = ColorWrapper(themeColor = null, color = Color(0x328A123F))
        val decoded = encodeToString(colorWrapper)
        assertEquals(colorWrapper, decodeFromStringWithFallback<ColorWrapper>(decoded))
    }

    @Test
    fun colorAsString_withoutAlpha() {
        val color = Color(0x8A123F)
        assertEquals("0xFF8A123F", color.asString())
    }

    @Test
    fun colorAsString_withAlpha_fullOpaque() {
        val color = Color(0xFF8A123F)
        assertEquals("0xFF8A123F", color.asString())
    }

    @Test
    fun colorAsString_withAlpha_notFullOpaque() {
        val color = Color(0x238A123F)
        assertEquals("0x238A123F", color.asString())
    }

    @Test
    fun deserialize_hexColorString_withoutAlpha() {
        val yaml =
            "themeColor: null\n" +
                "color: \"#23AB01\""
        val decoded = decodeFromStringWithFallback<ColorWrapper>(yaml)
        assertEquals(Color(0xFF23AB01.toInt()), decoded.color)
    }

    @Test
    fun deserialize_without_themColor() {
        val yaml = "color: \"#23AB01\""
        val decoded = decodeFromStringWithFallback<ColorWrapper>(yaml)
        assertEquals(Color(0xFF23AB01.toInt()), decoded.color)
    }

    @Test
    fun deserialize_hexColorString_withAlpha() {
        val yaml =
            "themeColor: null\n" +
                "color: \"#8023AB01\""
        val decoded = decodeFromStringWithFallback<ColorWrapper>(yaml)
        assertEquals(Color(0x8023AB01.toInt()), decoded.color)
    }
}
