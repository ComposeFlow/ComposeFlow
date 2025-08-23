package io.composeflow.kotlinpoet.wrapper

import com.squareup.kotlinpoet.FileSpec

/**
 * JVM implementation of FileSpecWrapper that delegates to actual KotlinPoet's FileSpec.
 */
actual class FileSpecWrapper internal constructor(
    private val actual: FileSpec,
) {
    actual companion object {
        actual fun builder(
            packageName: String,
            fileName: String,
        ): FileSpecBuilderWrapper = FileSpecBuilderWrapper(FileSpec.builder(packageName, fileName))
    }

    actual val packageName: String get() = actual.packageName
    actual val name: String get() = actual.name
    actual val annotations: List<AnnotationSpecWrapper> get() = actual.annotations.map { AnnotationSpecWrapper(it) }
    actual val members: List<Any> get() = actual.members

    actual override fun toString(): String = actual.toString()

    // Internal accessor for other wrapper classes
    internal fun toKotlinPoet(): FileSpec = actual
}

actual class FileSpecBuilderWrapper internal constructor(
    private val actual: FileSpec.Builder,
) {
    actual fun addType(typeSpec: TypeSpecWrapper): FileSpecBuilderWrapper = FileSpecBuilderWrapper(actual.addType(typeSpec.toKotlinPoet()))

    actual fun addFunction(funSpec: FunSpecWrapper): FileSpecBuilderWrapper =
        FileSpecBuilderWrapper(actual.addFunction(funSpec.toKotlinPoet()))

    actual fun addProperty(propertySpec: PropertySpecWrapper): FileSpecBuilderWrapper =
        FileSpecBuilderWrapper(actual.addProperty(propertySpec.toKotlinPoet()))

    actual fun addAnnotation(annotationSpec: AnnotationSpecWrapper): FileSpecBuilderWrapper =
        FileSpecBuilderWrapper(actual.addAnnotation(annotationSpec.toKotlinPoet()))

    actual fun addImport(
        packageName: String,
        vararg names: String,
    ): FileSpecBuilderWrapper = FileSpecBuilderWrapper(actual.addImport(packageName, *names))

    actual fun addImport(
        className: ClassNameWrapper,
        vararg names: String,
    ): FileSpecBuilderWrapper = FileSpecBuilderWrapper(actual.addImport(className.toKotlinPoetClassName(), *names))

    actual fun addComment(
        format: String,
        vararg args: Any,
    ): FileSpecBuilderWrapper = FileSpecBuilderWrapper(actual.addFileComment(format, *convertArgsArray(args)))

    actual fun addCode(codeBlock: CodeBlockWrapper): FileSpecBuilderWrapper =
        FileSpecBuilderWrapper(actual.addCode(codeBlock.toKotlinPoet()))

    actual fun suppressWarningTypes(vararg types: String): FileSpecBuilderWrapper {
        // Add @file:Suppress annotation with the specified warning types
        val suppressAnnotation =
            AnnotationSpecWrapper
                .builder(ClassNameWrapper.get("kotlin", "Suppress"))
                .addMember(types.joinToString(", ") { "\"$it\"" })
                .build()
        return FileSpecBuilderWrapper(actual.addAnnotation(suppressAnnotation.toKotlinPoet()))
    }

    actual fun build(): FileSpecWrapper = FileSpecWrapper(actual.build())
}

// Helper function
fun FileSpec.toWrapper(): FileSpecWrapper = FileSpecWrapper(this)
