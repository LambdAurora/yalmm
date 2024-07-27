package yalmm.task.setup.download

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import yalmm.Constants
import yalmm.data.meta.version.VersionManifest
import yalmm.task.DefaultYalmmTask
import yalmm.util.Downloader
import yalmm.util.FileUtils
import java.io.File
import java.nio.file.Files
import java.util.*

open class DownloadGameArtifactTask : DefaultYalmmTask(Constants.Groups.SETUP) {
	companion object {
		const val DOWNLOAD_CLIENT_JAR_TASK_NAME = "downloadGameClientJar"
		const val DOWNLOAD_CLIENT_MAPPINGS_TASK_NAME = "downloadGameClientMappings"
		const val DOWNLOAD_SERVER_TASK_NAME = "downloadGameServerJar"
		const val DOWNLOAD_SERVER_MAPPINGS_TASK_NAME = "downloadGameServerMappings"
	}

	@Input
	val name: Property<String> = this.project.objects.property(String::class.java)

	@Input
	val fileName: Property<String> = this.project.objects.property(String::class.java)

	@OutputFile
	val artifactFile: RegularFileProperty = this.project.objects.fileProperty().fileProvider(this.fileName.map {
		this.fileConstants.mcVersionDir
			.resolve("artifacts")
			.resolve(it)
			.toFile()
	})

	private val manifestFile: File
	private var version: Optional<VersionManifest>?

	init {
		this.dependsOn(DownloadVersionManifestTask.TASK_NAME)
		this.manifestFile = this.getTaskByName<DownloadVersionManifestTask>(DownloadVersionManifestTask.TASK_NAME).versionFile
		this.version = this.getVersionManifest()

		this.outputs.upToDateWhen {
			try {
				this.artifactFile.get().asFile.exists() && FileUtils.validateSha1(
					this.artifactFile.get().asFile.toPath(),
					this.getVersionManifest().map { it.downloads[this.name.get()].sha1() }.get()
				)
			} catch (exception: Exception) {
				false
			}
		}
	}

	@TaskAction
	fun run() {
		val name = this.name.get()

		this.logger.lifecycle("Downloading Minecraft ${Constants.MINECRAFT_VERSION} ${name} artifact.")

		val artifact = this.getVersionManifest().get().downloads[name]
		Downloader(this)
			.src(artifact.url())
			.dest(this.artifactFile.get().asFile)
			.overwrite(false)
			.download()

		FileUtils.validateSha1(
			this.artifactFile.get().asFile.toPath(),
			artifact.sha1()
		)
	}

	private fun getVersionManifest(): Optional<VersionManifest> {
		if (this.version != null && this.version!!.isPresent) {
			return this.version!!
		}

		this.version = if (this.manifestFile.exists())
			Optional.of(VersionManifest.fromString(Files.readString(this.manifestFile.toPath())))
		else Optional.empty()
		return this.version!!
	}
}
