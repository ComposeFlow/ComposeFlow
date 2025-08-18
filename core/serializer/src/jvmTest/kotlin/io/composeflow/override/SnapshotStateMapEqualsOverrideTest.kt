package io.composeflow.override

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SnapshotStateMapEqualsOverrideTest {
    @Test
    fun testEquals() {
        val map: MutableMap<String, String> = mutableStateMapEqualsOverrideOf()
        val map2: MutableMap<String, String> = mutableStateMapEqualsOverrideOf()

        map.putAll(mapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"))
        map2.putAll(mapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"))

        assertEquals(map, map2)

        map2.remove("key3")

        assertNotEquals(map, map2)
    }

    @Test
    fun testEqualsEmpty() {
        val map: MutableMap<String, String> = mutableStateMapEqualsOverrideOf()
        val map2: MutableMap<String, String> = mutableStateMapEqualsOverrideOf()

        assertEquals(map, map2)
    }

    @Test
    fun testHashCode() {
        val map: MutableMap<String, String> = mutableStateMapEqualsOverrideOf()
        val map2: MutableMap<String, String> = mutableStateMapEqualsOverrideOf()

        map.putAll(mapOf("key1" to "value1", "key2" to "value2"))
        map2.putAll(mapOf("key1" to "value1", "key2" to "value2"))

        assertEquals(map.hashCode(), map2.hashCode())
    }
}
