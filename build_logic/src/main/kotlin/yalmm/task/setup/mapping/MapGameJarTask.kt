package yalmm.task.setup.mapping

import yalmm.Constants
import yalmm.task.MapJarTask
import yalmm.task.setup.MergeGameJarsTask

open class MapGameJarTask : MapJarTask(Constants.Groups.SETUP, "official", "named") {
	companion object {
		const val TASK_NAME = "mapGameJar"
	}

	init {
		this.dependsOn(MergeGameJarsTask.TASK_NAME, BuildMojangTinyTask.TASK_NAME)

		this.inputJar.convention {
			this.getTaskByName<MergeGameJarsTask>(MergeGameJarsTask.TASK_NAME).mergedJar
		}
		this.mappingsFile.convention {
			this.getTaskByName<BuildMojangTinyTask>(BuildMojangTinyTask.TASK_NAME).tinyFile
		}
		this.outputJar.convention { this.fileConstants.mcVersionDir.resolve("mapped_game.jar").toFile() }

		this.inputs.files(this.fileConstants.librariesDir)
	}
}
