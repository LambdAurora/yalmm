package yalmm.task.setup.mapping

import net.fabricmc.mappingio.MappingWriter
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch
import net.fabricmc.mappingio.format.MappingFormat
import net.fabricmc.mappingio.format.proguard.ProGuardFileReader
import net.fabricmc.mappingio.tree.MemoryMappingTree
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import yalmm.Constants
import yalmm.task.DefaultYalmmTask
import yalmm.task.setup.download.DownloadGameArtifactTask
import java.io.File
import java.nio.file.Files

open class BuildMojangTinyTask : DefaultYalmmTask(Constants.Groups.SETUP) {
	companion object {
		const val TASK_NAME = "buildMojangTiny"
	}

	@InputFile
	val clientMappingsFile: RegularFileProperty = project.objects.fileProperty()

	@InputFile
	val serverMappingsFile: RegularFileProperty = project.objects.fileProperty()

	@OutputFile
	val tinyFile: File = this.fileConstants.mcVersionDir.resolve("artifacts").resolve("mojmap.tiny").toFile()

	init {
		this.dependsOn(DownloadGameArtifactTask.DOWNLOAD_CLIENT_MAPPINGS_TASK_NAME, DownloadGameArtifactTask.DOWNLOAD_SERVER_MAPPINGS_TASK_NAME)
		this.clientMappingsFile.convention {
			this.getTaskByName<DownloadGameArtifactTask>(DownloadGameArtifactTask.DOWNLOAD_CLIENT_MAPPINGS_TASK_NAME).artifactFile.get().asFile
		}
		this.serverMappingsFile.convention {
			this.getTaskByName<DownloadGameArtifactTask>(DownloadGameArtifactTask.DOWNLOAD_SERVER_MAPPINGS_TASK_NAME).artifactFile.get().asFile
		}
	}

	@TaskAction
	fun buildTiny() {
		val mappingTree = MemoryMappingTree()

		val nsSwitch = MappingSourceNsSwitch(mappingTree, "official")

		Files.newBufferedReader(this.clientMappingsFile.get().asFile.toPath()).use { clientReader ->
			Files.newBufferedReader(this.serverMappingsFile.get().asFile.toPath()) .use { serverReader ->
				ProGuardFileReader.read(clientReader, "named", "official", nsSwitch)
				ProGuardFileReader.read(serverReader, "named", "official", nsSwitch)
			}
		}

		MappingWriter.create(this.tinyFile.toPath(), MappingFormat.TINY_2_FILE).use {
			mappingTree.accept(it)
		}
	}
}
