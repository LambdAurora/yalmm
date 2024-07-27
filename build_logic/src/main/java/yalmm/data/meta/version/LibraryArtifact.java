package yalmm.data.meta.version;

public class LibraryArtifact extends Artifact {
	private final String path;

	public LibraryArtifact(String url, int size, String sha1, String path) {
		super(url, size, sha1);
		this.path = path;
	}

	public String path() {
		return this.path;
	}
}
