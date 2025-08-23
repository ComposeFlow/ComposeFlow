package io.composeflow.model.project.component

import io.composeflow.kotlinpoet.FileSpecWithDirectory
import io.composeflow.kotlinpoet.wrapper.ClassNameWrapper
import io.composeflow.kotlinpoet.wrapper.FileSpecWrapper
import io.composeflow.kotlinpoet.wrapper.FunSpecWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.kotlinpoet.wrapper.asTypeNameWrapper
import io.composeflow.kotlinpoet.wrapper.suppressRedundantVisibilityModifier
import io.composeflow.model.project.COMPOSEFLOW_PACKAGE
import io.composeflow.model.project.Project
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.serializer.FallbackMutableStateListSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ComponentHolder")
data class ComponentHolder(
    @Serializable(with = FallbackMutableStateListSerializer::class)
    val components: MutableList<Component> = mutableStateListEqualsOverrideOf(),
) {
    fun generateKoinViewModelModule(project: Project): FileSpecWithDirectory {
        val fileBuilder =
            FileSpecWrapper.builder(
                fileName = "ViewModelModule",
                packageName = "${COMPOSEFLOW_PACKAGE}.components",
            )
        val funSpecBuilder =
            FunSpecWrapper
                .builder("componentViewModelModule")
                .returns(org.koin.core.module.Module::class.asTypeNameWrapper())
                .addCode("return %M {", MemberNameWrapper.get("org.koin.dsl", "module"))
        components.forEach {
            funSpecBuilder.addStatement(
                "factory { %T() } ",
                ClassNameWrapper.get(it.getPackageName(project), it.viewModelFileName),
            )
        }
        funSpecBuilder.addCode("}")
        fileBuilder.addFunction(funSpecBuilder.build())
        fileBuilder.suppressRedundantVisibilityModifier()
        return FileSpecWithDirectory(fileBuilder.build())
    }
}
