package io.composeflow.platform

import co.touchlab.kermit.Logger
import io.composeflow.datastore.ProjectSaver
import io.composeflow.datastore.ProjectYamlNameWithLastModified
import kotlinx.browser.localStorage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.time.Instant

external object Date {
    fun now(): Double
}

actual fun createLocalProjectSaver(): ProjectSaver = WasmProjectSaverImpl()

@Serializable
data class ProjectData(
    val yamlContent: String,
    val lastModified: Long
)

@Serializable
data class UserProjects(
    val projectIds: Set<String>
)

class WasmProjectSaverImpl : ProjectSaver {
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    private fun projectKey(userId: String, projectId: String): String =
        "composeflow_project_${userId}_${projectId}"

    private fun userProjectsKey(userId: String): String =
        "composeflow_user_projects_$userId"

    override suspend fun saveProjectYaml(
        userId: String,
        projectId: String,
        yamlContent: String,
        syncWithCloud: Boolean,
    ) {
        val key = projectKey(userId, projectId)

        // Save project data
        val nowMillis = Date.now().toLong()
        val projectData = ProjectData(
            yamlContent = yamlContent,
            lastModified = nowMillis
        )
        localStorage[key] = json.encodeToString(projectData)

        // Update user's project list
        val projectsKey = userProjectsKey(userId)
        val existingProjects = try {
            localStorage[projectsKey]?.let {
                json.decodeFromString<UserProjects>(it).projectIds
            } ?: emptySet()
        } catch (e: Exception) {
            Logger.w(throwable = e) { "Failed to parse user projects" }
            emptySet()
        }

        val updatedProjects = UserProjects(existingProjects + projectId)
        localStorage[projectsKey] = json.encodeToString(updatedProjects)
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    override suspend fun loadProject(
        userId: String,
        projectId: String,
    ): ProjectYamlNameWithLastModified? {
        val key = projectKey(userId, projectId)

        return try {
            localStorage[key]?.let { jsonString ->
                val projectData = json.decodeFromString<ProjectData>(jsonString)
                ProjectYamlNameWithLastModified(
                    yaml = projectData.yamlContent,
                    lastModified = Instant.fromEpochMilliseconds(projectData.lastModified),
                )
            }
        } catch (e: Exception) {
            Logger.w(throwable = e) { "Failed to parse project data" }
            null
        }
    }

    override suspend fun deleteProject(
        userId: String,
        projectId: String,
    ) {
        val key = projectKey(userId, projectId)

        // Remove project data
        localStorage.removeItem(key)

        // Update user's project list
        val projectsKey = userProjectsKey(userId)
        val existingProjects = try {
            localStorage[projectsKey]?.let {
                json.decodeFromString<UserProjects>(it).projectIds
            } ?: emptySet()
        } catch (_: Exception) {
            emptySet()
        }

        val updatedProjects = existingProjects - projectId
        if (updatedProjects.isEmpty()) {
            localStorage.removeItem(projectsKey)
        } else {
            localStorage[projectsKey] = json.encodeToString(UserProjects(updatedProjects))
        }
    }

    override suspend fun loadProjectIdList(userId: String): List<String> {
        val projectsKey = userProjectsKey(userId)

        return try {
            localStorage[projectsKey]?.let {
                json.decodeFromString<UserProjects>(it).projectIds.toList()
            } ?: emptyList()
        } catch (e: Exception) {
            Logger.w(throwable = e) { "Failed to load project list for user: $userId" }
            emptyList()
        }
    }

    override suspend fun deleteCacheProjects() {
        // Clear all ComposeFlow project-related items from localStorage
        val keysToRemove = mutableListOf<String>()
        for (i in 0 until localStorage.length) {
            localStorage.key(i)?.let { key ->
                if (key.startsWith("composeflow_")) {
                    keysToRemove.add(key)
                }
            }
        }

        keysToRemove.forEach { key ->
            localStorage.removeItem(key)
        }
    }
}
