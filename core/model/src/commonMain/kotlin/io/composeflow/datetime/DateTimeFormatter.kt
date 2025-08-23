package io.composeflow.datetime

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import io.composeflow.kotlinpoet.MemberHolderWrapper
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.ui.propertyeditor.DropdownItem
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface SeparatorHolder {
    var separator: Separator
}

interface PaddingHolder {
    var padding: PaddingWrapper
}

@Suppress("ktlint:standard:class-naming")
@Serializable
sealed interface DateTimeFormatter : DropdownItem {
    fun asFormatter(): DateTimeFormat<LocalDateTime>

    fun simplifiedFormat(): String

    fun asCodeBlock(): CodeBlockWrapper

    fun newInstance(): DateTimeFormatter

    @Serializable
    @SerialName("YYYY_MM_DD")
    data class YYYY_MM_DD(
        override var separator: Separator = Separator.Slash,
        override var padding: PaddingWrapper = PaddingWrapper.Zero,
    ) : DateTimeFormatter,
        PaddingHolder,
        SeparatorHolder {
        override fun asFormatter(): DateTimeFormat<LocalDateTime> =
            LocalDateTime.Format {
                year(padding.padding)
                chars(separator.char)
                monthNumber(padding.padding)
                chars(separator.char)
                dayOfMonth(padding.padding)
            }

        override fun asCodeBlock(): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """
            %M.Format {
                year(${padding.asCodeBlock()})
                chars("${separator.char}")
                monthNumber(${padding.asCodeBlock()})
                chars("${separator.char}")
                dayOfMonth(${padding.asCodeBlock()})
            }
            """,
                MemberHolderWrapper.DateTime.LocalDateTime,
            )

        override fun simplifiedFormat(): String = "YYYY${separator}MM${separator}DD"

        @Composable
        override fun asDropdownText(): AnnotatedString =
            AnnotatedString("YYYY${separator}MM${separator}DD (2024${separator}${padding.asString()}8${separator}${padding.asString()}9)")

        override fun isSameItem(item: Any): Boolean = item is YYYY_MM_DD

        override fun newInstance(): DateTimeFormatter = YYYY_MM_DD()
    }

    @Serializable
    @SerialName("YYYY_MM_DD_HH_MM")
    data class YYYY_MM_DD_HH_MM(
        override var separator: Separator = Separator.Slash,
        override var padding: PaddingWrapper = PaddingWrapper.Zero,
    ) : DateTimeFormatter,
        PaddingHolder,
        SeparatorHolder {
        override fun asFormatter(): DateTimeFormat<LocalDateTime> =
            LocalDateTime.Format {
                year(padding.padding)
                chars(separator.char)
                monthNumber(padding.padding)
                chars(separator.char)
                dayOfMonth(padding.padding)
                chars(" ")
                hour()
                chars(":")
                minute()
            }

        override fun asCodeBlock(): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """
            %M.Format {
                year(${padding.asCodeBlock()})
                chars("${separator.char}")
                monthNumber(${padding.asCodeBlock()})
                chars("${separator.char}")
                dayOfMonth(${padding.asCodeBlock()})
                chars(" ")
                hour()
                chars(":")
                minute()
            }
            """,
                MemberHolderWrapper.DateTime.LocalDateTime,
            )

        override fun simplifiedFormat(): String = "YYYY${separator}MM${separator}DD HH:MM"

        @Composable
        override fun asDropdownText(): AnnotatedString =
            AnnotatedString(
                "YYYY${separator}MM${separator}DD HH:MM (2024${separator}${padding.asString()}8${separator}${padding.asString()}9 10:05)",
            )

        override fun isSameItem(item: Any): Boolean = item is YYYY_MM_DD_HH_MM

        override fun newInstance(): DateTimeFormatter = YYYY_MM_DD_HH_MM()
    }

    @Serializable
    @SerialName("YYYY_MM")
    data class YYYY_MM(
        override var separator: Separator = Separator.Slash,
        override var padding: PaddingWrapper = PaddingWrapper.Zero,
    ) : DateTimeFormatter,
        PaddingHolder,
        SeparatorHolder {
        override fun asFormatter(): DateTimeFormat<LocalDateTime> =
            LocalDateTime.Format {
                year(padding.padding)
                chars(separator.char)
                monthNumber(padding.padding)
            }

        override fun asCodeBlock(): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """
            %M.Format {
                year(${padding.asCodeBlock()})
                chars("${separator.char}")
                monthNumber(${padding.asCodeBlock()})
            }
            """,
                MemberHolderWrapper.DateTime.LocalDateTime,
            )

        override fun simplifiedFormat(): String = "YYYY${separator}MM$"

        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("YYYY${separator}MM (2024${separator}${padding.asString()}8)")

        override fun isSameItem(item: Any): Boolean = item is YYYY_MM

        override fun newInstance(): DateTimeFormatter = YYYY_MM()
    }

    @Serializable
    @SerialName("MM_DD")
    data class MM_DD(
        override var separator: Separator = Separator.Slash,
        override var padding: PaddingWrapper = PaddingWrapper.Zero,
    ) : DateTimeFormatter,
        PaddingHolder,
        SeparatorHolder {
        override fun asFormatter(): DateTimeFormat<LocalDateTime> =
            LocalDateTime.Format {
                monthNumber(padding.padding)
                chars(separator.char)
                dayOfMonth(padding.padding)
            }

        override fun asCodeBlock(): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """
            %M.Format {
                monthNumber(${padding.asCodeBlock()})
                chars("${separator.char}")
                dayOfMonth(${padding.asCodeBlock()})
            }
            """,
                MemberHolderWrapper.DateTime.LocalDateTime,
            )

        override fun simplifiedFormat(): String = "MM${separator}DD"

        @Composable
        override fun asDropdownText(): AnnotatedString =
            AnnotatedString("MM${separator}DD (${padding.asString()}8${separator}${padding.asString()}9)")

        override fun isSameItem(item: Any): Boolean = item is MM_DD

        override fun newInstance(): DateTimeFormatter = MM_DD()
    }

    @Serializable
    @SerialName("DD_MM_YYYY")
    data class DD_MM_YYYY(
        override var separator: Separator = Separator.Slash,
        override var padding: PaddingWrapper = PaddingWrapper.Zero,
    ) : DateTimeFormatter,
        SeparatorHolder,
        PaddingHolder {
        override fun asFormatter(): DateTimeFormat<LocalDateTime> =
            LocalDateTime.Format {
                dayOfMonth(padding.padding)
                chars(separator.char)
                monthNumber(padding.padding)
                chars(separator.char)
                year(padding.padding)
            }

        override fun asCodeBlock(): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """
            %M.Format {
                dayOfMonth(${padding.asCodeBlock()})
                chars("${separator.char}")
                monthNumber(${padding.asCodeBlock()})
                chars("${separator.char}")
                year(${padding.asCodeBlock()})
            }
            """,
                MemberHolderWrapper.DateTime.LocalDateTime,
            )

        override fun simplifiedFormat(): String = "DD${separator}MM${separator}YYYY"

        @Composable
        override fun asDropdownText(): AnnotatedString =
            AnnotatedString("DD${separator}MM${separator}YYYY (${padding.asString()}9${separator}${padding.asString()}8${separator}2024)")

        override fun isSameItem(item: Any): Boolean = item is DD_MM_YYYY

        override fun newInstance(): DateTimeFormatter = DD_MM_YYYY()
    }

    @Serializable
    @SerialName("MMM_DD_YYYY")
    data class MMM_DD_YYYY(
        override var padding: PaddingWrapper = PaddingWrapper.None,
    ) : DateTimeFormatter,
        PaddingHolder {
        override fun asFormatter(): DateTimeFormat<LocalDateTime> =
            LocalDateTime.Format {
                monthName(MonthNames.ENGLISH_ABBREVIATED)
                chars(" ")
                dayOfMonth(padding.padding)
                chars(", ")
                year(padding.padding)
            }

        override fun asCodeBlock(): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """
            %M.Format {
                monthName(%M.ENGLISH_ABBREVIATED)
                chars(" ")
                dayOfMonth(${padding.asCodeBlock()})
                chars(", ")
                year(${padding.asCodeBlock()})
            }
            """,
                MemberHolderWrapper.DateTime.LocalDateTime,
                MemberHolderWrapper.DateTime.MonthNames,
            )

        override fun simplifiedFormat(): String = "MMM DD, YYYY"

        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("MMM DD, YYYY (Aug ${padding.asString()}9, 2024)")

        override fun isSameItem(item: Any): Boolean = item is MMM_DD_YYYY

        override fun newInstance(): DateTimeFormatter = MMM_DD_YYYY()
    }

    @Serializable
    @SerialName("MMM_DD")
    data class MMM_DD(
        override var padding: PaddingWrapper = PaddingWrapper.None,
    ) : DateTimeFormatter,
        PaddingHolder {
        override fun asFormatter(): DateTimeFormat<LocalDateTime> =
            LocalDateTime.Format {
                monthName(MonthNames.ENGLISH_ABBREVIATED)
                chars(" ")
                dayOfMonth(padding.padding)
            }

        override fun asCodeBlock(): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """
            %M.Format {
                monthName(%M.ENGLISH_ABBREVIATED)
                chars(" ")
                dayOfMonth(${padding.asCodeBlock()})
            }
            """,
                MemberHolderWrapper.DateTime.LocalDateTime,
                MemberHolderWrapper.DateTime.MonthNames,
            )

        override fun simplifiedFormat(): String = "MMM DD"

        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("MMM DD (Aug ${padding.asString()}9)")

        override fun isSameItem(item: Any): Boolean = item is MMM_DD

        override fun newInstance(): DateTimeFormatter = MMM_DD()
    }

    @Serializable
    @SerialName("YYYY")
    data class YYYY(
        override var padding: PaddingWrapper = PaddingWrapper.Zero,
    ) : DateTimeFormatter,
        PaddingHolder {
        override fun asFormatter(): DateTimeFormat<LocalDateTime> =
            LocalDateTime.Format {
                year(padding.padding)
            }

        override fun asCodeBlock(): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """
            %M.Format {
                year(${padding.asCodeBlock()})
            }
            """,
                MemberHolderWrapper.DateTime.LocalDateTime,
            )

        override fun simplifiedFormat(): String = "YYYY"

        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("YYYY (2024)")

        override fun isSameItem(item: Any): Boolean = item is YYYY

        override fun newInstance(): DateTimeFormatter = YYYY()
    }

    @Serializable
    @SerialName("MM")
    data class MM(
        override var padding: PaddingWrapper = PaddingWrapper.Zero,
    ) : DateTimeFormatter,
        PaddingHolder {
        override fun asFormatter(): DateTimeFormat<LocalDateTime> =
            LocalDateTime.Format {
                monthNumber(padding.padding)
            }

        override fun asCodeBlock(): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """
            %M.Format {
                dayOfMonth(${padding.asCodeBlock()})
            }
            """,
                MemberHolderWrapper.DateTime.LocalDateTime,
            )

        override fun simplifiedFormat(): String = "MM"

        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("MM (${padding.asString()}8)")

        override fun isSameItem(item: Any): Boolean = item is MM

        override fun newInstance(): DateTimeFormatter = MM()
    }

    @Serializable
    @SerialName("MMM")
    data object MMM : DateTimeFormatter {
        override fun asFormatter(): DateTimeFormat<LocalDateTime> =
            LocalDateTime.Format {
                monthName(MonthNames.ENGLISH_ABBREVIATED)
            }

        override fun asCodeBlock(): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """
            %M.Format {
                monthName(%M.ENGLISH_ABBREVIATED)
            }
            """,
                MemberHolderWrapper.DateTime.LocalDateTime,
                MemberHolderWrapper.DateTime.MonthNames,
            )

        override fun simplifiedFormat(): String = "MMM"

        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("MMM (Aug)")

        override fun isSameItem(item: Any): Boolean = item is MMM

        override fun newInstance(): DateTimeFormatter = MMM
    }

    @Serializable
    @SerialName("DD")
    data class DD(
        override var padding: PaddingWrapper = PaddingWrapper.Zero,
    ) : DateTimeFormatter,
        PaddingHolder {
        override fun asFormatter(): DateTimeFormat<LocalDateTime> =
            LocalDateTime.Format {
                dayOfMonth(padding.padding)
            }

        override fun asCodeBlock(): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """
            %M.Format {
                dayOfMonth(${padding.asCodeBlock()})
            }
            """,
                MemberHolderWrapper.DateTime.LocalDateTime,
            )

        override fun simplifiedFormat(): String = "DD"

        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("DD (${padding.asString()}9)")

        override fun isSameItem(item: Any): Boolean = item is DD

        override fun newInstance(): DateTimeFormatter = DD()
    }

    @Serializable
    @SerialName("HH_MM")
    data class HH_MM(
        override var padding: PaddingWrapper = PaddingWrapper.Zero,
    ) : DateTimeFormatter,
        PaddingHolder {
        override fun asFormatter(): DateTimeFormat<LocalDateTime> =
            LocalDateTime.Format {
                hour(padding.padding)
                chars(":")
                minute(padding.padding)
            }

        override fun asCodeBlock(): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """
            %M.Format {
                hour(${padding.asCodeBlock()})
                chars(":")
                minute(${padding.asCodeBlock()})
            }
            """,
                MemberHolderWrapper.DateTime.LocalDateTime,
            )

        override fun simplifiedFormat(): String = "HH:MM"

        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("HH:MM")

        override fun isSameItem(item: Any): Boolean = item is HH_MM

        override fun newInstance(): DateTimeFormatter = HH_MM()
    }

    companion object {
        fun entries(): List<DateTimeFormatter> =
            listOf(
                YYYY_MM_DD(),
                YYYY_MM_DD_HH_MM(),
                YYYY_MM(),
                MM_DD(),
                DD_MM_YYYY(),
                MMM_DD_YYYY(),
                MMM_DD(),
                YYYY(),
                MM(),
                MMM,
                DD(),
                HH_MM(),
            )
    }
}

enum class Separator(
    val char: String,
) {
    Slash("/"),
    Dash("-"),
    Space(" "),
    ;

    override fun toString(): String = char
}

enum class PaddingWrapper(
    val padding: Padding,
) {
    Zero(Padding.ZERO),
    None(Padding.NONE),
    Space(Padding.SPACE),
    ;

    fun asString(): String = padding.asString()

    fun asCodeBlock(): CodeBlockWrapper = padding.asCodeBlock()
}

fun Padding.asString(): String =
    when (this) {
        Padding.ZERO -> "0"
        Padding.NONE -> ""
        Padding.SPACE -> " "
    }

fun Padding.asCodeBlock(): CodeBlockWrapper =
    when (this) {
        Padding.ZERO -> CodeBlockWrapper.of("%M.ZERO", MemberHolderWrapper.DateTime.Padding)
        Padding.NONE -> CodeBlockWrapper.of("%M.NONE", MemberHolderWrapper.DateTime.Padding)
        Padding.SPACE -> CodeBlockWrapper.of("%M.SPACE", MemberHolderWrapper.DateTime.Padding)
    }
