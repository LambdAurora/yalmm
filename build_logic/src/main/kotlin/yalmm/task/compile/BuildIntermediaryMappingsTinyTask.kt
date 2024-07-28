package yalmm.task.compile

import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.MappingWriter
import net.fabricmc.mappingio.adapter.MappingNsCompleter
import net.fabricmc.mappingio.adapter.MappingNsRenamer
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch
import net.fabricmc.mappingio.format.MappingFormat
import net.fabricmc.mappingio.tree.MemoryMappingTree
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import yalmm.Constants
import yalmm.mapping.NamespaceFilterMappingVisitor
import yalmm.mapping.YalmmMappingVisitor
import yalmm.task.DefaultYalmmTask
import yalmm.task.setup.download.DownloadMappingsTask
import yalmm.task.setup.mapping.BuildMojangTinyTask
import java.io.File

open class BuildIntermediaryMappingsTinyTask : DefaultYalmmTask(Constants.Groups.BUILD) {
	companion object {
		const val TASK_NAME = "buildIntermediaryMappingsTiny"
	}

	@InputFile
	val mappings: RegularFileProperty = project.objects.fileProperty()

	@InputFile
	val mojangTiny: RegularFileProperty = project.objects.fileProperty()

	@InputFile
	val intermediaryTiny: RegularFileProperty = project.objects.fileProperty()

	@OutputFile
	val outputMappings: File = this.project.layout.buildDirectory.file("generated/mappings/mappings.tiny").get().asFile

	init {
		this.dependsOn(BuildBaseMappingsTinyTask.TASK_NAME, BuildMojangTinyTask.TASK_NAME, "downloadIntermediary")
		this.mappings.convention { this.getTaskByName<BuildBaseMappingsTinyTask>(BuildBaseMappingsTinyTask.TASK_NAME).outputMappings }
		this.mojangTiny.convention { this.getTaskByName<BuildMojangTinyTask>(BuildMojangTinyTask.TASK_NAME).tinyFile }
		this.intermediaryTiny.convention { this.getTaskByName<DownloadMappingsTask>("downloadIntermediary").tinyFile.get().asFile }
	}

	@TaskAction
	fun run() {
		this.logger.info("Generating Intermediary Tiny v2 mappings.")

		val officialToKnown = MemoryMappingTree()

		MappingReader.read(this.intermediaryTiny.get().asFile.toPath(), officialToKnown)
		MappingReader.read(this.mojangTiny.get().asFile.toPath(), MappingNsRenamer(officialToKnown, mapOf("named" to "mojang_named")))

		val mojmapToIntermediary = MemoryMappingTree()
		val nsToIntermediary = MappingSourceNsSwitch(mojmapToIntermediary, "mojang_named")
		officialToKnown.accept(nsToIntermediary)

		val yalmmMapping = MemoryMappingTree()

		MappingReader.read(this.mappings.get().asFile.toPath(), YalmmMappingVisitor(yalmmMapping, mojmapToIntermediary))

		MappingWriter.create(this.outputMappings.toPath(), MappingFormat.TINY_2_FILE).use {
			yalmmMapping.accept(
				MappingSourceNsSwitch(
					NamespaceFilterMappingVisitor(
						MappingNsCompleter(it, mapOf("named" to "intermediary"))
					),
					"intermediary"
				)
			)
		}
	}
}
