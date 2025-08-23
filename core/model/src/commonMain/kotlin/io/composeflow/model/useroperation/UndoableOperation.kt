package io.composeflow.model.useroperation

import kotlin.time.Clock
import kotlin.time.Instant

data class UndoableOperation(
    val serializedProject: String,
    val userOperation: UserOperation,
    val date: Instant = Clock.System.now(),
)
