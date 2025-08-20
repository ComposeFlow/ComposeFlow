package io.composeflow.kotlinpoet.wrapper

/**
 * Multiplatform wrapper for KotlinPoet's TypeSpec.
 * On JVM, delegates to actual TypeSpec. On WASM, provides no-op implementation.
 */
expect class TypeSpecWrapper {
    companion object {
        fun classBuilder(name: String): TypeSpecBuilderWrapper
        fun classBuilder(className: ClassNameWrapper): TypeSpecBuilderWrapper
        fun objectBuilder(name: String): TypeSpecBuilderWrapper
        fun interfaceBuilder(name: String): TypeSpecBuilderWrapper
        fun enumBuilder(name: String): TypeSpecBuilderWrapper
        fun annotationBuilder(name: String): TypeSpecBuilderWrapper
        fun funInterfaceBuilder(name: String): TypeSpecBuilderWrapper
    }
    
    val name: String?
    val kind: Kind
    val modifiers: Set<KModifierWrapper>
    val annotations: List<AnnotationSpecWrapper>
    val propertySpecs: List<PropertySpecWrapper>
    val funSpecs: List<FunSpecWrapper>
    val typeSpecs: List<TypeSpecWrapper>
    
    enum class Kind {
        CLASS,
        OBJECT,
        INTERFACE,
        ENUM,
        ANNOTATION
    }
    
    override fun toString(): String
}

expect class TypeSpecBuilderWrapper {
    fun addModifiers(vararg modifiers: KModifierWrapper): TypeSpecBuilderWrapper
    fun addAnnotation(annotationSpec: AnnotationSpecWrapper): TypeSpecBuilderWrapper
    fun addProperty(propertySpec: PropertySpecWrapper): TypeSpecBuilderWrapper
    fun addFunction(funSpec: FunSpecWrapper): TypeSpecBuilderWrapper
    fun addType(typeSpec: TypeSpecWrapper): TypeSpecBuilderWrapper
    fun primaryConstructor(primaryConstructor: FunSpecWrapper): TypeSpecBuilderWrapper
    fun addSuperinterface(superinterface: TypeNameWrapper): TypeSpecBuilderWrapper
    fun superclass(superclass: TypeNameWrapper): TypeSpecBuilderWrapper
    fun build(): TypeSpecWrapper
}