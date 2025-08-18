@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.composeflow.repository

import io.composeflow.datastore.PlatformDataStore
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import com.github.michaelbull.result.runCatching
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.datastore.LocalFirstProjectSaver
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
    
    // Internal state flow for reactive behavior
    private val _editingProjectFlow = MutableStateFlow<Project?>(null)

    val editingProject: Flow<Project> = _editingProjectFlow.filterNotNull()
    
    private suspend fun loadEditingProject(): Project {
        val projectJson = dataStore.getString(editingProjectKey)
        val project = projectJson?.let { decodeFromStringWithFallback<Project>(it) } ?: Project()
        _editingProjectFlow.value = project
        return project
    }
    
    init {
        // Load editing project on initialization
        kotlinx.coroutines.runBlocking {
            loadEditingProject()
        }
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

        // Save the project to DataStore, too so that Flow
        dataStore.putString(editingProjectKey, encodeToString(project))
        // Update cached project
        _editingProjectFlow.value = project
    }

    suspend fun loadProjectIdList(): List<String> = projectSaver.loadProjectIdList(userId = firebaseIdToken.user_id)

    companion object {
        fun createAnonymous(): ProjectRepository = ProjectRepository(firebaseIdToken = FirebaseIdToken.Anonymouse)
    }
}
