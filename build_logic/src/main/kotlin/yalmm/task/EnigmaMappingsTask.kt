package yalmm.task

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.JavaExec
import yalmm.Constants

open class EnigmaMappingsTask : JavaExec() {
	companion object {
		const val TASK_NAME = "enigma"
	}

	@InputFile
	val jarToMap: RegularFileProperty = this.project.objects.fileProperty()

	init {
		this.group = Constants.Groups.MAPPINGS
		this.mainClass.set("cuchaz.enigma.gui.Main")
		this.classpath(this.project.configurations.getByName("enigmaRuntime"))
		this.jvmArgs("-Xmx2048M")
	}

	override fun exec() {
		this.args(
			listOf(
				"-jar", this.jarToMap.get().asFile.absolutePath,
				"-mappings", this.project.file("mappings").absolutePath
			)
		)
		super.exec()
	}
}
