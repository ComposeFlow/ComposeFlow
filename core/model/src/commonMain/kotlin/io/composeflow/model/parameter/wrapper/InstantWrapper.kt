@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.composeflow.model.parameter.wrapper

import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.kotlinpoet.wrapper.asTypeNameWrapper
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class InstantWrapper(
    @Serializable(LocationAwareShapeWrapperSerializer::class)
    val instant: Instant? = null,
) {
    @Transient
    private val dateFormat =
        DateTimeComponents.Format {
            date(LocalDate.Formats.ISO)
        }

    fun generateCode(): CodeBlockWrapper =
        if (instant == null) {
            CodeBlockWrapper.of("%T.System.now()", Clock::class.asTypeNameWrapper())
        } else {
            CodeBlockWrapper.of(
                "%T.parse(\"${asString()}\").%M(%T.UTC)",
                LocalDate::class.asTypeNameWrapper(),
                MemberNameWrapper.get("kotlinx.datetime", "atStartOfDayIn", isExtension = true),
                TimeZone::class.asTypeNameWrapper(),
            )
        }

    fun asString(): String {
        // TODO: Other candidates
        return instant?.let {
            val kotlinxInstant =
                kotlinx.datetime.Instant.fromEpochMilliseconds(it.toEpochMilliseconds())
            kotlinxInstant.format(dateFormat)
        } ?: ""
    }
}
