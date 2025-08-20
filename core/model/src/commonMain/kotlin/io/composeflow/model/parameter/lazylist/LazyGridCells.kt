package io.composeflow.model.parameter.lazylist
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.ClassHolder
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.serializer.LocationAwareDpSerializer
import io.composeflow.ui.propertyeditor.DropdownItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("LazyGridCells")
sealed interface LazyGridCells : DropdownItem {
    fun asCodeBlock(): CodeBlockWrapper

    fun asComposeGridCells(): GridCells

    @Serializable
    @SerialName("Adaptive")
    data class Adaptive(
        @Serializable(LocationAwareDpSerializer::class)
        val minSize: Dp = 100.dp,
    ) : LazyGridCells {
        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("Adaptive")

        override fun isSameItem(item: Any): Boolean = item is Adaptive

        override fun asCodeBlock(): CodeBlockWrapper =
            CodeBlockWrapper.of(
                "%T.Adaptive(${minSize.value.toInt()}.%M)",
                ClassHolder.AndroidX.Lazy.GridCells,
                MemberHolder.AndroidX.Ui.dp,
            )

        override fun asComposeGridCells(): GridCells = GridCells.Adaptive(minSize)
    }

    @Serializable
    @SerialName("Fixed")
    data class Fixed(
        val count: Int = 2,
    ) : LazyGridCells {
        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("Fixed")

        override fun isSameItem(item: Any): Boolean = item is Fixed

        override fun asCodeBlock(): CodeBlockWrapper =
            CodeBlockWrapper.of(
                "%T.Fixed($count)",
                ClassHolder.AndroidX.Lazy.GridCells,
            )

        override fun asComposeGridCells(): GridCells = GridCells.Fixed(count)
    }

    @Serializable
    @SerialName("FixedSize")
    data class FixedSize(
        @Serializable(LocationAwareDpSerializer::class)
        val size: Dp = 100.dp,
    ) : LazyGridCells {
        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("FixedSize")

        override fun isSameItem(item: Any): Boolean = item is FixedSize

        override fun asCodeBlock(): CodeBlockWrapper =
            CodeBlockWrapper.of(
                "%T.FixedSize(${size.value.toInt()}.%M)",
                ClassHolder.AndroidX.Lazy.GridCells,
                MemberHolder.AndroidX.Ui.dp,
            )

        override fun asComposeGridCells(): GridCells = GridCells.FixedSize(size)
    }

    companion object {
        fun entries(): List<LazyGridCells> =
            listOf(
                Adaptive(),
                Fixed(),
                FixedSize(),
            )
    }
}
