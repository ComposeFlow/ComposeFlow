package io.composeflow.datastore

const val PROJECT_YAML_FILE_NAME = "project.yaml"
const val ANONYMOUSE_USER_ID = "anonymous"

interface ProjectSaver {
    suspend fun saveProjectYaml(
        userId: String,
        projectId: String,
        yamlContent: String,
        syncWithCloud: Boolean = false,
    )

    suspend fun deleteProject(
        userId: String,
        projectId: String,
    )

    suspend fun loadProject(
        userId: String,
        projectId: String,
    ): ProjectYamlNameWithLastModified?

    suspend fun loadProjectIdList(userId: String): List<String>

    suspend fun deleteCacheProjects()
}
