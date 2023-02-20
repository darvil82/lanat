| <h1>Important!</h1> Before you read the information below, please note that this project is still in development and is not ready for production use. You are free to use it (thank you if you do so), but you should be aware that the project is in a constantly changing state at the moment! |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|



# Lanat

Lanat is a command line argument parser for Java 17 with ease  of use and high customization
possibilities in mind.

### Examples
Here is an example of a simple argument parser definition.

```java
class Test {
	public static void main(String[] args) {
		final var myParser = new ArgumentParser("MyProgram") {{
			this.addArgument(Argument.create("name", ArgumentType.STRING())
				.obligatory()
				.positional() // doesn't need the name to be specified
				.description("The name of the user.")
			);

			this.addArgument(Argument.create("surname", ArgumentType.STRING())
				.description("The surname of the user.")
			);

			this.addArgument(Argument.create("age", ArgumentType.INTEGER())
				.defaultValue(18)
				.description("The age of the user.")
				.addNames("a")
				.prefixChar(Argument.PrefixChar.PLUS)
			);
		}};

		// example: david +a20
		final var parsedArguments = myParser.parseArgs(args);

		System.out.printf(
			"Welcome %s! You are %d years old.%n",
			parsedArguments.get("name").get(), parsedArguments.<Integer>get("age").get()
		);

		// if no surname was specified, we'll assume it is "Lewis". (Don't ask why)
		System.out.println(
			"The surname of the user is " + parsedArguments.get("surname").undefined("Lewis")
		);
	}
}
```