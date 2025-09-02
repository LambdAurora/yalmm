package yalmm.task.setup.download

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import yalmm.Constants
import yalmm.data.meta.VersionsManifest
import yalmm.task.DefaultYalmmTask
import yalmm.util.Downloader
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.concurrent.TimeUnit

abstract class DownloadVersionManifestTask : DefaultYalmmTask(Constants.Groups.SETUP) {
	companion object {
		const val TASK_NAME = "downloadVersionManifest"
	}

	@get:Input
	abstract val targetVersion: Property<String>

	@OutputFile
	val versionFile: File = this.fileConstants.mcCacheDir.resolve(Constants.getMinecraftVersion(this.project)).resolve("manifest.json").toFile()

	private val manifestFile: File
	private val versionEntry: Optional<VersionsManifest.Entry>
	@Suppress("LeakingThis")
	private val action = Downloader(this)
		.overwrite(true)

	init {
		this.dependsOn(DownloadVersionsManifestTask.TASK_NAME)
		this.targetVersion.convention(Constants.getMinecraftVersion(this.project))
		this.manifestFile = this.getTaskByName<DownloadVersionsManifestTask>(DownloadVersionsManifestTask.TASK_NAME).manifestFile
		this.versionEntry = this.getManifestVersion()

		this.inputs.property("versionsManifest", this.manifestFile)
		this.inputs.property("releaseTime", this.versionEntry.map { it.releaseTime }.orElse("-1"))
	}

	@TaskAction
	fun run() {
		this.logger.lifecycle("Downloading Minecraft ${this.targetVersion.get()} version manifest.")

		val entry = this.versionEntry.or { getManifestVersion() }

		if (entry.isPresent) {
			this.action
				.src(entry.get().url)
				.dest(this.versionFile)
				.download()
				.orTimeout(1, TimeUnit.MINUTES)
		} else if (!this.versionFile.exists()) {
			throw RuntimeException("Could not find version data for Minecraft " + this.targetVersion.get())
		}
	}

	private fun getManifestVersion(): Optional<VersionsManifest.Entry> {
		val manifest = if (this.manifestFile.exists()) VersionsManifest.fromString(Files.readString(this.manifestFile.toPath())) else null

		return if (manifest != null) {
			manifest.versions.stream().filter { it.id.equals(this.targetVersion.get()) }.findFirst()
		} else {
			Optional.empty()
		}
	}
}
