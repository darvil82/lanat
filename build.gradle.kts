plugins {
	java
	`maven-publish`
}

group = "darvil"
version = "INDEV"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()

	// this is here in case fade needs to use lanat as a live testing and developing tool lmao
	// mavenLocal()

	maven {
		name = "github-mirror"
		url = uri("https://maven.pkg.github.com/fadeoffical/mirror")
		credentials {
			username = project.findProperty("gpr.user") as String? ?: System.getenv("CI_GITHUB_USERNAME")
			password = project.findProperty("gpr.key") as String? ?: System.getenv("CI_GITHUB_PASSWORD")
		}
	}
}

dependencies {
	implementation("org.jetbrains:annotations:24.0.1")
	implementation("fade:mirror:0.0.8+develop")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

java {
	withJavadocJar()
	withSourcesJar()
}


publishing {
	repositories {
		maven {
			name = "github"
			url = uri("https://maven.pkg.github.com/darvil82/Lanat")
			credentials {
				username = System.getenv("CI_GITHUB_USERNAME")
				password = System.getenv("CI_GITHUB_PASSWORD")
			}
		}
	}

	publications {
		register<MavenPublication>("gpr") {
			from(components["java"])
			artifactId = rootProject.name
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