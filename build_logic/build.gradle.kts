import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	`java-gradle-plugin`
	`kotlin-dsl`
}

val javaVersion = 21

repositories {
	mavenCentral()
	maven {
		name = "Fabric Maven"
		url = uri("https://maven.fabricmc.net/")
	}
}

dependencies {
	implementation(libs.jetbrains.annotations)
	implementation(libs.asm)
	implementation(libs.download.task)
	implementation(libs.gson)
	implementation(libs.mappingio)
	implementation(libs.stitch)
	implementation(libs.tinyremapper)
	implementation(libs.vineflower)
}

java {
	sourceCompatibility = JavaVersion.toVersion(javaVersion)
	targetCompatibility = JavaVersion.toVersion(javaVersion)
}

kotlin {
	compilerOptions {
		jvmTarget = JvmTarget.fromTarget(javaVersion.toString())
	}
}
