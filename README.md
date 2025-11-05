# Yet Another Light Minecraft Mappings - Mojmap backwards compat

This is a quick mapping to provide to versions before 25w45a the `Identifier` name
and some of the renames after then to ease multi-version development.

## How to Use?

With loom this is very simple as the toolchain added a way to
layer mappings on top of each other when the official Mojang
mappings released.

Simply add the following to your buildscript:

```kotlin
repositories {
	maven {
		name = "Gegy"
		url = uri("https://maven.gegy.dev/releases/")
	}
}

dependencies {
	mappings(loom.layered {
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-1.21.5:2025.06.15@zip")
		mappings("dev.lambdaurora:yalmm-mojbackward:1.21.5+build.<build>")
	})
}
```

And add to your `gradle.properties` the following:
```properties
fabric.loom.dropNonIntermediateRootMethods=true
```

## Wait what is YALMM?

I invite you to check out the regular branch of this project.
