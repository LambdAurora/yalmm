# Yet Another Light Minecraft Mappings

This is a "light" opinionated Minecraft mappings intended to be used
on top of the official Mojang-provided mappings for Minecraft.

## Why?

I have been a long-time modder, I had seen MCP mappings long ago,
then when I dive deep into modding I really started to understand
Yarn, used it a lot and contributed to it too.
Then Quilt happened and I switched to Quilt Mappings
and contributed to it too.

I have seen the rise of the Mojang mappings inside the ecosystem,
now that has a stable place in the ecosystem, I had to start
question whether I really wanted to continue to use incomplete,
sometimes confusing, mappings.

I do not have the energy to chase after mappings anymore,
and for a while I had the idea of making a very light mapping set
to use alongside the Mojang mappings, to fix the main gripes
I had with it, and I know I'm not the only one sharing those gripes.

I initially shared the idea with Quilt,
but it was rejected at the time as they believed in the strength
of Quilt Mappings.

Lately when doing my rounds of mod porting, I really became
tired of dealing with the mappings and finally have decided to
take matters into my own hands.
This led to this, a setup heavily inspired by other mappings but
lightened as much as possible, to create a light mapping set
with the main goal of making the Mojang mappings more bearable,
so I can finally mod in peace without having to deal
with holes in mappings.

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
		// Parchment mappings if you wanted even more parameters and javadocs.
		parchment("org.parchmentmc.data:parchment-${minecraftVersion}:<version>@zip")
		mappings("dev.lambdaurora:yalmm:${minecraftVersion}+build.${yalmmVersion}")
	})
}
```
