package io.composeflow.platform

import io.composeflow.datastore.ProjectSaver
import io.composeflow.datastore.ProjectYamlNameWithLastModified

actual fun createLocalProjectSaver(): ProjectSaver {
    // Local project saving is not supported in WASM environment
    return object : ProjectSaver {
        override suspend fun saveProjectYaml(
            userId: String,
            projectId: String,
            yamlContent: String,
            syncWithCloud: Boolean,
        ) {
            // No-op for WASM - could potentially use browser localStorage in future
        }

        override suspend fun loadProject(
            userId: String,
            projectId: String,
        ): ProjectYamlNameWithLastModified? = null

        override suspend fun deleteProject(
            userId: String,
            projectId: String,
        ) {
            // No-op for WASM
        }

        override suspend fun loadProjectIdList(userId: String): List<String> = emptyList()

        override suspend fun deleteCacheProjects() {
            // No-op for WASM
        }
    }
}
