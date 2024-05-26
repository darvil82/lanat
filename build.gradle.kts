plugins {
	`maven-publish`
	`java-library`
}

group = "com.darvil"
version = "1.2.0"
description = "Command line argument parser"

dependencies {
	api("com.darvil:utils:0.7.0")
	api("com.darvil:terminal-text-formatter:2.2.0")

	implementation("org.jetbrains:annotations:24.1.0")
	testImplementation(platform("org.junit:junit-bom:5.9.1"))
	testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
	withJavadocJar()
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
	mavenLocal()
	maven("https://repsy.io/mvn/darvil/java")
}

publishing {
	repositories {
		maven {
			name = "Github"
			url = uri("https://maven.pkg.github.com/darvil82/Lanat")
			credentials(PasswordCredentials::class)
		}

		maven {
			name = "Repsy"
			url = uri("https://repsy.io/mvn/darvil/java")
			credentials(PasswordCredentials::class)
		}

		mavenLocal()
	}

	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
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