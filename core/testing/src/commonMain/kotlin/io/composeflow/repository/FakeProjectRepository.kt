package io.composeflow.repository

import io.composeflow.auth.FirebaseIdToken
import io.composeflow.datastore.ProjectSaver
import io.composeflow.datastore.ProjectYamlNameWithLastModified
import kotlinx.serialization.json.JsonObject

val fakeFirebaseIdToken =
    FirebaseIdToken.SignedInToken(
        name = "John Doe",
        picture = "pictureUrl",
        iss = "iss",
        aud = "your-project-id",
        auth_time = 1609459200L, // Example timestamp
        user_id = "1234567890",
        sub = "1234567890",
        iat = 1609459200L, // Example timestamp
        exp = 1609462800L, // Example timestamp
        email = "email",
        email_verified = true,
        firebase = JsonObject(mapOf()),
    )

val fakeProjectRepository =
    ProjectRepository(
        firebaseIdToken = fakeFirebaseIdToken,
        projectSaver =
            object : ProjectSaver {
                override suspend fun saveProjectYaml(
                    userId: String,
                    projectId: String,
                    yamlContent: String,
                    syncWithCloud: Boolean,
                ) {
                }

                override suspend fun deleteProject(
                    userId: String,
                    projectId: String,
                ) {
                }

                override suspend fun loadProject(
                    userId: String,
                    projectId: String,
                ): ProjectYamlNameWithLastModified? = null

                override suspend fun loadProjectIdList(userId: String): List<String> = emptyList()

                override suspend fun deleteCacheProjects() {
                }
            },
    )
