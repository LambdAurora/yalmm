rootProject.name = "Yet Another Light Minecraft Mappings"

pluginManagement {
	repositories {
		gradlePluginPortal()
		maven {
			name = "Fabric Maven"
			url = uri("https://maven.fabricmc.net/")
		}
		maven {
			name = "Quilt Maven"
			url = uri("https://maven.quiltmc.org/repository/release/")
		}
	}
}

includeBuild("build_logic")
