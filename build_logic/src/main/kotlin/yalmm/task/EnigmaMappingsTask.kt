package yalmm.task

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.UntrackedTask
import yalmm.Constants
import yalmm.enigma.YalmmEnigmaPlugin
import java.io.File

@UntrackedTask(because = "Task needs to always run when asked.")
open class EnigmaMappingsTask : JavaExec() {
	companion object {
		const val TASK_NAME = "enigma"
	}

	@InputFile
	val jarToMap: RegularFileProperty = this.project.objects.fileProperty()

	@InputDirectory
	val mappingsDir: DirectoryProperty = project.objects.directoryProperty()

	@InputFile
	val enigmaProfileFile: RegularFileProperty = this.project.objects.fileProperty()

	init {
		this.group = Constants.Groups.MAPPINGS
		this.mainClass.set("cuchaz.enigma.gui.Main")

		val selfCodeSource = YalmmEnigmaPlugin::class.java.protectionDomain.codeSource
		val selfJarFile = File(selfCodeSource.location.file)

		val runtimeClassPath = this.project.files()
		runtimeClassPath.from(this.project.configurations.getByName("enigmaRuntime"))
		runtimeClassPath.from(selfJarFile)
		this.classpath(runtimeClassPath)

		this.jvmArgs("-Xmx2048M")

		this.mappingsDir.convention(this.project.layout.projectDirectory.dir("mappings"))
		this.enigmaProfileFile.convention(this.project.layout.projectDirectory.file("enigma_profile.json"))
	}

	override fun exec() {
		this.args(
			listOf(
				"-jar", this.jarToMap.get().asFile.absolutePath,
				"-mappings", this.mappingsDir.get().asFile.absolutePath,
				"-profile", this.enigmaProfileFile.get().asFile.absolutePath,
			)
		)
		super.exec()
	}
}
