package yalmm.task.setup.download

import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import yalmm.Constants
import yalmm.task.DefaultYalmmTask
import yalmm.util.Downloader
import java.io.File

open class DownloadVersionsManifestTask : DefaultYalmmTask(Constants.Groups.SETUP) {
	companion object {
		const val TASK_NAME = "downloadVersionsManifest"
		private const val FILE_NAME = "version_manifest_v2.json"
	}

	@OutputFile
	val manifestFile: File = this.fileConstants.mcCacheDir.resolve(FILE_NAME).toFile()

	@TaskAction
	fun run() {
		this.logger.lifecycle("Downloading Minecraft versions manifest.")
		Downloader(this)
			.src("https://piston-meta.mojang.com/mc/game/$FILE_NAME")
			.dest(this.manifestFile)
			.overwrite(true)
			.download()
	}
}
