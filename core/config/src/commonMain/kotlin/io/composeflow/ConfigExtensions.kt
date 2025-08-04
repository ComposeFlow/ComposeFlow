package io.composeflow

/**
 * gradle-buildconfig-plugin sets a String as "null" if the provided value doesn't exist.
 * This method is to identify that value.
 * Obviously, intrinsic "null" String can't be distinguished.
 */
fun String.isNullString(): Boolean = this == "null"

fun String.isNullStringOrEmpty(): Boolean = this.isNullString() || this.isEmpty()

/**
 * Check if authentication services are properly configured
 */
fun isAuthConfigured(): Boolean =
    !BuildConfig.AUTH_ENDPOINT.isNullStringOrEmpty() &&
        !BuildConfig.FIREBASE_API_KEY.isNullStringOrEmpty()

/**
 * Check if AI/LLM services are properly configured
 */
fun isAiConfigured(): Boolean = !BuildConfig.LLM_ENDPOINT.isNullStringOrEmpty()
