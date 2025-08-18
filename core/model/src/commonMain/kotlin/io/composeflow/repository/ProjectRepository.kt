@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.composeflow.repository

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.datastore.LocalFirstProjectSaver
import io.composeflow.datastore.PlatformDataStore
import io.composeflow.datastore.ProjectSaver
import io.composeflow.di.ServiceLocator
import io.composeflow.model.project.Project
import io.composeflow.model.project.serialize
import io.composeflow.platform.getOrCreateDataStore
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.util.toKotlinFileName
import io.composeflow.util.toPackageName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjectRepository(
    private val firebaseIdToken: FirebaseIdToken,
    private val projectSaver: ProjectSaver = LocalFirstProjectSaver(),
    private val dataStore: PlatformDataStore = ServiceLocator.getOrPut { getOrCreateDataStore() },
) {
    private val editingProjectKey = "editing_project"

    val editingProject: Flow<Project> =
        dataStore
            .observeString(editingProjectKey)
            .map { projectJson ->
                projectJson?.let {
                    decodeFromStringWithFallback<Project>(it)
                } ?: Project()
            }

    suspend fun createProject(
        projectName: String,
        packageName: String,
    ): Project {
        val project =
            Project(
                name = projectName.toKotlinFileName(),
                packageName = packageName.toPackageName(),
            )
        projectSaver.saveProjectYaml(
            userId = firebaseIdToken.user_id,
            projectId = project.id,
            yamlContent = project.serialize(),
        )
        return project
    }

    suspend fun deleteProject(projectId: String) {
        projectSaver.deleteProject(
            userId = firebaseIdToken.user_id,
            projectId = projectId,
        )
    }

    suspend fun loadProject(projectId: String): Result<Project?, Throwable> =
        runCatching {
            val loaded =
                projectSaver.loadProject(
                    userId = firebaseIdToken.user_id,
                    projectId = projectId.removeSuffix(".yaml"),
                )
            loaded?.let {
                val project = Project.deserializeFromString(it.yaml)
                project.lastModified = it.lastModified
                project
            }
        }

    suspend fun updateProject(
        project: Project,
        syncWithCloud: Boolean = false,
    ) {
        projectSaver.saveProjectYaml(
            userId = firebaseIdToken.user_id,
            projectId = project.id,
            yamlContent = project.serialize(),
            syncWithCloud = syncWithCloud,
        )

        // Save the project to DataStore - the observeString Flow will automatically emit the update
        dataStore.putString(editingProjectKey, encodeToString(project))
    }

    suspend fun loadProjectIdList(): List<String> = projectSaver.loadProjectIdList(userId = firebaseIdToken.user_id)

    companion object {
        fun createAnonymous(): ProjectRepository = ProjectRepository(firebaseIdToken = FirebaseIdToken.Anonymouse)
    }
}
