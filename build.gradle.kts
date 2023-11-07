plugins {
	java
	`maven-publish`
}

group = "darvil"
version = "0.1.0b"
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
	repositories {
		maven {
			name = "Github"
			url = uri("https://maven.pkg.github.com/darvil82/Lanat")
			credentials(PasswordCredentials::class)
		}

		maven {
			name = "Repsy"
			url = uri("https://repsy.io/mvn/darvil/lanat")
			credentials(PasswordCredentials::class)
		}
	}

	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
		}
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