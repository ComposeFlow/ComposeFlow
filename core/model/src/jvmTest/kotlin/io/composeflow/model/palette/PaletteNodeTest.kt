package io.composeflow.model.palette

import io.composeflow.model.parameter.LazyColumnTrait
import io.composeflow.model.parameter.TextTrait
import junit.framework.TestCase.assertFalse
import org.junit.Test
import kotlin.test.assertTrue

class PaletteNodeTest {
    @Test
    fun text_verify_not_having_any_constrains() {
        assertTrue(TextTrait().defaultConstraints().isEmpty())
    }

    @Test
    fun lazyColumn_verify_having_infiniteScroll_constraints() {
        assertTrue(
            Constraint.InfiniteScroll(Orientation.Vertical)
                in LazyColumnTrait().defaultConstraints(),
        )
        assertFalse(
            Constraint.InfiniteScroll(Orientation.Horizontal)
                in LazyColumnTrait().defaultConstraints(),
        )
    }
}
