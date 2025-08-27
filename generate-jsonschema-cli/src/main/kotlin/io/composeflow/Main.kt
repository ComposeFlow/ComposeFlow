package io.composeflow

import dev.adamko.kxstsgen.KxsTsGenerator
import io.composeflow.ai.AiResponse
import io.composeflow.ai.CreateProjectAiResponse
import io.composeflow.model.project.Project
import io.composeflow.model.project.string.StringResourceUpdate
import java.io.File

fun main() {
    println("Generating type script for ComposeFlow project...")
    val generator = KxsTsGenerator()
    val fileName = "composeflow.d.ts"
    File(fileName).writeText(generator.generate(Project.serializer(), StringResourceUpdate.serializer()))
    println("Project typescript file is generated at $fileName")

    val aiResponseFileName = "airesponse.d.ts"
    File(aiResponseFileName).writeText(generator.generate(AiResponse.serializer()))
    println("AiResponse typescript file is generated at $aiResponseFileName")

    val createProjectAiResponseFileName = "create_project_airesponse.d.ts"
    File(createProjectAiResponseFileName).writeText(generator.generate(CreateProjectAiResponse.serializer()))
    println("CreateProjectAiResponse typescript file is generated at $createProjectAiResponseFileName")
}
