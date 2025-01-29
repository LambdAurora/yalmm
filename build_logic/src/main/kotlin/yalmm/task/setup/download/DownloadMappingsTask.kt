package yalmm.task.setup.download

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import yalmm.Constants
import yalmm.task.DefaultYalmmTask
import yalmm.util.Downloader
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.TimeUnit
import java.util.stream.StreamSupport
import kotlin.io.path.name

open class DownloadMappingsTask : DefaultYalmmTask(Constants.Groups.SETUP) {
	@Input
	val mappingsName: Property<String> = project.objects.property(String::class.java)

	@Input
	val mappingsUrl: Property<String> = project.objects.property(String::class.java)

	@OutputFile
	val jarFile: RegularFileProperty = project.objects.fileProperty()

	@OutputFile
	val tinyFile: RegularFileProperty = project.objects.fileProperty()

	@Suppress("LeakingThis")
	private val action = Downloader(this)
		.overwrite(false)

	init {
		this.mappingsUrl.convention(this.mappingsName.map {
			this.project.configurations.getByName(it).resolve().iterator().next().toURI().toString()
		})
		this.jarFile.fileProvider(mappingsName.map {
			this.fileConstants.mcVersionDir.resolve("artifacts").resolve(getMappingsFileName(it) + ".jar").toFile()
		})
		this.tinyFile.fileProvider(mappingsName.map {
			this.fileConstants.mcVersionDir.resolve("artifacts").resolve(getMappingsFileName(it) + ".tiny").toFile()
		})
	}

	@TaskAction
	fun downloadMappings() {
		this.action
			.src(this.mappingsUrl.get())
			.dest(this.jarFile.get().asFile)
			.download()
			.thenRun {
				FileSystems.newFileSystem(this.jarFile.asFile.get().toPath()).use { fs ->
					val mappingsFile = StreamSupport.stream(fs.rootDirectories.spliterator(), false)
						.flatMap { Files.walk(it) }
						.filter { it.name.endsWith("mappings.tiny") }
						.findFirst().get()

					Files.copy(
						mappingsFile,
						this.tinyFile.get().asFile.toPath(),
						StandardCopyOption.REPLACE_EXISTING
					)
				}
			}.orTimeout(10, TimeUnit.MINUTES)
	}

	companion object {
		private fun getMappingsFileName(mappingsName: String): String {
			return mappingsName.replace(Regex("[a-z][A-Z]")) {
				it.value[0] + "_" + Character.toLowerCase(it.value[1])
			}
		}
	}
}