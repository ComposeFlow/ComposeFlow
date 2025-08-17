package io.composeflow.wrapper

import co.touchlab.kermit.Logger
import io.composeflow.CurrentOs
import io.composeflow.currentOs
import io.composeflow.http.KtorClientFactory
import io.composeflow.platform.getCacheDir
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.utils.EmptyContent.getProperty
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Buffer
import okio.BufferedSink
import okio.buffer
import okio.sink
import java.io.File
import kotlin.io.path.inputStream

actual object JdkDownloader {
    // TODO: Maybe replace with DI
    private val httpClient = KtorClientFactory.createWithoutLogging()

    actual suspend fun downloadAndExtract(
        downloadableJdk: DownloadableJdk,
        tarExtractor: TarExtractor,
        dispatcher: CoroutineDispatcher,
    ): String? =
        withContext(dispatcher) {
            val tempFile = kotlin.io.path.createTempFile()
            download(
                downloadableJdk = DownloadableJdk.OpenJdk17,
                destFile = tempFile.toFile(),
                dispatcher = dispatcher,
            )

            val jdkDir = getCacheDir().resolve("jdk")
            val destDir = jdkDir.resolve(downloadableJdk.getJdkDirName())
            if (!jdkDir.exists()) {
                jdkDir.mkdirs()
            }
            if (destDir.exists()) {
                destDir.deleteRecursively()
            }
            var extractedJavaHome: String? = null
            when (currentOs) {
                CurrentOs.Windows -> {
                    tempFile.inputStream().unzip(destDir.toPath())
                    extractedJavaHome = tryToFindJavaHomeDir(destDir)
                }

                CurrentOs.Linux -> {
                    tarExtractor.extractTarGz(tempFile.toFile(), destDir)
                    extractedJavaHome = tryToFindJavaHomeDir(destDir)
                }

                CurrentOs.Mac -> {
                    tarExtractor.extractTarGz(tempFile.toFile(), destDir)
                    extractedJavaHome = tryToFindJavaHomeDir(destDir)
                }

                CurrentOs.Other -> {}
            }
            extractedJavaHome
        }

    suspend fun download(
        destFile: File,
        downloadableJdk: DownloadableJdk = DownloadableJdk.OpenJdk17,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
    ) {
        withContext(dispatcher) {
            val url = downloadableJdk.getActualDownloadUrl()

            val response = httpClient.get(url)
            val channel: ByteReadChannel = response.bodyAsChannel()

            val sink: BufferedSink = destFile.sink().buffer()
            val sinkBuffer: Buffer = sink.buffer

            var totalBytesRead: Long = 0
            val bufferSize = 8 * 1024
            val buffer = ByteArray(bufferSize)

            while (!channel.isClosedForRead) {
                val bytesRead = channel.readAvailable(buffer, 0, bufferSize)
                if (bytesRead == -1) break
                sinkBuffer.write(buffer, 0, bytesRead)
                sink.emit()
                totalBytesRead += bytesRead
            }
            sink.flush()
            sink.close()
        }
    }

    private fun tryToFindJavaHomeDir(targetDir: File): String? {
        val parentDirectories = mutableListOf<File>()

        fun traverse(currentDir: File) {
            currentDir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    if (file.name.equals("bin", ignoreCase = true)) {
                        val javaFile = File(file, "java")
                        val javaFileWindows = File(file, "java.exe")

                        if (javaFile.exists() || javaFileWindows.exists()) {
                            val parentDir = file.parentFile
                            if (parentDir != null) {
                                parentDirectories.add(parentDir)
                            } else {
                                Logger.i(
                                    "Found 'bin' directory with 'java' file but no parent: ${file.absolutePath}\n",
                                )
                            }
                        }
                    }
                    // Continue traversing subdirectories
                    traverse(file)
                }
            }
        }

        traverse(targetDir)

        return if (parentDirectories.isNotEmpty()) parentDirectories[0].absolutePath else null
    }
}

// Extension function for desktop-specific URL building
private fun DownloadableJdk.getActualDownloadUrl(): String {
    return when (this) {
        is DownloadableJdk.OpenJdk17 -> {
            val baseUrl =
                "https://download.java.net/java/GA/jdk17/0d483333a00540d886896bac774ff48b/35/GPL/openjdk-17"

            val osName = getProperty("os.name").lowercase()
            val os: String =
                when {
                    osName.contains("windows") -> "windows"
                    osName.contains("mac") || osName.contains("darwin") -> "macos"
                    osName.contains("linux") -> "linux"
                    else -> throw UnsupportedOperationException("Unsupported Operating System: $osName")
                }

            val osArch = getProperty("os.arch").lowercase()
            val arch: String =
                when {
                    osArch in listOf("amd64", "x86_64") -> "x64"
                    osArch == "aarch64" -> "aarch64"
                    else -> throw UnsupportedOperationException("Unsupported CPU Architecture: $osArch")
                }

            val fileExtension =
                when (os) {
                    "windows" -> "zip"
                    else -> "tar.gz"
                }

            when (os) {
                "windows" -> "${baseUrl}_$os-x64_bin.$fileExtension"
                "macos" -> "${baseUrl}_$os-${arch}_bin.$fileExtension"
                "linux" -> "${baseUrl}_$os-${arch}_bin.$fileExtension"
                else -> throw UnsupportedOperationException("Unsupported Operating System: $os")
            }
        }
    }
}