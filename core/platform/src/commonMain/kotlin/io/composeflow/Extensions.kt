package io.composeflow

fun String.removeLineBreak() = this.replace("\r\n|\r|\n".toRegex(), "")
