plugins {
	`java-library`
	id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "io.github.darvil82"
version = "1.3.0"
description = "A highly customizable command line argument parser."

repositories {
	mavenCentral()
	mavenLocal()
}

dependencies {
	api("io.github.darvil82:utils:0.7.1")
	api("io.github.darvil82:terminal-text-formatter:2.2.0")

	implementation("org.jetbrains:annotations:24.1.0")
	testImplementation(platform("org.junit:junit-bom:5.9.1"))
	testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

mavenPublishing {
	publishToMavenCentral()
	signAllPublications()
	coordinates(group.toString(), name.toString(), version.toString())

	pom {
		name.set("Lanat")
		description.set(project.description)
		inceptionYear.set("2022")
		url.set("https://github.com/darvil82/lanat")
		licenses {
			license {
				name.set("MIT License")
				url.set("https://opensource.org/license/mit")
				distribution.set("https://opensource.org/license/mit")
			}
		}
		developers {
			developer {
				id.set("darvil82")
				name.set("darvil82")
				url.set("https://github.com/darvil82/")
			}
		}
		scm {
			url.set("https://github.com/darvil82/lanat")
			connection.set("scm:git:git://github.com/darvil82/lanat.git")
			developerConnection.set("scm:git:ssh://git@github.com/darvil82/lanat.git")
		}
	}
}

tasks.test {
	useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
}

tasks.withType<Javadoc>().configureEach {
	options.encoding = "UTF-8"
}
