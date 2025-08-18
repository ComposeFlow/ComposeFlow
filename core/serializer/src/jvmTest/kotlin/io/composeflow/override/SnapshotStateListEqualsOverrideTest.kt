package io.composeflow.override

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SnapshotStateListEqualsOverrideTest {
    @Test
    fun testEquals() {
        val list: MutableList<String> = mutableStateListEqualsOverrideOf()
        val list2: MutableList<String> = mutableStateListEqualsOverrideOf()

        list.addAll(listOf("a", "b", "c"))
        list2.addAll(listOf("a", "b", "c"))

        assertEquals(list, list2)

        list2.removeLast()

        assertNotEquals(list, list2)
    }

    @Test
    fun testEqualsEmpty() {
        val list: MutableList<String> = mutableStateListEqualsOverrideOf()
        val list2: MutableList<String> = mutableStateListEqualsOverrideOf()

        assertEquals(list, list2)
    }

    @Test
    fun testHashCode() {
        val list: MutableList<String> = mutableStateListEqualsOverrideOf()
        val list2: MutableList<String> = mutableStateListEqualsOverrideOf()

        list.addAll(listOf("a", "b"))
        list2.addAll(listOf("a", "b"))

        assertEquals(list.hashCode(), list2.hashCode())
    }
}
