plugins {
	java
	`maven-publish`
	signing
}

group = "darvil"
version = "0.0.3"
description = "Command line argument parser"

dependencies {
	implementation("org.jetbrains:annotations:24.0.1")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

java {
	withJavadocJar()
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
			artifactId = project.name
		}

		// GitHub Packages publication
		create<MavenPublication>("gpr") {
			from(components["java"])
			artifactId = project.name
		}
	}

	// Sonatype repository
	repositories {
		maven {
			url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
			credentials {
				username = project.findProperty("ossrhUsername") as? String ?: ""
				password = project.findProperty("ossrhPassword") as? String ?: ""
			}
		}

		// GitHub Packages repository
		repositories {
			maven {
				name = "github"
				url = uri("https://maven.pkg.github.com/darvil82/Lanat")
				credentials {
					username = project.findProperty("ciGithubUsername") as? String ?: ""
					password = project.findProperty("ciGithubPassword") as? String ?: ""
				}
			}
		}
	}

	signing {
		sign(configurations.archives.get())
		useInMemoryPgpKeys(System.getenv("GPG_KEY_ID"), System.getenv("GPG_KEY_RING_FILE"))
	}
}

tasks.named<Test>("test") {
	useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
}

tasks.withType<Javadoc>().configureEach {
	options.encoding = "UTF-8"
}
