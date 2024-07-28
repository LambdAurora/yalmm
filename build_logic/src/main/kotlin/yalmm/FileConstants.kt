package yalmm

import org.gradle.api.Project
import java.nio.file.Path

class FileConstants(project: Project) {
	val mcCacheDir: Path
	val librariesDir: Path
	val mcVersionDir: Path
	val mappingsDir: Path

	init {
		val dotGradleDir = project.file(".gradle")
		this.mcCacheDir = dotGradleDir.resolve("minecraft").toPath()
		this.librariesDir = this.mcCacheDir.resolve("libraries")
		this.mcVersionDir = this.mcCacheDir.resolve(Constants.MINECRAFT_VERSION)
		this.mappingsDir = project.file("mappings").toPath()
	}
}
