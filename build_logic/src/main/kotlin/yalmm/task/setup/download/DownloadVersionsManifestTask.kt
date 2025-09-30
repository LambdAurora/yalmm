package yalmm.task.setup.download

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import yalmm.Constants
import yalmm.task.DefaultYalmmTask
import yalmm.util.Downloader
import java.io.File
import java.util.concurrent.TimeUnit

abstract class DownloadVersionsManifestTask : DefaultYalmmTask(Constants.Groups.SETUP) {
	companion object {
		const val TASK_NAME = "downloadVersionsManifest"
		private const val FILE_NAME = "version_manifest_v2.json"
	}

	@get:Input
	abstract val targetVersion: Property<String>

	@Suppress("LeakingThis")
	private val action = Downloader(this)
		.src("https://piston-meta.mojang.com/mc/game/$FILE_NAME")
		.overwrite(true)

	@OutputFile
	val manifestFile: File = this.fileConstants.mcCacheDir.resolve(FILE_NAME).toFile()

	init {
		this.targetVersion.convention(Constants.getMinecraftVersion(this.project))
	}

	@TaskAction
	fun run() {
		this.logger.lifecycle("Downloading Minecraft versions manifest.")

		this.action
			.dest(this.manifestFile)
			.download()
			.orTimeout(1, TimeUnit.MINUTES)
	}
}
