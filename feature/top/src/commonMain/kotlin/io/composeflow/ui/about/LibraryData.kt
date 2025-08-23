package io.composeflow.ui.about

import io.composeflow.ui.popup.Library

object LibraryData {
    // Hardcoded library list based on gradle/libs.versions.toml
    // This should be updated when dependencies change
    val libraries =
        listOf(
            // AndroidX
            Library("activity-compose", "androidx.activity", "1.10.1"),
            Library("appcompat", "androidx.appcompat", "1.7.0"),
            Library("datastore-core-okio", "androidx.datastore", "1.1.4"),
            Library("datastore-preferences", "androidx.datastore", "1.1.4"),
            Library("credentials-play-services-auth", "androidx.credentials", "1.5.0"),
            // Compose
            Library("compose-multiplatform", "org.jetbrains.compose", "1.8.0"),
            Library("compose-navigation", "org.jetbrains.androidx.navigation", "2.8.0-alpha10"),
            Library("compose-adaptive", "org.jetbrains.compose.material3.adaptive", "1.0.0-alpha01"),
            Library("compose-color-picker", "com.github.skydoves", "1.1.2"),
            Library("compose-shimmer", "com.valentinilk.shimmer", "1.3.0"),
            Library("image-loader", "io.github.qdsfdhvh", "1.10.0"),
            Library("compose-code-editor", "com.github.qawaz.compose-code-editor", "v3.1.1"),
            // Jewel UI
            Library("jewel-int-ui-standalone", "org.jetbrains.jewel", "0.9.0"),
            Library("jewel-int-ui-decorated-window", "org.jetbrains.jewel", "0.9.0"),
            // PreCompose
            Library("precompose", "moe.tlaster", "1.6.2"),
            Library("precompose-viewmodel", "moe.tlaster", "1.6.2"),
            // Kotlin
            Library("kotlin", "org.jetbrains.kotlin", "2.1.21"),
            Library("kotlinx-coroutines-core", "org.jetbrains.kotlinx", "1.10.1"),
            Library("kotlinx-serialization-core", "org.jetbrains.kotlinx", "1.8.1"),
            Library("kotlinx-serialization-json", "org.jetbrains.kotlinx", "1.8.1"),
            Library("kotlinx-datetime", "org.jetbrains.kotlinx", "0.6.2"),
            Library("kotlinpoet", "com.squareup", "2.1.0"),
            // Ktor
            Library("ktor-client-core", "io.ktor", "3.1.2"),
            Library("ktor-client-cio", "io.ktor", "3.1.2"),
            Library("ktor-client-okhttp", "io.ktor", "3.1.2"),
            Library("ktor-serialization-kotlinx-json", "io.ktor", "3.1.2"),
            // Firebase
            Library("firebase-auth", "dev.gitlive", "2.1.0"),
            Library("firebase-firestore", "dev.gitlive", "2.1.0"),
            Library("firebase-admin", "com.google.firebase", "9.3.0"),
            // Auth
            Library("kmpauth-firebase", "io.github.mirzemehdi", "2.1.0-alpha02"),
            Library("kmpauth-google", "io.github.mirzemehdi", "2.1.0-alpha02"),
            // UI Libraries
            Library("reorderable", "sh.calvin.reorderable", "3.0.0"),
            Library("richeditor-compose", "com.mohamedrejeb.richeditor", "1.0.0-rc05-k2"),
            Library("material-kolor", "com.materialkolor", "1.6.1"),
            Library("filekit-compose", "io.github.vinceglb", "0.6.3"),
            // Logging
            Library("kermit", "co.touchlab", "2.0.5"),
            Library("slf4j-api", "org.slf4j", "2.0.16"),
            Library("logback-classic", "ch.qos.logback", "1.4.14"),
            // Monitoring
            Library("sentry", "io.sentry", "8.7.0"),
            Library("sentry-compose", "io.sentry", "8.7.0"),
            // Testing
            Library("roborazzi-compose-desktop", "io.github.takahirom.roborazzi", "1.26.0"),
            // Other
            Library("multiplatform-settings", "com.russhwolf", "1.3.0"),
            Library("conveyor-control", "dev.hydraulic.conveyor", "1.1"),
            Library("kaml", "com.charleskorn.kaml", "0.55.0"),
            Library("kotlin-result", "com.michael-bull.kotlin-result", "1.1.18"),
            Library("commons-compress", "org.apache.commons", "1.21"),
            Library("okhttp", "com.squareup.okhttp3", "4.12.0"),
            Library("paging-common", "app.cash.paging", "3.3.0-alpha02-0.5.1"),
            Library("atomicfu", "org.jetbrains.kotlinx", "0.27.0"),
        ).sortedBy { "${it.group}:${it.name}" }
}
