# Lanat

Lanat is a command line argument parser for Java 17 with ease  of use and high customization
possibilities in mind.

### Examples
Here is an example of a simple argument parser definition.

```java
@Command.Define
class MyProgram {
	@Argument.Define(obligatory = true, positional = true, description = "The name of the user.")
	public String name;

	@Argument.Define(argType = StringArgumentType.class, description = "The surname of the user.")
	public Optional<String> surname;

	@Argument.Define(names = {"age", "a"}, description = "The age of the user.", prefix = '+')
	public int age = 18;
	
	@InitDef
	public static void beforeInit(@NotNull CommandBuildHelper cmdBuildHelper) {
		// configure the argument "age" to have an argument type of
		// number range and set the range to 1-100
		cmdBuildHelper.<NumberRangeArgumentType<Integer>, Integer>getArgument("age")
			.withArgType(new NumberRangeArgumentType<>(1, 100))
			.onOk(v -> System.out.println("The age is valid!"));
	}
}

class Test {
	public static void main(String[] args) {
		// example: david +a20
		var myProgram = ArgumentParser.parseFromInto(MyProgram.class, CLInput.from(args));
		
		System.out.printf(
			"Welcome %s! You are %d years old.%n",
			myProgram.name, myProgram.age
		);

		// if no surname was specified, we'll show "none" instead
		System.out.printf("The surname of the user is %s.", myProgram.surname.orElse("none"));
	}
}
```

## Documentation

Javadoc documentation for the latest stable version is available [here](https://darvil82.github.io/Lanat/).


## Installation

The package is currently only available on GitHub Packages.

### Gradle

1. Authenticate to GitHub Packages in order to be able to download the package. You can do this by adding the following to your [gradle.properties](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties) file:

	```
	gpr.user=USERNAME
	gpr.key=PERSONAL_ACCESS_TOKEN
	```

	Replace `USERNAME` with your GitHub username and `PERSONAL_ACCESS_TOKEN` with a personal access token that has the `read:packages` scope.

2. If using Gradle, add the following inside your `repositories` block:

    ```kotlin
    maven {
        url = uri("https://maven.pkg.github.com/darvil82/lanat")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("CI_GITHUB_USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("CI_GITHUB_PASSWORD")
        }
    }
    ```

3. And add the following to your `dependencies` block:

    ```kotlin
    implementation("darvil:lanat")
    ```

    Note that you may need to explicitly specify the version of the package you want to use. (e.g. `darvil:lanat:0.0.1`)

This information is available at the [GitHub Packages documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package).

### Other build tools

See [here](https://docs.github.com/en/packages/working-with-a-github-packages-registry).
