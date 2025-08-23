package io.composeflow.model.project.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import io.composeflow.font.FontFamilyWrapper
import io.composeflow.font.FontWeightWrapper
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.kotlinpoet.wrapper.AnnotationSpecWrapper
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.FileSpecBuilderWrapper
import io.composeflow.kotlinpoet.wrapper.FileSpecWrapper
import io.composeflow.kotlinpoet.wrapper.FunSpecWrapper
import io.composeflow.kotlinpoet.wrapper.asTypeNameWrapper
import io.composeflow.kotlinpoet.wrapper.suppressRedundantVisibilityModifier
import io.composeflow.model.enumwrapper.TextStyleWrapper
import io.composeflow.model.project.COMPOSEFLOW_PACKAGE
import io.composeflow.serializer.FallbackMutableStateMapSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias TextStyleOverrides = MutableMap<TextStyleWrapper, TextStyleOverride>

val defaultFontFamily = FontFamilyWrapper.NotoSansJP

fun FontHolder.copyContents(args: FontHolder) {
    textStyleOverrides.clear()
    textStyleOverrides.putAll(args.textStyleOverrides)
    primaryFontFamily = args.primaryFontFamily
    secondaryFontFamily = args.secondaryFontFamily
}

@Serializable
@SerialName("FontHolder")
data class FontHolder(
    @Serializable(FallbackMutableStateMapSerializer::class)
    val textStyleOverrides: TextStyleOverrides = mutableStateMapOf(),
    // Intentionally var so that overwriting them are easy
    var primaryFontFamily: FontFamilyWrapper = defaultFontFamily,
    var secondaryFontFamily: FontFamilyWrapper = defaultFontFamily,
) {
    @Composable
    fun generateTypography(): Typography =
        Typography().generateWithOverrides(
            primaryFontFamily,
            secondaryFontFamily,
            textStyleOverrides,
        )

    fun resetToDefaults() {
        primaryFontFamily = defaultFontFamily
        secondaryFontFamily = defaultFontFamily
        textStyleOverrides.clear()
    }

    fun generateFontFile(): FileSpecWrapper {
        val fileBuilder = FileSpecWrapper.builder("${COMPOSEFLOW_PACKAGE}.common", "Font")
        // generateFontFamilyFunSpec returns Any (FunSpec on JVM, Unit on WASM)
        // We need platform-specific handling
        addFontFamilyFunSpec(fileBuilder, primaryFontFamily)

        if (primaryFontFamily != secondaryFontFamily) {
            addFontFamilyFunSpec(fileBuilder, secondaryFontFamily)
        }

        val typographyFunSpecBuilder =
            FunSpecWrapper
                .builder("AppTypography")
                .addAnnotation(AnnotationSpecWrapper.get(Composable::class))
                .returns(Typography::class.asTypeNameWrapper())

        val primaryFontFamilyVariable = primaryFontFamily.fontFamilyName().lowercase()
        val secondaryFontFamilyVariable = secondaryFontFamily.fontFamilyName().lowercase()

        typographyFunSpecBuilder.addStatement("val $primaryFontFamilyVariable = ${primaryFontFamily.fontFamilyName()}()")
        if (primaryFontFamily != secondaryFontFamily) {
            typographyFunSpecBuilder.addStatement("val $secondaryFontFamilyVariable = ${secondaryFontFamily.fontFamilyName()}()")
        }

        typographyFunSpecBuilder.addCode(
            CodeBlockWrapper.of(
                "return %M().run {",
                MemberHolder.Material3.Typography,
            ),
        )

        typographyFunSpecBuilder.addCode("copy(")
        TextStyleWrapper.entries.forEach {
            typographyFunSpecBuilder.addCode("${it.styleName} = ${it.styleName}.copy(")
            val textStyleOverride = textStyleOverrides[it]
            if (textStyleOverride == null) {
                typographyFunSpecBuilder.addCode("fontFamily = $primaryFontFamilyVariable),")
            } else {
                textStyleOverride.fontSize?.let { fontSize ->
                    typographyFunSpecBuilder.addCode(
                        CodeBlockWrapper.of(
                            "fontSize = $fontSize.%M, ",
                            MemberHolder.AndroidX.Ui.sp,
                        ),
                    )
                }
                textStyleOverride.fontWeight?.let { fontWeight ->
                    typographyFunSpecBuilder.addCode(
                        CodeBlockWrapper.of(
                            "fontWeight = %M.${fontWeight.name}, ",
                            MemberHolder.AndroidX.Ui.FontWeight,
                        ),
                    )
                }
                textStyleOverride.letterSpacing?.let { letterSpacing ->
                    typographyFunSpecBuilder.addCode(
                        CodeBlockWrapper.of(
                            "letterSpacing = $letterSpacing.%M, ",
                            MemberHolder.AndroidX.Ui.sp,
                        ),
                    )
                }
                when (textStyleOverride.fontFamilyCandidate) {
                    FontFamilyCandidate.Primary -> {
                        typographyFunSpecBuilder.addCode(
                            "fontFamily = $primaryFontFamilyVariable,",
                        )
                    }

                    FontFamilyCandidate.Secondary -> {
                        typographyFunSpecBuilder.addCode(
                            "fontFamily = $secondaryFontFamilyVariable,",
                        )
                    }
                }
                typographyFunSpecBuilder.addStatement("),") // Close displayLarge.copy()
            }
        }
        typographyFunSpecBuilder.addStatement(")") // Close Typography.copy()
        typographyFunSpecBuilder.addStatement("}") // Close Typography().run {

        fileBuilder.addFunction(typographyFunSpecBuilder.build())
        fileBuilder.suppressRedundantVisibilityModifier()
        return fileBuilder.build()
    }

    /**
     * Generate the instructions to copy specified resources into the destination directory.
     * This is to ensure that font files are included in the generated app.
     *
     * The from path is expected to be loaded from resources folder obtained like
     * `object {}.javaClass.getResourceAsStream("/composeResources/io.composeflow/font/Caveat-Bold.ttf")`
     *
     * The destination path is a relative path from the app's root directory e.g.:
     * `"composeApp/src/commonMain/composeResources/font/Caveat-Bold.ttf"`
     */
    fun generateCopyFileInstructions(): Map<String, String> =
        buildMap {
            primaryFontFamily.fontFileWrappers.forEach {
                put(
                    "/composeResources/io.composeflow/font/${it.fontFileName}",
                    "composeApp/src/commonMain/composeResources/font/${it.fontFileName}",
                )
            }
            secondaryFontFamily.fontFileWrappers.forEach {
                put(
                    "/composeResources/io.composeflow/font/${it.fontFileName}",
                    "composeApp/src/commonMain/composeResources/font/${it.fontFileName}",
                )
            }
        }
}

enum class FontFamilyCandidate {
    Primary,
    Secondary,
}

@Serializable
data class TextStyleOverride(
    val fontSize: Int? = null,
    val letterSpacing: Float? = null,
    val fontWeight: FontWeightWrapper? = null,
    val fontFamilyCandidate: FontFamilyCandidate = FontFamilyCandidate.Primary,
) {
    @Composable
    fun asTextStyle(
        primaryFontFamily: FontFamilyWrapper,
        secondaryFontFamily: FontFamilyWrapper,
        originalStyle: TextStyle,
    ): TextStyle {
        var result = originalStyle
        fontSize?.let {
            result = result.copy(fontSize = it.sp)
        }
        letterSpacing?.let {
            result = result.copy(letterSpacing = it.sp)
        }
        fontWeight?.let {
            result = result.copy(fontWeight = it.asFontWeight())
        }
        val fontFamily =
            when (fontFamilyCandidate) {
                FontFamilyCandidate.Primary -> primaryFontFamily.asFontFamily()
                FontFamilyCandidate.Secondary -> secondaryFontFamily.asFontFamily()
            }
        result = result.copy(fontFamily = fontFamily)
        return result
    }
}

@Composable
fun Typography.generateWithOverrides(
    primaryFontFamily: FontFamilyWrapper,
    secondaryFontFamily: FontFamilyWrapper,
    textStyleOverrides: TextStyleOverrides,
): Typography =
    copy(
        displayLarge =
            textStyleOverrides[TextStyleWrapper.DisplayLarge]?.asTextStyle(
                primaryFontFamily,
                secondaryFontFamily,
                displayLarge,
            ) ?: displayLarge.copy(fontFamily = primaryFontFamily.asFontFamily()),
        displayMedium =
            textStyleOverrides[TextStyleWrapper.DisplayMedium]?.asTextStyle(
                primaryFontFamily,
                secondaryFontFamily,
                displayMedium,
            ) ?: displayMedium.copy(fontFamily = primaryFontFamily.asFontFamily()),
        displaySmall =
            textStyleOverrides[TextStyleWrapper.DisplaySmall]?.asTextStyle(
                primaryFontFamily,
                secondaryFontFamily,
                displaySmall,
            ) ?: displaySmall.copy(fontFamily = primaryFontFamily.asFontFamily()),
        headlineLarge =
            textStyleOverrides[TextStyleWrapper.HeadlineLarge]?.asTextStyle(
                primaryFontFamily,
                secondaryFontFamily,
                headlineLarge,
            ) ?: headlineLarge.copy(fontFamily = primaryFontFamily.asFontFamily()),
        headlineMedium =
            textStyleOverrides[TextStyleWrapper.HeadlineMedium]?.asTextStyle(
                primaryFontFamily,
                secondaryFontFamily,
                headlineMedium,
            ) ?: headlineMedium.copy(fontFamily = primaryFontFamily.asFontFamily()),
        headlineSmall =
            textStyleOverrides[TextStyleWrapper.HeadlineSmall]?.asTextStyle(
                primaryFontFamily,
                secondaryFontFamily,
                headlineSmall,
            ) ?: headlineSmall.copy(fontFamily = primaryFontFamily.asFontFamily()),
        titleLarge =
            textStyleOverrides[TextStyleWrapper.TitleLarge]?.asTextStyle(
                primaryFontFamily,
                secondaryFontFamily,
                titleLarge,
            ) ?: titleLarge.copy(fontFamily = primaryFontFamily.asFontFamily()),
        titleMedium =
            textStyleOverrides[TextStyleWrapper.TitleMedium]?.asTextStyle(
                primaryFontFamily,
                secondaryFontFamily,
                titleMedium,
            ) ?: titleMedium.copy(fontFamily = primaryFontFamily.asFontFamily()),
        titleSmall =
            textStyleOverrides[TextStyleWrapper.TitleSmall]?.asTextStyle(
                primaryFontFamily,
                secondaryFontFamily,
                titleSmall,
            ) ?: titleSmall.copy(fontFamily = primaryFontFamily.asFontFamily()),
        bodyLarge =
            textStyleOverrides[TextStyleWrapper.BodyLarge]?.asTextStyle(
                primaryFontFamily,
                secondaryFontFamily,
                bodyLarge,
            )
                ?: bodyLarge.copy(fontFamily = primaryFontFamily.asFontFamily()),
        bodyMedium =
            textStyleOverrides[TextStyleWrapper.BodyMedium]?.asTextStyle(
                primaryFontFamily,
                secondaryFontFamily,
                bodyMedium,
            ) ?: bodyMedium.copy(fontFamily = primaryFontFamily.asFontFamily()),
        bodySmall =
            textStyleOverrides[TextStyleWrapper.BodySmall]?.asTextStyle(
                primaryFontFamily,
                secondaryFontFamily,
                bodySmall,
            )
                ?: bodySmall.copy(fontFamily = primaryFontFamily.asFontFamily()),
        labelLarge =
            textStyleOverrides[TextStyleWrapper.LabelLarge]?.asTextStyle(
                primaryFontFamily,
                secondaryFontFamily,
                labelLarge,
            ) ?: labelLarge.copy(fontFamily = primaryFontFamily.asFontFamily()),
        labelMedium =
            textStyleOverrides[TextStyleWrapper.LabelMedium]?.asTextStyle(
                primaryFontFamily,
                secondaryFontFamily,
                labelMedium,
            ) ?: labelMedium.copy(fontFamily = primaryFontFamily.asFontFamily()),
        labelSmall =
            textStyleOverrides[TextStyleWrapper.LabelSmall]?.asTextStyle(
                primaryFontFamily,
                secondaryFontFamily,
                labelSmall,
            ) ?: labelSmall.copy(fontFamily = primaryFontFamily.asFontFamily()),
    )

// Platform-specific helper to add font family function spec to the file builder
internal expect fun addFontFamilyFunSpec(
    fileBuilder: FileSpecBuilderWrapper,
    fontFamily: FontFamilyWrapper,
)
