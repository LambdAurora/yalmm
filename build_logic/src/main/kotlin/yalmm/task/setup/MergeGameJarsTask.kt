package yalmm.task.setup

import net.fabricmc.stitch.merge.JarMerger
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import yalmm.Constants
import yalmm.task.DefaultYalmmTask
import yalmm.task.setup.download.DownloadGameArtifactTask
import java.io.File

open class MergeGameJarsTask : DefaultYalmmTask(Constants.Groups.SETUP) {
	companion object {
		const val TASK_NAME = "mergeGameJars"
	}

	@InputFile
	val clientJar: RegularFileProperty = this.project.objects.fileProperty()

	@InputFile
	val serverJar: RegularFileProperty = this.project.objects.fileProperty()

	@OutputFile
	val mergedJar: File = this.fileConstants.mcVersionDir
		.resolve("artifacts")
		.resolve("game.jar")
		.toFile()

	init {
		this.dependsOn(DownloadGameArtifactTask.DOWNLOAD_CLIENT_JAR_TASK_NAME, ExtractServerJarTask.TASK_NAME)
		this.clientJar.convention {
			this.getTaskByName<DownloadGameArtifactTask>(DownloadGameArtifactTask.DOWNLOAD_CLIENT_JAR_TASK_NAME).artifactFile.get().asFile
		}
		this.serverJar.convention { this.getTaskByName<ExtractServerJarTask>(ExtractServerJarTask.TASK_NAME).serverJar }
	}

	@TaskAction
	fun run() {
		this.logger.lifecycle("Merging game JARs.")

		if (this.mergedJar.exists()) {
			this.logger.lifecycle("> SKIPPING")
			return
		}

		JarMerger(this.clientJar.get().asFile, this.serverJar.get().asFile, this.mergedJar).use {
			it.merge()
		}
	}
}
