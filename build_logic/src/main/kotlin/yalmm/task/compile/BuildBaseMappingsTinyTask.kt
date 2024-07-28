package yalmm.task.compile

import cuchaz.enigma.ProgressListener
import cuchaz.enigma.analysis.index.JarIndex
import cuchaz.enigma.classprovider.CachingClassProvider
import cuchaz.enigma.classprovider.JarClassProvider
import cuchaz.enigma.command.MappingCommandsUtil
import cuchaz.enigma.translation.MappingTranslator
import cuchaz.enigma.translation.Translator
import cuchaz.enigma.translation.mapping.EntryMapping
import cuchaz.enigma.translation.mapping.serde.MappingFileNameFormat
import cuchaz.enigma.translation.mapping.serde.MappingSaveParameters
import cuchaz.enigma.translation.mapping.tree.EntryTree
import cuchaz.enigma.translation.mapping.tree.HashEntryTree
import cuchaz.enigma.translation.representation.entry.MethodEntry
import cuchaz.enigma.utils.Utils
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import yalmm.Constants
import yalmm.task.DefaultYalmmTask
import yalmm.task.setup.mapping.MapGameJarTask
import java.io.File

open class BuildBaseMappingsTinyTask : DefaultYalmmTask(Constants.Groups.BUILD) {
	companion object {
		const val TASK_NAME = "buildBaseMappingsTiny"
	}

	@InputDirectory
	val mappings: RegularFileProperty = project.objects.fileProperty()

	@OutputFile
	val outputMappings: File = this.project.layout.buildDirectory.file("yalmm_base.tiny").get().asFile

	init {
		this.dependsOn(MapGameJarTask.TASK_NAME)
		this.mappings.convention { this.fileConstants.mappingsDir.toFile() }
	}

	@TaskAction
	fun run() {
		this.logger.info("Generating Tiny v2 mappings.")

		// Based on MapSpecializedMethodsCommand from Enigma CLI.
		// But modified to not include unmapped specialized methods.

		val saveParameters = MappingSaveParameters(MappingFileNameFormat.BY_DEOBF)
		val source = MappingCommandsUtil.read("enigma", this.mappings.get().asFile.toPath(), saveParameters)
		val result: EntryTree<EntryMapping?> = HashEntryTree()

		val jcp = JarClassProvider(this.getTaskByName<MapGameJarTask>(MapGameJarTask.TASK_NAME).outputJar.get().asFile.toPath())
		val jarIndex = JarIndex.empty()
		jarIndex.indexJar(jcp.classNames, CachingClassProvider(jcp), ProgressListener.none())

		val bridgeMethodIndex = jarIndex.bridgeMethodIndex
		val translator: Translator = MappingTranslator(source, jarIndex.entryResolver)

		// Copy all non-specialized methods
		for (node in source) {
			if (node.entry !is MethodEntry || !bridgeMethodIndex.isSpecializedMethod(node.entry as MethodEntry)) {
				result.insert(node.entry, node.value)
			}
		}

		// Add correct mappings for specialized methods
		for ((bridge, specialized) in bridgeMethodIndex.bridgeToSpecialized) {
			val translated = translator.extendedTranslate(bridge)!!

			if (translated.isProposed) {
				// Only add if the bridge is also mapped.
				result.insert(specialized, EntryMapping(translated.value.name))
			}
		}

		val output = this.outputMappings.toPath()
		Utils.delete(output)
		MappingCommandsUtil.write(result, "tinyv2:mojang_named:named", output, saveParameters)
	}
}
