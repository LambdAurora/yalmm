package yalmm.task

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.JavaExec
import yalmm.Constants
import yalmm.enigma.YalmmEnigmaPlugin
import java.io.File

open class EnigmaMappingsTask : JavaExec() {
	companion object {
		const val TASK_NAME = "enigma"
	}

	@InputFile
	val jarToMap: RegularFileProperty = this.project.objects.fileProperty()

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
	}

	override fun exec() {
		this.args(
			listOf(
				"-jar", this.jarToMap.get().asFile.absolutePath,
				"-mappings", this.project.file("mappings").absolutePath,
				"-profile", this.project.file("enigma_profile.json").absolutePath,
			)
		)
		super.exec()
	}
}
