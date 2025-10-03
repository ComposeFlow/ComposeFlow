package io.composeflow.ui.themeeditor

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import com.materialkolor.PaletteStyle
import io.composeflow.font.FontFamilyWrapper
import io.composeflow.model.color.ExtendedColor
import io.composeflow.model.enumwrapper.TextStyleWrapper
import io.composeflow.model.project.theme.TextStyleOverride
import io.composeflow.ui.EventResult

data class ThemeEditorCallbacks(
    val onColorSchemeUpdated: (
        sourceColor: Color,
        paletteStyle: PaletteStyle,
        lightScheme: ColorScheme,
        darkScheme: ColorScheme,
    ) -> Unit,
    val onColorResetToDefault: () -> Unit,
    val onPrimaryFontFamilyChanged: (FontFamilyWrapper) -> Unit,
    val onSecondaryFontFamilyChanged: (FontFamilyWrapper) -> Unit,
    val onTextStyleOverridesChanged: (TextStyleWrapper, TextStyleOverride) -> Unit,
    val onApplyFontEditableParams: () -> Unit,
    val onResetFonts: () -> Unit,
    val onKeyPressed: (KeyEvent) -> EventResult,
    val onUndo: () -> Unit,
    val onRedo: () -> Unit,
    val onAddNewExtendedColor: (extendedColor: ExtendedColor) -> Unit,
    val onChangeExtendedColor: (extendedColor: ExtendedColor, newExtendedColor: ExtendedColor) -> Unit,
    val onRenameExtendedColor: (extendedColor: ExtendedColor, newName: String) -> Unit,
    val onDeleteExtendedColor: (extendedColor: ExtendedColor) -> Unit,
)
