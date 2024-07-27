package yalmm.task

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.Internal
import yalmm.FileConstants
import yalmm.MappingsExtension

open class DefaultYalmmTask(group: String) : DefaultTask() {
	@Internal
	protected val fileConstants: FileConstants = MappingsExtension.get(this.project).fileConstants

	init {
		this.group = group
	}

	@Suppress("UNCHECKED_CAST")
	fun <TASK : Task> getTaskByName(name: String): TASK {
		return this.project.tasks.getByName(name) as TASK
	}
}
