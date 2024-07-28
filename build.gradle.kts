import yalmm.Constants

plugins {
	id("yalmm")
	`java-library`
	`maven-publish`
}

version = "${Constants.MINECRAFT_VERSION}+build.${System.getenv().getOrDefault("BUILD_NUMBER", "local")}"
base.archivesName.set("yalmm")

repositories {
	mavenCentral()
	maven {
		name = "Fabric Maven"
		url = uri("https://maven.fabricmc.net/")
	}
}

dependencies {
	intermediaryMappings("net.fabricmc:intermediary:${Constants.MINECRAFT_VERSION}")

	enigmaRuntime(libs.enigma.gui)
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
