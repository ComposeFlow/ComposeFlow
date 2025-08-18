package io.composeflow

enum class CurrentOs {
    Windows,
    Linux,
    Mac,
    Other,
}

expect val currentOs: CurrentOs
