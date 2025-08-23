package io.composeflow.kotlinpoet.wrapper

/**
 * WASM implementation of TypeSpecWrapper that provides no-op implementations.
 * This is used when code generation is not supported on WASM platform.
 */
actual class TypeSpecWrapper(
    actual val name: String? = null,
    actual val kind: Kind = Kind.CLASS,
    actual val modifiers: Set<KModifierWrapper> = emptySet(),
    actual val annotations: List<AnnotationSpecWrapper> = emptyList(),
    actual val propertySpecs: List<PropertySpecWrapper> = emptyList(),
    actual val funSpecs: List<FunSpecWrapper> = emptyList(),
    actual val typeSpecs: List<TypeSpecWrapper> = emptyList(),
) {
    actual companion object {
        actual fun classBuilder(name: String): TypeSpecBuilderWrapper = TypeSpecBuilderWrapper()

        actual fun classBuilder(className: ClassNameWrapper): TypeSpecBuilderWrapper = TypeSpecBuilderWrapper()

        actual fun objectBuilder(name: String): TypeSpecBuilderWrapper = TypeSpecBuilderWrapper()

        actual fun interfaceBuilder(name: String): TypeSpecBuilderWrapper = TypeSpecBuilderWrapper()

        actual fun enumBuilder(name: String): TypeSpecBuilderWrapper = TypeSpecBuilderWrapper()

        actual fun annotationBuilder(name: String): TypeSpecBuilderWrapper = TypeSpecBuilderWrapper()

        actual fun funInterfaceBuilder(name: String): TypeSpecBuilderWrapper = TypeSpecBuilderWrapper()

        actual fun anonymousClassBuilder(): TypeSpecBuilderWrapper = TypeSpecBuilderWrapper()
    }

    actual enum class Kind {
        CLASS,
        OBJECT,
        INTERFACE,
        ENUM,
        ANNOTATION,
    }

    actual override fun toString(): String = "${kind.name.lowercase()} $name"
}

actual class TypeSpecBuilderWrapper {
    actual fun addModifiers(vararg modifiers: KModifierWrapper): TypeSpecBuilderWrapper = this

    actual fun addAnnotation(annotationSpec: AnnotationSpecWrapper): TypeSpecBuilderWrapper = this

    actual fun addProperty(propertySpec: PropertySpecWrapper): TypeSpecBuilderWrapper = this

    actual fun addFunction(funSpec: FunSpecWrapper): TypeSpecBuilderWrapper = this

    actual fun addType(typeSpec: TypeSpecWrapper): TypeSpecBuilderWrapper = this

    actual fun primaryConstructor(primaryConstructor: FunSpecWrapper): TypeSpecBuilderWrapper = this

    actual fun addSuperinterface(superinterface: TypeNameWrapper): TypeSpecBuilderWrapper = this

    actual fun superclass(superclass: TypeNameWrapper): TypeSpecBuilderWrapper = this

    actual fun addInitializerBlock(block: CodeBlockWrapper): TypeSpecBuilderWrapper = this

    actual fun addEnumConstant(
        name: String,
        typeSpec: TypeSpecWrapper,
    ): TypeSpecBuilderWrapper = this

    actual fun addEnumConstant(name: String): TypeSpecBuilderWrapper = this

    actual fun addSuperclassConstructorParameter(
        format: String,
        vararg args: Any?,
    ): TypeSpecBuilderWrapper = this

    actual fun addSuperclassConstructorParameter(codeBlock: CodeBlockWrapper): TypeSpecBuilderWrapper = this

    actual fun build(): TypeSpecWrapper = TypeSpecWrapper()
}
