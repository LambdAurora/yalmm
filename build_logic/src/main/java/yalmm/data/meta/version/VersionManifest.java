package yalmm.data.meta.version;

import yalmm.util.JsonUtils;

import java.util.List;

public record VersionManifest(
		String id,
		Downloads downloads,
		List<Library> libraries
) {
	public static VersionManifest fromString(String versionsManifest) {
		return JsonUtils.GSON.fromJson(versionsManifest, VersionManifest.class);
	}
}
