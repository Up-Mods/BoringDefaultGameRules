plugins {
	id("maven-publish")
	alias(libs.plugins.fabric.loom)
}

version = "2.0.0+1.21.5"
group = "page.langeweile"

repositories {
	maven("https://maven.parchmentmc.org")
	maven("https://maven.terraformersmc.com/releases/")
}

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

tasks.named<ProcessResources>("processResources").configure {
	val version = project.version
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType<JavaCompile> {
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

// If you plan to use a different file for the license, don't forget to change the file name here!
tasks.named<Jar>("jar").configure {
	val name = project.name
	inputs.files("LICENSE.md")
	inputs.property("name", name)

	from("LICENSE.md") {
		rename { "LICENSE_${name}.md" }
	}
}

// Configure the maven publication
publishing {
	publications {
		register<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}