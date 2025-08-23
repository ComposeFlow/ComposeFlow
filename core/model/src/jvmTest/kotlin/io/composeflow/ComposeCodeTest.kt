package io.composeflow

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Rect
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.parameter.BoxTrait
import io.composeflow.model.parameter.ColumnTrait
import io.composeflow.model.parameter.LazyColumnTrait
import io.composeflow.model.parameter.RowTrait
import io.composeflow.model.parameter.TextTrait
import io.composeflow.model.parameter.wrapper.AlignmentHorizontalWrapper
import io.composeflow.model.parameter.wrapper.AlignmentVerticalWrapper
import io.composeflow.model.parameter.wrapper.AlignmentWrapper
import io.composeflow.model.parameter.wrapper.ArrangementHorizontalWrapper
import io.composeflow.model.parameter.wrapper.ArrangementVerticalWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.StringProperty
import io.composeflow.override.mutableStateListEqualsOverrideOf
import org.junit.Test
import kotlin.test.assertEquals

class ComposeCodeTest {
    @Test
    fun toComposeCode_emptyBox() {
        val root =
            ComposeNode(
                trait = mutableStateOf(BoxTrait()),
                label = mutableStateOf("Root"),
            )
        root.boundsInWindow.value = Rect(left = 0f, top = 0f, right = 200f, bottom = 200f)

        val code = root.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """
            androidx.compose.foundation.layout.Box {}
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_BoxWithParameters() {
        val root =
            ComposeNode(
                label = mutableStateOf("Root"),
                trait =
                    mutableStateOf(
                        BoxTrait(
                            contentAlignment = AlignmentWrapper.TopCenter,
                        ),
                    ),
            )
        root.boundsInWindow.value = Rect(left = 0f, top = 0f, right = 200f, bottom = 200f)

        val code = root.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """
            androidx.compose.foundation.layout.Box(
              contentAlignment = androidx.compose.ui.Alignment.TopCenter,
            ) {}
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_BoxWithModifiers() {
        val root =
            ComposeNode(
                label = mutableStateOf("Root"),
                trait =
                    mutableStateOf(
                        BoxTrait(
                            contentAlignment = AlignmentWrapper.TopCenter,
                        ),
                    ),
                modifierList = mutableStateListEqualsOverrideOf(ModifierWrapper.FillMaxSize()),
            )
        root.boundsInWindow.value = Rect(left = 0f, top = 0f, right = 200f, bottom = 200f)

        val code = root.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """
            androidx.compose.foundation.layout.Box(
              contentAlignment = androidx.compose.ui.Alignment.TopCenter,
              modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.fillMaxSize(),
            ) {}
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_emptyColumn() {
        val root =
            ComposeNode(
                trait = mutableStateOf(ColumnTrait()),
                label = mutableStateOf("Root"),
            )
        root.boundsInWindow.value = Rect(left = 0f, top = 0f, right = 200f, bottom = 200f)

        val code = root.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """
            androidx.compose.foundation.layout.Column {}
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_ColumnWithParameters() {
        val root =
            ComposeNode(
                label = mutableStateOf("Root"),
                trait =
                    mutableStateOf(
                        ColumnTrait(
                            verticalArrangementWrapper = ArrangementVerticalWrapper.Top,
                            horizontalAlignmentWrapper = AlignmentHorizontalWrapper.Start,
                        ),
                    ),
            )
        root.boundsInWindow.value = Rect(left = 0f, top = 0f, right = 200f, bottom = 200f)

        val code = root.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """
            androidx.compose.foundation.layout.Column(
              verticalArrangement = androidx.compose.foundation.layout.Arrangement.Top,
              horizontalAlignment = androidx.compose.ui.Alignment.Start,
            ) {}
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_ColumnWithModifiers() {
        val root =
            ComposeNode(
                label = mutableStateOf("Root"),
                trait =
                    mutableStateOf(
                        ColumnTrait(
                            verticalArrangementWrapper = ArrangementVerticalWrapper.Top,
                            horizontalAlignmentWrapper = AlignmentHorizontalWrapper.Start,
                        ),
                    ),
                modifierList = mutableStateListEqualsOverrideOf(ModifierWrapper.FillMaxSize()),
            )
        root.boundsInWindow.value = Rect(left = 0f, top = 0f, right = 200f, bottom = 200f)

        val code = root.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """
            androidx.compose.foundation.layout.Column(
              verticalArrangement = androidx.compose.foundation.layout.Arrangement.Top,
              horizontalAlignment = androidx.compose.ui.Alignment.Start,
              modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.fillMaxSize(),
            ) {}
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_Column_Text() {
        val root =
            ComposeNode(
                label = mutableStateOf("Root"),
                trait =
                    mutableStateOf(
                        ColumnTrait(
                            verticalArrangementWrapper = ArrangementVerticalWrapper.Top,
                            horizontalAlignmentWrapper = AlignmentHorizontalWrapper.Start,
                        ),
                    ),
                modifierList = mutableStateListEqualsOverrideOf(ModifierWrapper.FillMaxSize()),
            ).apply {
                addChild(
                    ComposeNode(
                        trait = mutableStateOf(TextTrait(text = StringProperty.StringIntrinsicValue("test"))),
                    ),
                )
            }
        root.boundsInWindow.value = Rect(left = 0f, top = 0f, right = 200f, bottom = 200f)

        val code = root.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """
                androidx.compose.foundation.layout.Column(
                  verticalArrangement = androidx.compose.foundation.layout.Arrangement.Top,
                  horizontalAlignment = androidx.compose.ui.Alignment.Start,
                  modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.fillMaxSize(),
                ) {
                  androidx.compose.material3.Text(text="test",)
                }
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_RowWithParameters() {
        val root =
            ComposeNode(
                label = mutableStateOf("Root"),
                trait =
                    mutableStateOf(
                        RowTrait(
                            horizontalArrangement = ArrangementHorizontalWrapper.Start,
                            verticalAlignment = AlignmentVerticalWrapper.Top,
                        ),
                    ),
            )
        root.boundsInWindow.value = Rect(left = 0f, top = 0f, right = 200f, bottom = 200f)

        val code = root.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """
            androidx.compose.foundation.layout.Row(
              horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Start,
              verticalAlignment = androidx.compose.ui.Alignment.Top,
            ) {}
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_RowWithModifiers() {
        val root =
            ComposeNode(
                label = mutableStateOf("Root"),
                trait =
                    mutableStateOf(
                        RowTrait(
                            horizontalArrangement = ArrangementHorizontalWrapper.Start,
                            verticalAlignment = AlignmentVerticalWrapper.Top,
                        ),
                    ),
                modifierList = mutableStateListEqualsOverrideOf(ModifierWrapper.FillMaxSize()),
            )
        root.boundsInWindow.value = Rect(left = 0f, top = 0f, right = 200f, bottom = 200f)

        val code = root.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """
         androidx.compose.foundation.layout.Row(
           horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Start,
           verticalAlignment = androidx.compose.ui.Alignment.Top,
           modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.fillMaxSize(),
         ) {}
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_Row_Text() {
        val root =
            ComposeNode(
                label = mutableStateOf("Root"),
                trait =
                    mutableStateOf(
                        RowTrait(
                            horizontalArrangement = ArrangementHorizontalWrapper.Start,
                            verticalAlignment = AlignmentVerticalWrapper.Top,
                        ),
                    ),
                modifierList = mutableStateListEqualsOverrideOf(ModifierWrapper.FillMaxSize()),
            ).apply {
                addChild(
                    ComposeNode(
                        trait = mutableStateOf(TextTrait(text = StringProperty.StringIntrinsicValue("test"))),
                    ),
                )
            }
        root.boundsInWindow.value = Rect(left = 0f, top = 0f, right = 200f, bottom = 200f)

        val code = root.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """
         androidx.compose.foundation.layout.Row(
           horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Start,
           verticalAlignment = androidx.compose.ui.Alignment.Top,
           modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.fillMaxSize(),
         ) {
           androidx.compose.material3.Text(text="test",)
         }
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_LazyColumn_Text() {
        val root =
            ComposeNode(
                label = mutableStateOf("Root"),
                trait =
                    mutableStateOf(
                        LazyColumnTrait(
                            verticalArrangement = ArrangementVerticalWrapper.Bottom,
                            horizontalAlignment = AlignmentHorizontalWrapper.CenterHorizontally,
                        ),
                    ),
                modifierList = mutableStateListEqualsOverrideOf(ModifierWrapper.FillMaxSize()),
            ).apply {
                addChild(
                    ComposeNode(
                        trait = mutableStateOf(TextTrait(text = StringProperty.StringIntrinsicValue("test"))),
                    ),
                )
            }
        root.boundsInWindow.value = Rect(left = 0f, top = 0f, right = 200f, bottom = 200f)

        val code =
            root.generateCode(
                Project(),
                context = GenerationContext(),
                dryRun = false,
            )

        // Note that the number of items is defined in the [PaletteNode#numOfItemsInLazyList]
        assertEquals(
            """
         androidx.compose.foundation.lazy.LazyColumn(
           verticalArrangement = androidx.compose.foundation.layout.Arrangement.Bottom,
           horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
           modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.fillMaxSize(),) {
             items(count = 1) { lazyColumnIndex ->
               androidx.compose.material3.Text(text="test",)
             }
           }
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }
}
