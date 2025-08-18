package io.composeflow.wrapper

import io.composeflow.platform.PlatformFile

expect class ZipWrapper {
    fun unzip(sourceFile: PlatformFile, destDirectory: PlatformFile)
    fun zipDirectory(input: PlatformFile, output: PlatformFile)
}