package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.TypeSpec

/**
 * JVM implementation of TypeSpecWrapper that delegates to actual KotlinPoet's TypeSpec.
 */
actual class TypeSpecWrapper internal constructor(private val actual: TypeSpec) {
    actual enum class Kind {
        CLASS,
        OBJECT,
        INTERFACE,
        ENUM,
        ANNOTATION
    }
    
    actual companion object {
        actual fun classBuilder(name: String): TypeSpecBuilderWrapper = 
            TypeSpecBuilderWrapper(TypeSpec.classBuilder(name))
        actual fun classBuilder(className: ClassNameWrapper): TypeSpecBuilderWrapper = 
            TypeSpecBuilderWrapper(TypeSpec.classBuilder(className.toKotlinPoetClassName()))
        actual fun objectBuilder(name: String): TypeSpecBuilderWrapper = 
            TypeSpecBuilderWrapper(TypeSpec.objectBuilder(name))
        actual fun interfaceBuilder(name: String): TypeSpecBuilderWrapper = 
            TypeSpecBuilderWrapper(TypeSpec.interfaceBuilder(name))
        actual fun enumBuilder(name: String): TypeSpecBuilderWrapper = 
            TypeSpecBuilderWrapper(TypeSpec.enumBuilder(name))
        actual fun annotationBuilder(name: String): TypeSpecBuilderWrapper = 
            TypeSpecBuilderWrapper(TypeSpec.annotationBuilder(name))
        actual fun funInterfaceBuilder(name: String): TypeSpecBuilderWrapper = 
            TypeSpecBuilderWrapper(TypeSpec.funInterfaceBuilder(name))
        actual fun anonymousClassBuilder(): TypeSpecBuilderWrapper = 
            TypeSpecBuilderWrapper(TypeSpec.anonymousClassBuilder())
    }
    
    actual val name: String? get() = actual.name
    actual val kind: TypeSpecWrapper.Kind get() = when (actual.kind) {
        com.squareup.kotlinpoet.TypeSpec.Kind.CLASS -> TypeSpecWrapper.Kind.CLASS
        com.squareup.kotlinpoet.TypeSpec.Kind.OBJECT -> TypeSpecWrapper.Kind.OBJECT
        com.squareup.kotlinpoet.TypeSpec.Kind.INTERFACE -> TypeSpecWrapper.Kind.INTERFACE
        else -> {
            // For kinds that don't have direct mappings (ENUM, ANNOTATION), determine based on other properties
            // This is a temporary workaround - the actual KotlinPoet may have different enum values
            if (actual.name?.contains("Enum") == true) TypeSpecWrapper.Kind.ENUM
            else if (actual.annotations.isNotEmpty()) TypeSpecWrapper.Kind.ANNOTATION
            else TypeSpecWrapper.Kind.CLASS // Default fallback
        }
    }
    actual val modifiers: Set<KModifierWrapper> get() = actual.modifiers.map { it.toWrapper() }.toSet()
    actual val annotations: List<AnnotationSpecWrapper> get() = actual.annotations.map { AnnotationSpecWrapper(it) }
    actual val propertySpecs: List<PropertySpecWrapper> get() = actual.propertySpecs.map { PropertySpecWrapper(it) }
    actual val funSpecs: List<FunSpecWrapper> get() = actual.funSpecs.map { FunSpecWrapper(it) }
    actual val typeSpecs: List<TypeSpecWrapper> get() = actual.typeSpecs.map { TypeSpecWrapper(it) }
    
    actual override fun toString(): String = actual.toString()
    
    // Internal accessor for other wrapper classes
    internal fun toKotlinPoet(): TypeSpec = actual
}

actual class TypeSpecBuilderWrapper internal constructor(private val actual: TypeSpec.Builder) {
    actual fun addModifiers(vararg modifiers: KModifierWrapper): TypeSpecBuilderWrapper = 
        TypeSpecBuilderWrapper(actual.addModifiers(*modifiers.map { it.toKotlinPoet() }.toTypedArray()))
    actual fun addAnnotation(annotationSpec: AnnotationSpecWrapper): TypeSpecBuilderWrapper = 
        TypeSpecBuilderWrapper(actual.addAnnotation(annotationSpec.toKotlinPoet()))
    actual fun addProperty(propertySpec: PropertySpecWrapper): TypeSpecBuilderWrapper = 
        TypeSpecBuilderWrapper(actual.addProperty(propertySpec.toKotlinPoet()))
    actual fun addFunction(funSpec: FunSpecWrapper): TypeSpecBuilderWrapper = 
        TypeSpecBuilderWrapper(actual.addFunction(funSpec.toKotlinPoet()))
    actual fun addType(typeSpec: TypeSpecWrapper): TypeSpecBuilderWrapper = 
        TypeSpecBuilderWrapper(actual.addType(typeSpec.toKotlinPoet()))
    actual fun primaryConstructor(primaryConstructor: FunSpecWrapper): TypeSpecBuilderWrapper = 
        TypeSpecBuilderWrapper(actual.primaryConstructor(primaryConstructor.toKotlinPoet()))
    actual fun addSuperinterface(superinterface: TypeNameWrapper): TypeSpecBuilderWrapper = 
        TypeSpecBuilderWrapper(actual.addSuperinterface(superinterface.toKotlinPoet()))
    actual fun superclass(superclass: TypeNameWrapper): TypeSpecBuilderWrapper = 
        TypeSpecBuilderWrapper(actual.superclass(superclass.toKotlinPoet()))
    actual fun addInitializerBlock(block: CodeBlockWrapper): TypeSpecBuilderWrapper = 
        TypeSpecBuilderWrapper(actual.addInitializerBlock(block.toKotlinPoet()))
    actual fun addEnumConstant(name: String, typeSpec: TypeSpecWrapper): TypeSpecBuilderWrapper = 
        TypeSpecBuilderWrapper(actual.addEnumConstant(name, typeSpec.toKotlinPoet()))
    actual fun addEnumConstant(name: String): TypeSpecBuilderWrapper = 
        TypeSpecBuilderWrapper(actual.addEnumConstant(name))
    actual fun addSuperclassConstructorParameter(format: String, vararg args: Any?): TypeSpecBuilderWrapper = 
        TypeSpecBuilderWrapper(actual.addSuperclassConstructorParameter(format, *args.filterNotNull().toTypedArray()))
    actual fun addSuperclassConstructorParameter(codeBlock: CodeBlockWrapper): TypeSpecBuilderWrapper = 
        TypeSpecBuilderWrapper(actual.addSuperclassConstructorParameter(codeBlock.toKotlinPoet()))
    actual fun build(): TypeSpecWrapper = TypeSpecWrapper(actual.build())
}

// Helper function
fun TypeSpec.toWrapper(): TypeSpecWrapper = TypeSpecWrapper(this)