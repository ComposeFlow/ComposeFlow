package io.composeflow.model.apieditor

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import com.squareup.kotlinpoet.CodeBlock
import io.composeflow.ui.propertyeditor.DropdownItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
@SerialName("Authorization")
sealed interface Authorization : DropdownItem {
    @Serializable
    @OptIn(ExperimentalEncodingApi::class)
    @SerialName("BasicAuth")
    data class BasicAuth(
        val username: String = "",
        val password: String = "",
    ) : Authorization {
        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("Basic")

        override fun isSameItem(item: Any): Boolean = item is BasicAuth

        fun makeAuthorizationHeader(): String? =
            if (username.isNotBlank() && password.isNotBlank()) {
                val base64Encoded = Base64.encode("$username:$password".toByteArray())
                "Basic $base64Encoded"
            } else {
                null
            }

        override fun generateCodeBlock(): CodeBlock? =
            if (username.isNotBlank() && password.isNotBlank()) {
                val base64Encoded = Base64.encode("$username:$password".toByteArray())
                CodeBlock.of("Basic $base64Encoded")
            } else {
                null
            }
    }

    fun generateCodeBlock(): CodeBlock?

    companion object {
        fun entries(): List<Authorization> = listOf(BasicAuth())
    }
}
