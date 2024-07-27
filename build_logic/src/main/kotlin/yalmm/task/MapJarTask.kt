package yalmm.task

import net.fabricmc.tinyremapper.OutputConsumerPath
import net.fabricmc.tinyremapper.TinyRemapper
import net.fabricmc.tinyremapper.TinyUtils
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import yalmm.task.setup.download.GatherMinecraftLibrariesTask
import java.io.IOException
import java.nio.file.Files
import java.util.regex.Pattern


open class MapJarTask(group: String, private val from: String, private val to: String) : DefaultYalmmTask(group) {
	@InputFile
	val inputJar: RegularFileProperty = project.objects.fileProperty()

	@InputFile
	val mappingsFile: RegularFileProperty = project.objects.fileProperty()

	@OutputFile
	val outputJar: RegularFileProperty = project.objects.fileProperty()

	init {
		this.dependsOn(GatherMinecraftLibrariesTask.TASK_NAME)
	}

	@TaskAction
	fun remapJar() {
		this.logger.lifecycle("Mapping game JAR from ${this.from} to ${this.to}.")

		val inputPath = this.inputJar.get().asFile.toPath()
		val outputJar = this.outputJar.get().asFile.toPath()

		this.logger.lifecycle("  Input JAR: $inputPath")
		this.logger.lifecycle("  Mappings: ${this.mappingsFile.get().asFile}")

		if (Files.exists(outputJar)) {
			try {
				Files.deleteIfExists(outputJar)
			} catch (e: IOException) {
				throw RuntimeException("Failed to delete the existing output file", e)
			}
		}

		val remapper = TinyRemapper.newRemapper()
			.withMappings(TinyUtils.createTinyMappingProvider(this.mappingsFile.get().asFile.toPath(), this.from, this.to))
			.removeFrames(false)
			.ignoreConflicts(true)
			.resolveMissing(false)
			.checkPackageAccess(false)
			.fixPackageAccess(false)
			.rebuildSourceFilenames(true)
			.skipLocalVariableMapping(false)
			.renameInvalidLocals(true)
			.invalidLvNamePattern(Pattern.compile("\\$\\$\\d+|c_[a-z]{8}"))
			.inferNameFromSameLvIndex(false)
			.build()

		OutputConsumerPath.Builder(this.outputJar.get().asFile.toPath()).build().use {
			it.addNonClassFiles(inputPath)
			remapper.readInputs(inputPath)

			remapper.readClassPath(this.fileConstants.librariesDir)

			remapper.apply(it)

			remapper.finish()
		}
	}

	companion object {
		val JAVAX_TO_JETBRAINS = mapOf(
			"javax/annotation/Nullable" to "org/jetbrains/annotations/Nullable",
			"javax/annotation/Nonnull" to "org/jetbrains/annotations/NotNull",
			"javax/annotation/concurrent/Immutable" to "org/jetbrains/annotations/Unmodifiable"
		)
	}
}
