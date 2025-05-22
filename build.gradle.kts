plugins {
	id("maven-publish")
    alias(libs.plugins.fabric.loom)
}

version = "2.0.0+1.21.5"
group = "io.github.ennuil"

repositories {
    maven("https://maven.parchmentmc.org")
	maven("https://maven.terraformersmc.com/releases/")
}

loom {
	mods {
		register("boring_default_game_rules") {
			sourceSet("main")
		}
	}

	mixin {
		useLegacyMixinAp = false
	}

	accessWidenerPath = file("src/main/resources/boring_default_game_rules.accesswidener")
}

// All the dependencies are declared at gradle/libs.version.toml and referenced with "libs.<id>"
// See https://docs.gradle.org/current/userguide/platforms.html for information on how version catalogs work.
dependencies {
	minecraft(libs.minecraft)
	mappings(loom.layered {
		officialMojangMappings()
		parchment(libs.parchment)
	})
	modImplementation(libs.fabric.loader)

	modImplementation(libs.fabric.api)
	modImplementation(libs.mod.menu)
}

tasks.named<ProcessResources>("processResources").configure {
    val version = project.version
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        expand("version" to version)
    }
}

tasks.withType<JavaCompile> {
    // Minecraft 1.20.6 (24w14a) upwards uses Java 21.
    options.release = 21
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	// If this mod is going to be a library, then it should also generate Javadocs in order to aid with development.
	// Uncomment this line to generate them.
	// withJavadocJar()
}

// Configure the maven publication
publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
