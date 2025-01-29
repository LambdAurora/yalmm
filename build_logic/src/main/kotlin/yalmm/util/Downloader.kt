package yalmm.util

import de.undercouch.gradle.tasks.download.DownloadAction
import de.undercouch.gradle.tasks.download.DownloadDetails
import org.gradle.api.Action
import org.gradle.api.Task
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.concurrent.CompletableFuture

class Downloader(task: Task) {
	private val action = DownloadAction(task.project, task)

	fun src(url: String?): Downloader {
		return this.src(URI(url!!))
	}

	fun src(url: URI): Downloader {
		return this.src(url.toURL())
	}

	private fun src(url: java.net.URL): Downloader {
		this.action.src(url)
		return this
	}

	fun dest(file: File?): Downloader {
		this.action.dest(file)
		return this
	}

	fun overwrite(overwrite: Boolean): Downloader {
		this.action.overwrite(overwrite)
		return this
	}

	fun eachFile(action: Action<DownloadDetails>): Downloader {
		this.action.eachFile(action)
		return this
	}

	@Throws(IOException::class)
	fun download(): CompletableFuture<Void> {
		return this.action.execute()
	}
}