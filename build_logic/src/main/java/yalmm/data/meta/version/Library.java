package yalmm.data.meta.version;

import java.util.List;

public record Library(
		String name,
		Downloads downloads,
		List<Rule> rules
) {
	public record Downloads(LibraryArtifact artifact) {}

	public record Rule(String action) {}
}
