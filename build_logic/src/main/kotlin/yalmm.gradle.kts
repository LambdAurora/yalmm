import yalmm.Constants
import yalmm.MappingsExtension
import yalmm.task.EnigmaMappingsTask
import yalmm.task.compile.BuildBaseMappingsTinyTask
import yalmm.task.compile.BuildIntermediaryMappingsTinyTask
import yalmm.task.setup.ExtractServerJarTask
import yalmm.task.setup.MergeGameJarsTask
import yalmm.task.setup.download.*
import yalmm.task.setup.mapping.BuildMojangTinyTask
import yalmm.task.setup.mapping.MapGameJarTask

plugins {
	`java-library`
}

group = Constants.GROUP

extensions.add("mappings", MappingsExtension(project))

sourceSets {
	main {
		resources {
			srcDir(layout.buildDirectory.dir("generated"))
		}
	}
}

val intermediaryMappingsConfig: Configuration = configurations.create("intermediaryMappings")
configurations.create("enigmaRuntime")

tasks.register(DownloadVersionsManifestTask.TASK_NAME, DownloadVersionsManifestTask::class)
tasks.register(DownloadVersionManifestTask.TASK_NAME, DownloadVersionManifestTask::class)
tasks.register(DownloadGameArtifactTask.DOWNLOAD_CLIENT_JAR_TASK_NAME, DownloadGameArtifactTask::class) {
	this.name.set("client")
	this.fileName.set("client.jar")
}
tasks.register(DownloadGameArtifactTask.DOWNLOAD_CLIENT_MAPPINGS_TASK_NAME, DownloadGameArtifactTask::class) {
	this.name.set("client_mappings")
	this.fileName.set("client_mappings.txt")
}
tasks.register(DownloadGameArtifactTask.DOWNLOAD_SERVER_TASK_NAME, DownloadGameArtifactTask::class) {
	this.name.set("server")
	this.fileName.set("server_bootstrap.jar")
}
tasks.register(DownloadGameArtifactTask.DOWNLOAD_SERVER_MAPPINGS_TASK_NAME, DownloadGameArtifactTask::class) {
	this.name.set("server_mappings")
	this.fileName.set("server_mappings.txt")
}
tasks.register(ExtractServerJarTask.TASK_NAME, ExtractServerJarTask::class)
tasks.register(MergeGameJarsTask.TASK_NAME, MergeGameJarsTask::class)
tasks.register(GatherMinecraftLibrariesTask.TASK_NAME, GatherMinecraftLibrariesTask::class)
tasks.register("downloadIntermediary", DownloadMappingsTask::class) {
	this.mappingsName.set(intermediaryMappingsConfig.name)
}
tasks.register(BuildMojangTinyTask.TASK_NAME, BuildMojangTinyTask::class)
val mapGameJarTask = tasks.register(MapGameJarTask.TASK_NAME, MapGameJarTask::class)
tasks.register(EnigmaMappingsTask.TASK_NAME, EnigmaMappingsTask::class) {
	this.dependsOn(MapGameJarTask.TASK_NAME)
	this.jarToMap.set(mapGameJarTask.get().outputJar.get().asFile)
}
tasks.register(BuildBaseMappingsTinyTask.TASK_NAME, BuildBaseMappingsTinyTask::class)
val buildTinyTask = tasks.register(BuildIntermediaryMappingsTinyTask.TASK_NAME, BuildIntermediaryMappingsTinyTask::class)

tasks.processResources.configure {
	dependsOn(buildTinyTask)
	mustRunAfter(tasks.compileJava)
}
