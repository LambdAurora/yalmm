package yalmm.task.setup

import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import yalmm.Constants
import yalmm.task.DefaultYalmmTask
import yalmm.task.setup.download.DownloadGameArtifactTask
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

open class ExtractServerJarTask : DefaultYalmmTask(Constants.Groups.SETUP) {
	companion object {
		const val TASK_NAME = "extractServerJar"
	}

	@OutputFile
	val serverJar: File = this.fileConstants.mcVersionDir
		.resolve("artifacts")
		.resolve("server.jar")
		.toFile()

	private val serverBootstrapJar: File

	init {
		this.dependsOn(DownloadGameArtifactTask.DOWNLOAD_SERVER_TASK_NAME)
		this.serverBootstrapJar = this.getTaskByName<DownloadGameArtifactTask>(DownloadGameArtifactTask.DOWNLOAD_SERVER_TASK_NAME)
			.artifactFile.get().asFile
		this.inputs.files(this.serverBootstrapJar)
	}

	@TaskAction
	fun run() {
		Files.copy(
			this.project.zipTree(this.serverBootstrapJar)
				.matching { include("META-INF/versions/*/server-*.jar") }
				.singleFile
				.toPath(),
			this.serverJar.toPath(),
			StandardCopyOption.REPLACE_EXISTING
		)
	}
}
