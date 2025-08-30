import yalmm.Constants

plugins {
	id("yalmm")
	`maven-publish`
}

val minecraftVersion = project.property("minecraft_version").toString()
version = "$minecraftVersion+build.${System.getenv().getOrDefault("BUILD_NUMBER", "local")}"
base.archivesName.set("yalmm")

repositories {
	maven {
		name = "Quilt Maven"
		url = uri("https://maven.quiltmc.org/repository/release/")
	}
}

dependencies {
	intermediaryMappings("net.fabricmc:intermediary:$minecraftVersion")

	enigmaRuntime(libs.enigma.gui)
	enigmaRuntime(libs.asm)
	enigmaRuntime(libs.quilt.json5)
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			from(components["java"])
			artifactId = "yalmm"

			pom {
				name = "Yet Another Light Minecraft Mappings"
				description = "A light Minecraft mappings intended to be used along with the official Mojang mappings."
			}
		}

		repositories {
			mavenLocal()

			val mavenRepo = System.getenv("MAVEN_URL")
			if (mavenRepo != null) {
				maven {
					name = "ReleaseMaven"
					url = uri(mavenRepo)
					credentials {
						username = (project.findProperty("gpr.user") ?: System.getenv("MAVEN_USERNAME")) as String
						password = (project.findProperty("gpr.key") ?: System.getenv("MAVEN_PASSWORD")) as String
					}
				}
			}
		}
	}
}
