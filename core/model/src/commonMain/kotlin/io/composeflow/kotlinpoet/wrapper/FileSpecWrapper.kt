package io.composeflow.kotlinpoet.wrapper

/**
 * Multiplatform wrapper for KotlinPoet's FileSpec.
 * On JVM, delegates to actual FileSpec. On WASM, provides no-op implementation.
 */
expect class FileSpecWrapper {
    companion object {
        fun builder(packageName: String, fileName: String): FileSpecBuilderWrapper
    }
    
    val packageName: String
    val name: String
    val annotations: List<AnnotationSpecWrapper>
    val members: List<Any>
    
    override fun toString(): String
}

expect class FileSpecBuilderWrapper {
    fun addType(typeSpec: TypeSpecWrapper): FileSpecBuilderWrapper
    fun addFunction(funSpec: FunSpecWrapper): FileSpecBuilderWrapper
    fun addProperty(propertySpec: PropertySpecWrapper): FileSpecBuilderWrapper
    fun addAnnotation(annotationSpec: AnnotationSpecWrapper): FileSpecBuilderWrapper
    fun addImport(packageName: String, vararg names: String): FileSpecBuilderWrapper
    fun addImport(className: ClassNameWrapper, vararg names: String): FileSpecBuilderWrapper
    fun addComment(format: String, vararg args: Any): FileSpecBuilderWrapper
    fun addCode(codeBlock: CodeBlockWrapper): FileSpecBuilderWrapper
    fun suppressWarningTypes(vararg types: String): FileSpecBuilderWrapper
    fun build(): FileSpecWrapper
}