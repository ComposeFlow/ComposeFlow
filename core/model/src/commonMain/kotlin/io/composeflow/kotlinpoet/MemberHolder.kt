package io.composeflow.kotlinpoet

import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.model.project.COMPOSEFLOW_PACKAGE

/**
 * Utility objects that hold MemberName for KotlinPoet to avoid same MemberName are repeatedly
 * defined.
 *
 *
 */
@Deprecated(
    """Use MemberHolderWrapper instead if possible for supporting Multiplatform.
        This object will be removed in the future.""",
)
object MemberHolder {
    object AndroidX {
        object Foundation {
            val Image = MemberNameWrapper.get("androidx.compose.foundation", "Image")
            val background =
                MemberNameWrapper.get(
                    "androidx.compose.foundation",
                    "background",
                    isExtension = true,
                )
        }

        object Lazy {
            val items = MemberNameWrapper.get("androidx.compose.foundation.lazy", "items")
            val itemsIndexed =
                MemberNameWrapper.get("androidx.compose.foundation.lazy", "itemsIndexed")
            val gridItemsIndexed =
                MemberNameWrapper.get("androidx.compose.foundation.lazy.grid", "itemsIndexed")
        }

        object Ui {
            val dp = MemberNameWrapper.get("androidx.compose.ui.unit", "dp", isExtension = true)
            val sp = MemberNameWrapper.get("androidx.compose.ui.unit", "sp", isExtension = true)
            val Alignment = MemberNameWrapper.get("androidx.compose.ui", "Alignment")
            val Color = MemberNameWrapper.get("androidx.compose.ui.graphics", "Color")
            val Dialog = MemberNameWrapper.get("androidx.compose.ui.window", "Dialog")
            val FontWeight = MemberNameWrapper.get("androidx.compose.ui.text.font", "FontWeight")
            val Modifier = MemberNameWrapper.get("androidx.compose.ui", "Modifier")
        }

        object Layout {
            val Arrangement =
                MemberNameWrapper.get("androidx.compose.foundation.layout", "Arrangement")
            val Column = MemberNameWrapper.get("androidx.compose.foundation.layout", "Column")
            val Row = MemberNameWrapper.get("androidx.compose.foundation.layout", "Row")
            val Box = MemberNameWrapper.get("androidx.compose.foundation.layout", "Box")
            val Spacer = MemberNameWrapper.get("androidx.compose.foundation.layout", "Spacer")
            val align =
                MemberNameWrapper.get(
                    "androidx.compose.foundation.layout",
                    "align",
                    isExtension = true,
                )
            val padding = MemberNameWrapper.get("androidx.compose.foundation.layout", "padding")
            val size =
                MemberNameWrapper.get(
                    "androidx.compose.foundation.layout",
                    "size",
                    isExtension = true,
                )
            val width =
                MemberNameWrapper.get(
                    "androidx.compose.foundation.layout",
                    "width",
                    isExtension = true,
                )
        }

        object Platform {
            val LocalUriHandler =
                MemberNameWrapper.get("androidx.compose.ui.platform", "LocalUriHandler")
        }

        object Runtime {
            val collectAsState = MemberNameWrapper.get("androidx.compose.runtime", "collectAsState")
            val LaunchedEffect = MemberNameWrapper.get("androidx.compose.runtime", "LaunchedEffect")
            val mutableStateOf = MemberNameWrapper.get("androidx.compose.runtime", "mutableStateOf")
            val remember = MemberNameWrapper.get("androidx.compose.runtime", "remember")
            val rememberSaveable =
                MemberNameWrapper.get("androidx.compose.runtime.saveable", "rememberSaveable")
            val rememberCoroutineScope =
                MemberNameWrapper.get("androidx.compose.runtime", "rememberCoroutineScope")
        }
    }

    object AppCash {
        val collectAsLazyPagingItems =
            MemberNameWrapper.get("app.cash.paging.compose", "collectAsLazyPagingItems")
    }

    object ComposeFlow {
        val asErrorMessage =
            MemberNameWrapper.get(
                "${COMPOSEFLOW_PACKAGE}.validator",
                "asErrorMessage",
                isExtension = true,
            )
        val isSuccess =
            MemberNameWrapper.get(
                "${COMPOSEFLOW_PACKAGE}.validator",
                "isSuccess",
                isExtension = true,
            )
        val ValidateResult =
            MemberNameWrapper.get("${COMPOSEFLOW_PACKAGE}.validator", "ValidateResult")
        val LocalAuthenticatedUser =
            MemberNameWrapper.get("${COMPOSEFLOW_PACKAGE}.auth", "LocalAuthenticatedUser")
        val LocalOnShowsnackbar =
            MemberNameWrapper.get("${COMPOSEFLOW_PACKAGE}.ui", "LocalOnShowSnackbar")
        val Res = MemberNameWrapper.get(COMPOSEFLOW_PACKAGE, "Res")

        object String {
            val cancel = MemberNameWrapper.get(COMPOSEFLOW_PACKAGE, "cancel", isExtension = true)
            val confirm = MemberNameWrapper.get(COMPOSEFLOW_PACKAGE, "confirm", isExtension = true)
        }
    }

    object Coroutines {
        val launch = MemberNameWrapper.get("kotlinx.coroutines", "launch")

        object Flow {
            val collectLatest = MemberNameWrapper.get("kotlinx.coroutines.flow", "collectLatest")
            val first = MemberNameWrapper.get("kotlinx.coroutines.flow", "first")
            val map = MemberNameWrapper.get("kotlinx.coroutines.flow", "map")
            val mapLatest = MemberNameWrapper.get("kotlinx.coroutines.flow", "mapLatest")
            val stateIn = MemberNameWrapper.get("kotlinx.coroutines.flow", "stateIn")
            val SharingStarted = MemberNameWrapper.get("kotlinx.coroutines.flow", "SharingStarted")
        }
    }

    object DateTime {
        val format = MemberNameWrapper.get("kotlinx.datetime", "format", isExtension = true)
        val plus = MemberNameWrapper.get("kotlinx.datetime", "plus", isExtension = true)
        val toLocalDateTime =
            MemberNameWrapper.get("kotlinx.datetime", "toLocalDateTime", isExtension = true)
        val DateTimeUnit = MemberNameWrapper.get("kotlinx.datetime", "DateTimeUnit")
        val Instant = MemberNameWrapper.get("kotlinx.datetime", "Instant")
        val LocalDateTime = MemberNameWrapper.get("kotlinx.datetime", "LocalDateTime")
        val MonthNames = MemberNameWrapper.get("kotlinx.datetime.format", "MonthNames")
        val Padding = MemberNameWrapper.get("kotlinx.datetime.format", "Padding")
        val TimeZone = MemberNameWrapper.get("kotlinx.datetime", "TimeZone")
    }

    object JetBrains {
        val imageResource =
            MemberNameWrapper.get("org.jetbrains.compose.resources", "imageResource")
        val stringResource =
            MemberNameWrapper.get("org.jetbrains.compose.resources", "stringResource")
        val vectorResource =
            MemberNameWrapper.get("org.jetbrains.compose.resources", "vectorResource")
    }

    object Koin {
        val inject = MemberNameWrapper.get("org.koin.core.component", "inject")
        val get = MemberNameWrapper.get("org.koin.core.component", "get")
    }

    object Kotlin {
        object Collection {
            val sorted = MemberNameWrapper.get("kotlin.collections", "sorted", isExtension = true)
            val sortedBy =
                MemberNameWrapper.get("kotlin.collections", "sortedBy", isExtension = true)
            val isNotEmpty =
                MemberNameWrapper.get("kotlin.collections", "isNotEmpty", isExtension = true)
            val toMutableList =
                MemberNameWrapper.get("kotlin.collections", "toMutableList", isExtension = true)
        }
    }

    object Firebase {
        val Firebase = MemberNameWrapper.get("dev.gitlive.firebase", "Firebase")
        val auth = MemberNameWrapper.get("dev.gitlive.firebase.auth", "auth")
    }

    object Material3 {
        val Card = MemberNameWrapper.get("androidx.compose.material3", "Card")
        val CircularProgressIndicator =
            MemberNameWrapper.get("androidx.compose.material3", "CircularProgressIndicator")
        val DatePicker = MemberNameWrapper.get("androidx.compose.material3", "DatePicker")
        val DatePickerDialog =
            MemberNameWrapper.get("androidx.compose.material3", "DatePickerDialog")
        val MaterialTheme = MemberNameWrapper.get("androidx.compose.material3", "MaterialTheme")
        val OutlinedButton = MemberNameWrapper.get("androidx.compose.material3", "OutlinedButton")
        val rememberDatePickerState =
            MemberNameWrapper.get("androidx.compose.material3", "rememberDatePickerState")
        val rememberTimePickerState =
            MemberNameWrapper.get("androidx.compose.material3", "rememberTimePickerState")
        val Icon = MemberNameWrapper.get("androidx.compose.material3", "Icon")
        val SelectableDates = MemberNameWrapper.get("androidx.compose.material3", "SelectableDates")
        val Text = MemberNameWrapper.get("androidx.compose.material3", "Text")
        val TextButton = MemberNameWrapper.get("androidx.compose.material3", "TextButton")
        val TimePicker = MemberNameWrapper.get("androidx.compose.material3", "TimePicker")
        val Typography = MemberNameWrapper.get("androidx.compose.material3", "Typography")
    }

    object PreCompose {
        // Using collectAsState doesn't make the js app work probably because the expected lifecycle events aren't sent as in mobile
        // Use AndroidX.Runtime.collectAsState instead
//        val collectAsStateWithLifeCycle = MemberNameWrapper.get("moe.tlaster.precompose.flow", "collectAsStateWithLifecycle")
        val viewModelScope =
            MemberNameWrapper.get("moe.tlaster.precompose.viewmodel", "viewModelScope")
        val koinViewModel = MemberNameWrapper.get("moe.tlaster.precompose.koin", "koinViewModel")
    }

    object Serialization {
        val encodeToString = MemberNameWrapper.get("kotlinx.serialization", "encodeToString")
    }

    object Settings {
        val getBooleanFlow =
            MemberNameWrapper.get("com.russhwolf.settings.coroutines", "getBooleanFlow")
        val getFloatFlow =
            MemberNameWrapper.get("com.russhwolf.settings.coroutines", "getFloatFlow")
        val getIntFlow = MemberNameWrapper.get("com.russhwolf.settings.coroutines", "getIntFlow")
        val getLongFlow = MemberNameWrapper.get("com.russhwolf.settings.coroutines", "getLongFlow")
        val getStringFlow =
            MemberNameWrapper.get("com.russhwolf.settings.coroutines", "getStringFlow")
    }
}
