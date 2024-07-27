package yalmm.data.meta.version;

import com.google.gson.annotations.SerializedName;

public record Downloads(
		Artifact client,
		@SerializedName("client_mappings") Artifact clientMappings,
		Artifact server,
		@SerializedName("server_mappings") Artifact serverMappings
) {
	public Artifact get(String name) {
		return switch (name) {
			case "client" -> this.client;
			case "client_mappings" -> this.clientMappings;
			case "server" -> this.server;
			case "server_mappings" -> this.serverMappings;
			default -> throw new IllegalArgumentException("There is no artifact " + name + ".");
		};
	}
}
