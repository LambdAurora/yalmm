import yalmm.Constants
import yalmm.task.BuildTinyTask

plugins {
	`java-library`
}

group = Constants.GROUP

sourceSets {
	main {
		resources {
			srcDir(layout.buildDirectory.dir("generated"))
		}
	}
}

val buildTinyTask = tasks.register("buildTiny", BuildTinyTask::class) {
}

tasks.processResources.configure {
	dependsOn(buildTinyTask)
	mustRunAfter(tasks.compileJava)
}
