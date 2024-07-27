package yalmm.task.setup.download

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import yalmm.Constants
import yalmm.task.DefaultYalmmTask
import yalmm.util.Downloader
import java.nio.file.Files
import java.nio.file.StandardCopyOption

open class DownloadMappingsTask : DefaultYalmmTask(Constants.Groups.SETUP) {
	@Input
	val mappingsName: Property<String> = project.objects.property(String::class.java)

	@OutputFile
	val jarFile: RegularFileProperty = project.objects.fileProperty()

	@OutputFile
	val tinyFile: RegularFileProperty = project.objects.fileProperty()

	init {
		this.jarFile.fileProvider(mappingsName.map {
			this.fileConstants.mcVersionDir.resolve("artifacts").resolve(getMappingsFileName(it) + ".jar").toFile()
		})
		this.tinyFile.fileProvider(mappingsName.map {
			this.fileConstants.mcVersionDir.resolve("artifacts").resolve(getMappingsFileName(it) + ".tiny").toFile()
		})
	}

	@TaskAction
	fun downloadMappings() {
		Downloader(this)
			.src(this.project.configurations.getByName(this.mappingsName.get()).resolve().iterator().next().toURI().toString())
			.dest(this.jarFile.get().asFile)
			.overwrite(false)
			.download()

		Files.copy(
			this.project.zipTree(this.jarFile.get())
				.files.stream()
				.filter { it.name.endsWith("mappings.tiny") }
				.findFirst().get().toPath(),
			this.tinyFile.get().asFile.toPath(),
			StandardCopyOption.REPLACE_EXISTING
		)
	}

	companion object {
		private fun getMappingsFileName(mappingsName: String): String {
			return mappingsName.replace(Regex("[a-z][A-Z]")) {
				it.value[0] + "_" + Character.toLowerCase(it.value[1])
			}
		}
	}
}