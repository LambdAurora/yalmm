package yalmm.data.meta;

import yalmm.util.JsonUtils;

import java.util.List;

/**
 * Represents the Minecraft versions manifest.
 *
 * @param versions the versions
 */
public record VersionsManifest(List<Entry> versions) {
	public record Entry(String id, String url, String releaseTime, String sha1) {}

	public static VersionsManifest fromString(String versionsManifest) {
		return JsonUtils.GSON.fromJson(versionsManifest, VersionsManifest.class);
	}
}
