package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.UNIT as KotlinPoetUNIT

/**
 * JVM implementation of UNIT wrapper that delegates to actual KotlinPoet's UNIT.
 */
actual val UNIT: TypeNameWrapper = KotlinPoetUNIT.toWrapper()
