import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	`java-gradle-plugin`
	`kotlin-dsl`
}

val javaVersion = 21

repositories {
	gradlePluginPortal()
}

dependencies {
	implementation(libs.jetbrains.annotations)
	implementation(libs.download.task)
	implementation(libs.gson)
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
