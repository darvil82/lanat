<div align="center">
	<div>
		<img alt="Lanat logo" src="https://github.com/DarviL82/Lanat/assets/48654552/33f9a03d-1ce3-49f0-839d-475e35d9e816" width="450">
	</div>
	<br>
	<strong>
		A command line argument parser for Java 17 with <br>
		ease of use and high customization possibilities in mind.
	</strong>
</div>

<br><br>

> [!IMPORTANT]
> **This project is still in development.** It is not recommended to use Lanat in production, as it could possibly
> have important issues. It is also quickly evolving, thus breaking changes are constantly being made.

### Example
- First, we define our Command by creating a *Command Template*.
	
	```java
	@Command.Define
	class MyProgram {
		@Argument.Define(required = true, positional = true, description = "The name of the user.")
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
	```
 
 - Then, let that class definition also serve as the container for the parsed values.
	```java
 	class Test {
		public static void main(String[] args) {
			// example: david +a20
			var myProgram = ArgumentParser.parseFromInto(MyProgram.class, CLInput.from(args));
			
			System.out.printf(
				"Welcome %s! You are %d years old.%n",
				myProgram.name, myProgram.age
			);
	
			// if no surname was specified, we'll show "none" instead
			System.out.printf("The surname of the user is %s.%n", myProgram.surname.orElse("none"));
		}
	}
 	```

## Documentation

Javadoc documentation for the latest stable version is available [here](https://darvil82.github.io/Lanat/).

Deep documentation and tutorials comming soon.


## Installation

The package is currently available on Repsy and GitHub Packages.

1. Add the following to your `repositories` block:
	```kotlin
	maven("https://repsy.io/mvn/darvil/java")
	```
 
2. And add the following to your `dependencies` block:
	```kotlin
	implementation("com.darvil:lanat:+")
	```
	> [!NOTE]
	> The `+` symbol is a wildcard that will automatically use the latest version of the package.
	> You can also specify a specific version (e.g. `0.1.0`).

<details>

<summary><strong>Install from Github Packages</strong></summary>

If using Gradle, follow these steps:

1. Authenticate to GitHub Packages to be able to download the package. You can do this by adding the following to your [gradle.properties](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties) file:

   ```
   gpr.user=USERNAME
   gpr.key=PERSONAL_ACCESS_TOKEN
   ```

   Replace `USERNAME` with your GitHub username and `PERSONAL_ACCESS_TOKEN` with a personal access token that has the `read:packages` scope.

2. Add the following inside your `repositories` block:

    ```kotlin
    maven {
        url = uri("https://maven.pkg.github.com/darvil82/lanat")
        credentials {
            username = project.findProperty("gpr.user") as String?
            password = project.findProperty("gpr.key") as String?
        }
    }
    ```

3. Follow step 2 from the previous section.

This information is available at the [GitHub Packages documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package).

For other build tools, see [here](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package).

</details>
