rootProject.name = "Yet Another Light Minecraft Mappings"

pluginManagement {
	repositories {
		maven {
			name = "Fabric Maven"
			url = uri("https://maven.fabricmc.net/")
		}
		gradlePluginPortal()
	}
}

includeBuild("build_logic")
