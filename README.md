<div align="center">
	<a href="https://darvil82.github.io/lanat-web/">
	  <picture>
		<source media="(prefers-color-scheme: dark)" srcset="resources/logo.png" width="450">
		<img width="450" src="resources/logo_dark.png">
	  </picture>
	</a>
	<br>
	<strong>
		A command line argument parser for Java 17 with <br>
		ease of use and high customization possibilities in mind.
	</strong>
</div>

<br><br>

## Example
- First, we define our Command by creating a *Command Template*.

	```java
	@Command.Define
	class MyProgram {
		@Argument.Define(required = true, positional = true, description = "The name of the user.")
		public String name;
	
		@Argument.Define(type = String.class, description = "The surname of the user.")
		public Optional<String> surname;
	
		@Argument.Define(names = {"age", "a"}, description = "The age of the user.", prefix = Argument.Prefix.PLUS)
		public int age = 18;
		
		@InitDef
		public static void beforeInit(@NotNull CommandBuildContext ctx) {
			// configure the argument "age" to have an argument type of
			// number range and set the range to 18-100
			ctx.argWithType("age", new NumberRangeArgumentType<>(18, 100))
				.onOk(v -> System.out.println("The age is valid!"));
		}
	}
	```

- Then, let that class definition also serve as the container for the parsed values.

	```java
	public static void main(String[] args) {
		// example: david +a20
		var myProgram = ArgumentParser.parseFromInto(MyProgram.class, args);
		
		System.out.printf(
			"Welcome %s! You are %d years old.%n",
			myProgram.name, myProgram.age
		);

		// if no surname was specified, we'll show "none" instead
		System.out.printf("The surname of the user is %s.%n", myProgram.surname.orElse("none"));
	}
	```


## Documentation

Check out the [website](https://darvil82.github.io/lanat-web/) for more information.

[Click here](https://darvil82.github.io/lanat-docs/acquire-lanat.html) to get started with Lanat, and to check out the
full documentation of the latest stable version.

Javadocs for the latest stable version is available [here](https://darvil82.github.io/lanat/).
