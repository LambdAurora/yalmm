package yalmm.task.compile

import cuchaz.enigma.Enigma
import cuchaz.enigma.EnigmaProfile
import cuchaz.enigma.ProgressListener
import cuchaz.enigma.command.MappingCommandsUtil
import cuchaz.enigma.translation.MappingTranslator
import cuchaz.enigma.translation.Translator
import cuchaz.enigma.translation.mapping.EntryMapping
import cuchaz.enigma.translation.mapping.serde.MappingFileNameFormat
import cuchaz.enigma.translation.mapping.serde.MappingSaveParameters
import cuchaz.enigma.translation.mapping.tree.EntryTree
import cuchaz.enigma.translation.representation.entry.ClassEntry
import cuchaz.enigma.utils.Utils
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import yalmm.Constants
import yalmm.enigma.MappingEmitter
import yalmm.enigma.YalmmEnigmaPlugin
import yalmm.mapping.InnerClassSolver
import yalmm.task.DefaultYalmmTask
import yalmm.task.setup.mapping.MapGameJarTask
import yalmm.util.VerboseConsoleProgressListener
import java.io.File
import java.util.stream.Collectors

open class BuildBaseMappingsTinyTask : DefaultYalmmTask(Constants.Groups.BUILD) {
	companion object {
		const val TASK_NAME = "buildBaseMappingsTiny"
	}

	@InputDirectory
	val mappings: RegularFileProperty = project.objects.fileProperty()

	@InputFile
	val jarToMap: RegularFileProperty = this.project.objects.fileProperty()

	@InputFile
	val enigmaProfile: RegularFileProperty = project.objects.fileProperty()

	@OutputFile
	val outputMappings: File = this.project.layout.buildDirectory.file("yalmm_base.tiny").get().asFile

	init {
		this.dependsOn(MapGameJarTask.TASK_NAME)
		this.mappings.convention { this.fileConstants.mappingsDir.toFile() }
		this.enigmaProfile.convention(this.project.layout.projectDirectory.file("enigma_profile.json"))
	}

	@TaskAction
	fun run() {
		this.logger.info("Generating Tiny v2 mappings.")

		val plugin = YalmmEnigmaPlugin()
		val profile = EnigmaProfile.read(this.enigmaProfile.get().asFile.toPath())
		val enigma = Enigma.builder()
			.setProfile(profile)
			.setPlugins(listOf(plugin))
			.build()

		// Based on MapSpecializedMethodsCommand from Enigma CLI.
		// But modified to not include unmapped specialized methods.

		val progress: ProgressListener = VerboseConsoleProgressListener()
		val project = MappingEmitter.openProject(
			enigma,
			this.jarToMap.get().asFile.toPath(),
			this.mappings.get().asFile.toPath(),
			progress
		)

		val emitter = MappingEmitter(project, plugin)
		emitter.fillMappings()
		val result: EntryTree<EntryMapping?> = emitter.result()
		plugin.newMappingEmitter(project).fillMappings(result)

		val bridgeMethodIndex = project.jarIndex.bridgeMethodIndex
		val translator: Translator = MappingTranslator(project.mapper.obfToDeobf, project.jarIndex.entryResolver)

		// Add inner classes that are not present in the result mapping tree.
		val innerClassesMap = project.jarIndex.entryIndex.classes.parallelStream()
			.filter { it.isInnerClass }
			.sorted(Comparator.comparing { it.fullName })
			.collect(Collectors.toMap(ClassEntry::getOutermostClass, { listOf(it) }, { a, b -> a + b }))
		for ((outMostParent, innerClasses) in innerClassesMap) {
			val parentSolver = InnerClassSolver(null, outMostParent);
			val knownOutMostName = result.get(outMostParent)?.targetName

			if (knownOutMostName != null) {
				parentSolver.setKnownName(knownOutMostName)
			}

			for (innerClass in innerClasses) {
				val hierarchy = listHierarchy(innerClass)
				var currentParent = parentSolver

				for (part in hierarchy) {
					var partSolver = currentParent.innerClasses[currentParent.node()]

					if (partSolver == null) {
						partSolver = InnerClassSolver(currentParent, part)

						val currentKnownOutMostName = result.get(part)?.targetName
						if (currentKnownOutMostName != null) {
							partSolver.setKnownName(currentKnownOutMostName)
						}

						currentParent.innerClasses[part] = partSolver
					}

					currentParent = parentSolver
				}
			}

			if (parentSolver.shouldResolve()) {
				parentSolver.apply(result)
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
		MappingCommandsUtil.write(result, "tinyv2:mojang_named:named", output, MappingSaveParameters(MappingFileNameFormat.BY_DEOBF))
	}

	private fun listHierarchy(node: ClassEntry): List<ClassEntry> {
		val list = ArrayList<ClassEntry>()
		var currentNode: ClassEntry? = node
		while (currentNode?.outerClass != null) {
			list.add(currentNode)
			currentNode = currentNode.outerClass
		}
		return list.reversed()
	}
}
