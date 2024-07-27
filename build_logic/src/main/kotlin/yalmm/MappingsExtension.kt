package yalmm

import org.gradle.api.Project

class MappingsExtension(project: Project) {
	val fileConstants: FileConstants = FileConstants(project)

	companion object {
		fun get(project: Project): MappingsExtension {
			return project.extensions.getByType(MappingsExtension::class.java)
		}
	}
}
