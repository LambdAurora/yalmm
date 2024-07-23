package yalmm.task

import org.gradle.api.DefaultTask

open class DefaultYalmmTask(group: String) : DefaultTask() {
	init {
		this.group = group
	}
}
