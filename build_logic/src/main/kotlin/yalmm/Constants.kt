package yalmm

import org.gradle.api.Project

object Constants {
	fun getMinecraftVersion(project: Project): String {
		return project.property("minecraft_version").toString()
	}

	object Groups {
		const val SETUP = "setup"
		const val MAPPINGS = "mappings"
		const val BUILD = "build"
	}
}
