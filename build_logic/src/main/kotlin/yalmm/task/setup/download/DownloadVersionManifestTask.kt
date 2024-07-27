package yalmm.task.setup.download

import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import yalmm.Constants
import yalmm.data.meta.VersionsManifest
import yalmm.task.DefaultYalmmTask
import yalmm.util.Downloader
import java.io.File
import java.nio.file.Files
import java.util.*

open class DownloadVersionManifestTask : DefaultYalmmTask(Constants.Groups.SETUP) {
	companion object {
		const val TASK_NAME = "downloadVersionManifest"
	}

	@OutputFile
	val versionFile: File = this.fileConstants.mcCacheDir.resolve(Constants.MINECRAFT_VERSION).resolve("manifest.json").toFile()

	private val manifestFile: File
	private val versionEntry: Optional<VersionsManifest.Entry>

	init {
		this.dependsOn(DownloadVersionsManifestTask.TASK_NAME)
		this.manifestFile = this.getTaskByName<DownloadVersionsManifestTask>(DownloadVersionsManifestTask.TASK_NAME).manifestFile
		this.versionEntry = this.getManifestVersion()

		this.inputs.property("versionsManifest", this.manifestFile)
		this.inputs.property("releaseTime", this.versionEntry.map { it.releaseTime }.orElse("-1"))
	}

	@TaskAction
	fun run() {
		this.logger.lifecycle("Downloading Minecraft ${Constants.MINECRAFT_VERSION} version manifest.")

		val entry = this.versionEntry.or { getManifestVersion() }

		if (entry.isPresent) {
			Downloader(this)
				.src(entry.get().url)
				.dest(this.versionFile)
				.overwrite(true)
				.download()
		} else if (!this.versionFile.exists()) {
			throw RuntimeException("Could not find version data for Minecraft " + Constants.MINECRAFT_VERSION)
		}
	}

	private fun getManifestVersion(): Optional<VersionsManifest.Entry> {
		val manifest = if (this.manifestFile.exists()) VersionsManifest.fromString(Files.readString(this.manifestFile.toPath())) else null

		return if (manifest != null) {
			manifest.versions.stream().filter { it.id.equals(Constants.MINECRAFT_VERSION) }.findFirst()
		} else {
			Optional.empty()
		}
	}
}
