package yalmm.util

import de.undercouch.gradle.tasks.download.DownloadAction
import org.gradle.api.Task
import java.io.File
import java.io.IOException
import java.net.URI

class Downloader(private val task: Task) {
	private val project = task.project
	private var src: String? = null
	private var dest: File? = null
	private var overwrite = false

	fun src(url: String?): Downloader {
		this.src = url
		return this
	}

	fun dest(file: File?): Downloader {
		this.dest = file
		return this
	}

	fun overwrite(overwrite: Boolean): Downloader {
		this.overwrite = overwrite
		return this
	}

	@Throws(IOException::class)
	fun download() {
		val downloadAction = DownloadAction(this.project, this.task)
		downloadAction.src(URI(this.src!!).toURL())
		downloadAction.dest(this.dest!!)
		downloadAction.overwrite(this.overwrite)

		downloadAction.execute()
	}
}