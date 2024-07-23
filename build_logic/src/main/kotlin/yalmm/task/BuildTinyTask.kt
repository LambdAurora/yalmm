package yalmm.task

import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.*
import org.gradle.api.tasks.util.PatternSet
import yalmm.model.Mapping
import yalmm.model.MappingClass
import yalmm.util.JsonUtils
import java.io.File
import java.nio.file.Files

open class BuildTinyTask : DefaultYalmmTask("build") {
	private var files: FileCollection = project.objects.fileCollection().from("mappings")

	@OutputFile
	var output: File? = this.project.layout.buildDirectory.file("generated/mappings/mappings.tiny").get().asFile

	@InputFiles
	@SkipWhenEmpty
	@IgnoreEmptyDirectories
	fun getSource(): FileTree {
		return this.files.asFileTree.matching(PatternSet().include("**/*.json5"))
	}

	fun setSource(source: FileTree) {
		this.files = this.project.objects.fileCollection().from(source);
	}

	@TaskAction
	fun build() {
		// Load mappings.
		val mapping = Mapping()

		for (file in this.getSource().files) {
			try {
				val content = Files.readString(file.toPath())
				val result = JsonUtils.GSON.fromJson(content, MappingClass::class.java)
				mapping.classes.add(result)
			} catch (e: Exception) {
				this.logger.error("Failed to parse {}.", file, e)
				throw e
			}
		}

		Files.writeString(this.output!!.toPath(), mapping.toTinyV2())
	}
}