package yalmm.data.meta.version;

public class Artifact {
	private final String url;
	private final int size;
	private final String sha1;

	public Artifact(String url, int size, String sha1) {
		this.url = url;
		this.size = size;
		this.sha1 = sha1;
	}

	public String url() {
		return this.url;
	}

	public int size() {
		return this.size;
	}

	public String sha1() {
		return this.sha1;
	}
}
