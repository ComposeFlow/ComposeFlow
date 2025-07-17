package io.composeflow.wrapper

import co.touchlab.kermit.Logger
import io.composeflow.CurrentOs
import io.composeflow.currentOs
import io.composeflow.platform.getCacheDir
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSink
import okio.buffer
import okio.sink
import java.io.File
import java.lang.System.getProperty
import kotlin.io.path.inputStream

sealed interface DownloadableJdk {
    fun getDownloadUrl(): String
    fun getJdkDirName(): String

    data object OpenJdk17 : DownloadableJdk {

        override fun getJdkDirName(): String = "openjdk-17"

        override fun getDownloadUrl(): String {
            val baseUrl =
                "https://download.java.net/java/GA/jdk17/0d483333a00540d886896bac774ff48b/35/GPL/openjdk-17"

            val osName = getProperty("os.name").lowercase()
            val os: String = when {
                osName.contains("windows") -> "windows"
                osName.contains("mac") || osName.contains("darwin") -> "macos"
                osName.contains("linux") -> "linux"
                else -> throw UnsupportedOperationException("Unsupported Operating System: $osName")
            }

            val osArch = getProperty("os.arch").lowercase()
            val arch: String = when {
                osArch in listOf("amd64", "x86_64") -> "x64"
                osArch == "aarch64" -> "aarch64"
                else -> throw UnsupportedOperationException("Unsupported CPU Architecture: $osArch")
            }

            val fileExtension = when (os) {
                "windows" -> "zip"
                else -> "tar.gz"
            }

            val downloadUrl = when (os) {
                "windows" -> "${baseUrl}_${os}-x64_bin.$fileExtension"
                "macos" -> "${baseUrl}_${os}-${arch}_bin.$fileExtension"
                "linux" -> "${baseUrl}_${os}-${arch}_bin.$fileExtension"
                else -> throw UnsupportedOperationException("Unsupported Operating System: $os")
            }
            return downloadUrl
        }
    }
}

object JdkDownloader {

    // TODO: Maybe replace with DI
    private val okHttpClient: OkHttpClient = OkHttpClient()

    suspend fun downloadAndExtract(
        downloadableJdk: DownloadableJdk = DownloadableJdk.OpenJdk17,
        tarExtractor: TarExtractor = TarExtractor(),
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): String? {
        return withContext(dispatcher) {
            val tempFile = kotlin.io.path.createTempFile()
            download(
                downloadableJdk = DownloadableJdk.OpenJdk17,
                destFile = tempFile.toFile(),
                dispatcher = dispatcher
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
    }

    suspend fun download(
        destFile: File,
        downloadableJdk: DownloadableJdk = DownloadableJdk.OpenJdk17,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) {
        withContext(dispatcher) {
            val url = downloadableJdk.getDownloadUrl()

            val request: Request = Request.Builder().url(url).build()
            val response: Response = okHttpClient.newCall(request).execute()
            val body: ResponseBody = response.body ?: return@withContext
            val contentLength = body.contentLength()
            val source = body.source()

            val sink: BufferedSink = destFile.sink().buffer()
            val sinkBuffer: Buffer = sink.buffer

            var totalBytesRead: Long = 0
            val bufferSize = 8 * 1024
            var bytesRead: Long
            while ((source.read(sinkBuffer, bufferSize.toLong()).also { bytesRead = it }) != -1L) {
                sink.emit()
                totalBytesRead += bytesRead
//                val progress = ((totalBytesRead * 100) / contentLength).toInt()
//                publishProgress(progress)
            }
            sink.flush()
            sink.close()
            source.close()
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
                                    "Found 'bin' directory with 'java' file but no parent: ${file.absolutePath}\n"
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