package io.composeflow.model.project.theme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import io.composeflow.kotlinpoet.wrapper.FileSpecWrapper
import io.composeflow.model.color.ColorSchemeWrapper
import io.composeflow.model.project.COMPOSEFLOW_PACKAGE
import io.composeflow.serializer.LocationAwareColorSerializer
import io.composeflow.serializer.MutableStateSerializer
import io.composeflow.ui.common.defaultDarkScheme
import io.composeflow.ui.common.defaultLightScheme
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ColorSchemeHolder")
data class ColorSchemeHolder(
    /**
     * Source color of the color schemes to generate the lightScheme and darkScheme
     */
    @Serializable(LocationAwareColorSerializer::class)
    var sourceColor: Color? = null,
    /**
     * Source palette style to generate the lightScheme and darkScheme
     */
    var paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    // Using MutableState so that the color scheme editor screen is aware of any changes of
    // color schemes
    @Serializable(MutableStateSerializer::class)
    val lightColorScheme: MutableState<ColorSchemeWrapper> =
        mutableStateOf(ColorSchemeWrapper.fromColorScheme(defaultLightScheme)),
    @Serializable(MutableStateSerializer::class)
    val darkColorScheme: MutableState<ColorSchemeWrapper> =
        mutableStateOf(ColorSchemeWrapper.fromColorScheme(defaultDarkScheme)),
) {
    fun generateColorFile(): FileSpecWrapper {
        val fileSpecBuilder = FileSpecWrapper.builder("$COMPOSEFLOW_PACKAGE.common", "Color")
        lightColorScheme.value.generateColorProperties(suffix = "Light").forEach {
            fileSpecBuilder.addProperty(it)
        }
        darkColorScheme.value.generateColorProperties(suffix = "Dark").forEach {
            fileSpecBuilder.addProperty(it)
        }
        return fileSpecBuilder.build()
    }
}

fun ColorSchemeHolder.copyContents(arg: ColorSchemeHolder) {
    sourceColor = arg.sourceColor
    paletteStyle = arg.paletteStyle
    lightColorScheme.value = arg.lightColorScheme.value
    darkColorScheme.value = arg.darkColorScheme.value
}
