package io.composeflow.wrapper

import io.composeflow.platform.PlatformFile
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

actual class TarExtractor {
    actual fun extractTarGz(
        tarGzFile: PlatformFile,
        destinationDirectory: PlatformFile,
    ) {
        val tarFile = tarGzFile.toFile()
        val destFile = destinationDirectory.toFile()
        
        File(tarFile.absolutePath).inputStream().use { fileInputStream ->
            GzipCompressorInputStream(fileInputStream).use { gzipInputStream ->
                TarArchiveInputStream(gzipInputStream).use { tarInputStream ->
                    var entry = tarInputStream.nextTarEntry
                    while (entry != null) {
                        val outputFile = File(destFile, entry.name)
                        if (entry.isDirectory) {
                            if (!outputFile.exists()) {
                                if (!outputFile.mkdirs()) {
                                    throw IOException("Failed to create directory: ${outputFile.absolutePath}")
                                }
                            }
                        } else {
                            // Ensure parent directories exist
                            val parent = outputFile.parentFile
                            if (!parent.exists()) {
                                if (!parent.mkdirs()) {
                                    throw IOException("Failed to create directory: ${parent.absolutePath}")
                                }
                            }

                            // Write file content
                            FileOutputStream(outputFile).use { outputStream ->
                                tarInputStream.copyTo(outputStream)
                            }

                            // Preserve file permissions
                            setFilePermissions(outputFile, entry.mode)

                            // Preserve last modified time
                            outputFile.setLastModified(entry.lastModifiedDate.time)
                        }
                        entry = tarInputStream.nextTarEntry
                    }
                }
            }
        }
    }

    private fun setFilePermissions(
        file: File,
        mode: Int,
    ) {
        // POSIX permissions are only supported on UNIX-like systems
        if (File.separatorChar == '/') {
            val perms = mutableSetOf<PosixFilePermission>()
            if ((mode and 0b100_000_000) != 0) perms.add(PosixFilePermission.OWNER_READ)
            if ((mode and 0b010_000_000) != 0) perms.add(PosixFilePermission.OWNER_WRITE)
            if ((mode and 0b001_000_000) != 0) perms.add(PosixFilePermission.OWNER_EXECUTE)
            if ((mode and 0b000_100_000) != 0) perms.add(PosixFilePermission.GROUP_READ)
            if ((mode and 0b000_010_000) != 0) perms.add(PosixFilePermission.GROUP_WRITE)
            if ((mode and 0b000_001_000) != 0) perms.add(PosixFilePermission.GROUP_EXECUTE)
            if ((mode and 0b000_000_100) != 0) perms.add(PosixFilePermission.OTHERS_READ)
            if ((mode and 0b000_000_010) != 0) perms.add(PosixFilePermission.OTHERS_WRITE)
            if ((mode and 0b000_000_001) != 0) perms.add(PosixFilePermission.OTHERS_EXECUTE)

            try {
                Files.setPosixFilePermissions(file.toPath(), perms)
            } catch (e: UnsupportedOperationException) {
                println("Warning: Cannot set file permissions on non-POSIX file system.")
            }
        }
    }
}