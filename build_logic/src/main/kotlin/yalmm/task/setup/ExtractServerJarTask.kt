package yalmm.task.setup

import org.gradle.api.GradleException
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import yalmm.Constants
import yalmm.task.DefaultYalmmTask
import yalmm.task.setup.download.DownloadGameArtifactTask
import java.io.File
import java.nio.file.FileSystems
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
		FileSystems.newFileSystem(this.serverBootstrapJar.toPath()).use { fs ->
			val matcher = fs.getPathMatcher("glob:/META-INF/versions/*/server-*.jar")

			val serverJar = Files.walk(fs.getPath("/META-INF/versions")).filter { matcher.matches(it) }
				.findFirst()

			if (serverJar.isEmpty) {
				throw GradleException("Server JAR could not be found.")
			}

			Files.copy(
				serverJar.get(),
				this.serverJar.toPath(),
				StandardCopyOption.REPLACE_EXISTING
			)
		}
	}
}
