| <h1>Important!</h1> Before you read the information below, please note that this project is still in development and is not ready for production use. You are free to use it (thank you if you do so), but you should be aware that the project is in a constantly changing state at the moment! |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|



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
		// IntegerRangeArgumentType and set the range to 1-100
		cmdBuildHelper.<IntegerRangeArgumentType, Integer>getArgument("age")
			.withArgType(ArgumentType.INTEGER_RANGE(1, 100))
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
		System.out.println("The surname of the user is " + myProgram.surname.orElse("none"));
	}
}
```

## Documentation

Javadoc documentation for the latest stable version is available [here](https://darvil82.github.io/Lanat/).