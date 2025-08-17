package io.composeflow.font

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource

enum class FontFamilyWrapper(
    val displayName: String,
    val fontFileWrappers: List<FontFileWrapper>,
) {
    Roboto("Roboto", robotoFontFiles),
    NotoSansJP("Noto Sans JP", notoSansJpFontFiles),
    BaskervvilleSC("Baskervville SC", baskervvillescFontFiles),
    BungeeTint("Bungee Tint", bungeetintFontFiles),
    Caveat("Caveat", caveatFontFiles),
    CrimsonText("Crimson Text", crimsontextFontFiles),
    DancingScript("Dancing Script", dancingscriptFontFiles),
    Handjet("Handjet", handjetFontFiles),
    Lato("Lato", latoFontFiles),
    LibreBaskerville("Libre Baskerville", librebaskervilleFontFiles),
    Montserrat("Montserrat", montserratFontFiles),
    NerkoOne("Nerko One", nerkooneFontFiles),
    Oswald("Oswald", oswaldFontFiles),
    PlayfairDisplay("Playfair Display", playfairdisplayFontFiles),
    ProtestGuerilla("Protest Guerilla", protestguerrillaFontFiles),
    PTSans("PT Sans", ptsansFontFiles),
    PTSerif("PT Serif", ptserifFontFiles),
    Raleway("Raleway", ralewayFontFiles),
    Sevillana("Sevillana", sevillanaFontFiles),
    SourceCodePro("Source Code Pro", sourcecodeproFontFiles),
    SUSE("SUSE", suseFontFiles),
    Teko("Teko", tekoFontFiles),
    Ubuntu("Ubuntu", ubuntuFontFiles),
    ;

    @Composable
    fun asFontFamily(): FontFamily = FontFamily(fontFileWrappers.map { it.asFont() })

    fun fontFamilyName(): String = "${name}FontFamily"
}

// TODO: Temporarily defining the type as Any for multiplatform compatibility. Define common
// interface
expect fun FontFamilyWrapper.generateFontFamilyFunSpec(): Any

data class FontFileWrapper(
    val fontFileName: String,
    val fontResource: FontResource,
    val fontWeight: FontWeight = FontWeight.Normal,
    val fontStyle: FontStyle = FontStyle.Normal,
) {
    val fontResourceName =
        fontFileName
            .replace("-", "_")
            .replace(".ttf", "")

    @Composable
    fun asFont(): Font =
        Font(
            resource = fontResource,
            weight = fontWeight,
            style = fontStyle,
        )
}
