package yalmm.task.setup.download

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import yalmm.Constants
import yalmm.data.meta.version.VersionManifest
import yalmm.task.DefaultYalmmTask
import yalmm.util.Downloader
import yalmm.util.FileUtils
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption

open class GatherMinecraftLibrariesTask : DefaultYalmmTask(Constants.Groups.SETUP) {
	companion object {
		const val TASK_NAME = "gatherMinecraftLibraries"
	}

	@InputFile
	val versionFile: RegularFileProperty = this.project.objects.fileProperty()

	@InputFile
	val serverBootstrapJar: RegularFileProperty = this.project.objects.fileProperty()

	init {
		this.dependsOn(DownloadVersionManifestTask.TASK_NAME, DownloadGameArtifactTask.DOWNLOAD_SERVER_TASK_NAME)

		this.versionFile.convention(this.getTaskByName<DownloadVersionManifestTask>(DownloadVersionManifestTask.TASK_NAME)::versionFile)
		this.serverBootstrapJar.fileProvider(
			this.getTaskByName<DownloadGameArtifactTask>(DownloadGameArtifactTask.DOWNLOAD_SERVER_TASK_NAME).artifactFile.map { it.asFile }
		)

		this.outputs.dir(this.fileConstants.librariesDir.toFile())
		this.outputs.upToDateWhen { false }
	}

	@TaskAction
	fun run() {
		val version = VersionManifest.fromString(Files.readString(this.versionFile.get().asFile.toPath()))

		this.logger.lifecycle("Gathering Minecraft libraries...")

		Files.createDirectories(this.fileConstants.librariesDir)

		val extractedLibraries = this.extractBundledLibraries().map { it.name }

		version.libraries.parallelStream().filter {
			// We only download libraries that are either not available locally and that are required at compile time.
			// Any libraries with rules can be considered a runtime-only library.
			!extractedLibraries.contains(it.name) && it.rules == null
		}.forEach {
			val artifact = it.downloads.artifact

			Downloader(this)
				.src(artifact.url())
				.dest(this.fileConstants.librariesDir.resolve(artifact.path()).toFile())
				.overwrite(false)
				.download()
		}
	}

	/**
	 * Extract the bundled libraries from the server bootstrap JAR.
	 *
	 * This allows to speed up the setup process by not downloading already available libraries.
	 *
	 * @return the extracted libraries
	 */
	private fun extractBundledLibraries(): List<LibraryEntry> {
		FileSystems.newFileSystem(this.serverBootstrapJar.get().asFile.toPath()).use {
			val librariesListPath = it.getPath("/META-INF/libraries.list")

			val libraries = Files.readAllLines(librariesListPath).stream()
				.filter { entry -> entry.isNotEmpty() }
				.map { entry -> entry.split("\t") }
				.map { entry -> LibraryEntry(entry[1], entry[2], entry[0]) }
				.toList()

			libraries.parallelStream().forEach { entry ->
				val inJarLibraryPath = it.getPath("/META-INF/libraries/${entry.path}")
				val libraryPath = this.fileConstants.librariesDir.resolve(entry.path)

				if (!(Files.exists(libraryPath) && FileUtils.validateSha1(libraryPath, entry.sha1))) {
					Files.createDirectories(libraryPath.parent)

					Files.copy(inJarLibraryPath, libraryPath, StandardCopyOption.REPLACE_EXISTING)
				}
			}

			return libraries
		}
	}

	private data class LibraryEntry(val name: String, val path: String, val sha1: String) {}
}
