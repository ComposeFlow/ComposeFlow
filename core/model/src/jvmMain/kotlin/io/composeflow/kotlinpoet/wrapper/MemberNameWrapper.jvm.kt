package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.MemberName

/**
 * JVM implementation of MemberNameWrapper that delegates to actual KotlinPoet's MemberName.
 */
actual class MemberNameWrapper internal constructor(private val actual: MemberName) {
    actual companion object {
        actual fun get(packageName: String, simpleName: String, isExtension: Boolean): MemberNameWrapper = 
            MemberNameWrapper(MemberName(packageName, simpleName, isExtension))
    }
    
    actual val packageName: String get() = actual.packageName
    actual val simpleName: String get() = actual.simpleName
    actual val isExtension: Boolean get() = actual.isExtension
    
    actual override fun toString(): String = actual.toString()
    
    // Internal accessor for other wrapper classes
    internal fun toKotlinPoet(): MemberName = actual
}

// Helper function to create MemberNameWrapper from KotlinPoet MemberName
actual fun MemberName.toWrapper(): MemberNameWrapper = MemberNameWrapper(this)