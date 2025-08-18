package io.composeflow.wrapper

import io.composeflow.platform.PlatformFile
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

private const val UNZIP_MAX_ENTRIES: Int = 1024 * 15
private const val UNZIP_MAX_FILE_SIZE: Long = 1024L * 1024L * 1024L * 5L // 5GiB
private const val UNZIP_TOTAL_MAX_SIZE: Long = 1024L * 1024L * 1024L * 5L // 5GiB
private const val UNZIP_BUFFER_SIZE: Int = 1024 * 1024 // 1MiB

actual class ZipWrapper {
    actual fun unzip(sourceFile: PlatformFile, destDirectory: PlatformFile) {
        sourceFile.toFile().inputStream().unzip(destDirectory.toFile().toPath())
    }
    
    actual fun zipDirectory(input: PlatformFile, output: PlatformFile) {
        zipDirectory(input.toFile(), output.toFile())
    }
}

fun InputStream.unzip(
    destDirPath: Path,
    zipFileCoding: Charset = Charset.forName("UTF-8"),
) {
    ZipInputStream(this, zipFileCoding).use { f ->
        var zipEntry: ZipEntry?
        var nEntries = 0
        var totalReads = 0L
        val buffer = ByteArray(UNZIP_BUFFER_SIZE)
        while (f.nextEntry.also { zipEntry = it } != null) {
            val entryPath = Paths.get(zipEntry!!.name).normalize()
            if (entryPath.startsWith(Paths.get(".."))) {
                throw IllegalStateException("File is outside extraction target directory.")
            }
            if (nEntries++ >= UNZIP_MAX_ENTRIES) {
                throw IllegalStateException("Too many files to unzip.")
            }

            val dst = destDirPath.resolve(entryPath)
            if (zipEntry!!.isDirectory) {
                Files.createDirectories(dst)
            } else {
                Files.createDirectories(dst.parent)

                var totalFileReads = 0L
                var nReads: Int
                FileOutputStream(dst.toFile()).use { fos ->
                    BufferedOutputStream(fos).use { out ->
                        while (f.read(buffer, 0, buffer.size).also { nReads = it } != -1) {
                            totalReads += nReads
                            if (totalReads > UNZIP_TOTAL_MAX_SIZE) {
                                throw IllegalStateException("Total file size being unzipped is too big.")
                            }
                            totalFileReads += nReads
                            if (totalFileReads > UNZIP_MAX_FILE_SIZE) {
                                throw IllegalStateException("File being unzipped is too big.")
                            }
                            out.write(buffer, 0, nReads)
                        }
                        out.flush()
                    }
                }
                f.closeEntry()
            }
        }
    }
}

fun zipDirectory(
    input: File,
    output: File,
) {
    ZipArchiveOutputStream(BufferedOutputStream(FileOutputStream(output))).use { zos ->
        input
            .walkTopDown()
            .filter { file ->
                // Exclude any file that is in a directory named "build"
                !file.absolutePath.split(File.separator).contains("build")
            }.forEach { file ->
                val zipFileName =
                    file.absolutePath.removePrefix(input.absolutePath).removePrefix("/")
                val entry = ZipArchiveEntry(file, zipFileName)

                // Preserve executable bit for gradlew
                if (file.name == "gradlew" && file.canExecute()) {
                    entry.unixMode =
                        Integer.parseInt("0755", 8) // Parse "0755" as an octal (radix 8)
                } else if (file.isDirectory) {
                    entry.unixMode = Integer.parseInt("0755", 8)
                } else {
                    entry.unixMode = Integer.parseInt("0644", 8) // Default permissions
                }

                zos.putArchiveEntry(entry)
                if (file.isFile) {
                    file.inputStream().use { fis -> fis.copyTo(zos) }
                }
                zos.closeArchiveEntry()
            }
    }
}