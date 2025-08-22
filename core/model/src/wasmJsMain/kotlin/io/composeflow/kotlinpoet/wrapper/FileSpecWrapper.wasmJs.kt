package io.composeflow.kotlinpoet.wrapper

/**
 * WASM implementation of FileSpecWrapper that provides no-op implementations.
 * This is used when code generation is not supported on WASM platform.
 */
actual class FileSpecWrapper(
    actual val packageName: String = "",
    actual val name: String = "",
    actual val annotations: List<AnnotationSpecWrapper> = emptyList(),
    actual val members: List<Any> = emptyList()
) {
    actual companion object {
        actual fun builder(packageName: String, fileName: String): FileSpecBuilderWrapper = 
            FileSpecBuilderWrapper()
    }
    
    actual override fun toString(): String = "package $packageName\n\nfile $name"
}

actual class FileSpecBuilderWrapper {
    actual fun addType(typeSpec: TypeSpecWrapper): FileSpecBuilderWrapper = this
    actual fun addFunction(funSpec: FunSpecWrapper): FileSpecBuilderWrapper = this
    actual fun addProperty(propertySpec: PropertySpecWrapper): FileSpecBuilderWrapper = this
    actual fun addAnnotation(annotationSpec: AnnotationSpecWrapper): FileSpecBuilderWrapper = this
    actual fun addImport(packageName: String, vararg names: String): FileSpecBuilderWrapper = this
    actual fun addImport(className: ClassNameWrapper, vararg names: String): FileSpecBuilderWrapper = this
    actual fun addComment(format: String, vararg args: Any): FileSpecBuilderWrapper = this
    actual fun addCode(codeBlock: CodeBlockWrapper): FileSpecBuilderWrapper = this
    actual fun suppressWarningTypes(vararg types: String): FileSpecBuilderWrapper = this
    actual fun build(): FileSpecWrapper = FileSpecWrapper()
}