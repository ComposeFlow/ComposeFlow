package io.composeflow.kotlinpoet.wrapper

/**
 * Converts wrapper types to their KotlinPoet equivalents for use in format strings.
 * This is used when passing arguments to KotlinPoet methods that accept format strings.
 */
internal fun convertWrapperToKotlinPoet(arg: Any?): Any? {
    return when (arg) {
        is ClassNameWrapper -> arg.toKotlinPoetClassName()
        is TypeNameWrapper -> arg.toKotlinPoet()
        is MemberNameWrapper -> arg.toKotlinPoet()
        is CodeBlockWrapper -> arg.toKotlinPoet()
        else -> arg
    }
}

/**
 * Converts an array of arguments, replacing wrapper types with their KotlinPoet equivalents.
 * Also filters out null values to match KotlinPoet's expected parameter types.
 */
internal fun convertArgsArray(args: Array<out Any?>): Array<Any> {
    return args.mapNotNull(::convertWrapperToKotlinPoet).toTypedArray()
}