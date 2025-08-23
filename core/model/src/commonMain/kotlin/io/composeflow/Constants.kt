package io.composeflow

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import io.composeflow.kotlinpoet.MemberHolderWrapper
import io.composeflow.kotlinpoet.wrapper.ClassNameWrapper
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.KModifierWrapper
import io.composeflow.kotlinpoet.wrapper.PropertySpecWrapper
import io.composeflow.kotlinpoet.wrapper.asTypeNameWrapper
import kotlinx.serialization.json.Json

const val ON_SCREEN_INITIALLY_LOADED = "onScreenInitiallyLoaded"
const val SCREEN_INITIALLY_LOADED_FLAG = "screenInitiallyLoadedFlag"

@Suppress("EnumEntryName")
enum class ComposeScreenConstant {
    arguments,
    onNavigateToRoute,
    onNavigateBack,
    coroutineScope,
    navDrawerState,
}

@Suppress("EnumEntryName")
enum class ViewModelConstant {
    flowSettings {
        @OptIn(ExperimentalSettingsApi::class)
        override fun generateProperty(): PropertySpecWrapper =
            PropertySpecWrapper
                .builder(flowSettings.name, FlowSettings::class.asTypeNameWrapper())
                .addModifiers(KModifierWrapper.PRIVATE)
                .delegate(
                    CodeBlockWrapper
                        .builder()
                        .add("lazy { ")
                        .add("%M()", MemberHolderWrapper.Koin.get)
                        .add(" }")
                        .build(),
                ).build()
    },
    jsonSerializer {
        override fun generateProperty(): PropertySpecWrapper =
            PropertySpecWrapper
                .builder(jsonSerializer.name, Json::class.asTypeNameWrapper())
                .addModifiers(KModifierWrapper.PRIVATE)
                .delegate(
                    CodeBlockWrapper
                        .builder()
                        .add("%M()", MemberHolderWrapper.Koin.inject)
                        .build(),
                ).build()
    },
    firestore {
        override fun generateProperty(): PropertySpecWrapper =
            PropertySpecWrapper
                .builder(firestore.name, ClassNameWrapper.get("dev.gitlive.firebase.firestore", "FirebaseFirestore"))
                .addModifiers(KModifierWrapper.PRIVATE)
                .delegate(
                    CodeBlockWrapper
                        .builder()
                        .add("%M()", MemberHolderWrapper.Koin.inject)
                        .build(),
                ).build()
    },
    ;

    abstract fun generateProperty(): PropertySpecWrapper
}
