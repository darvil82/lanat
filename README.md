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
		public static void beforeInit(@NotNull CommandBuildHelper helper) {
			// configure the argument "age" to have an argument type of
			// number range and set the range to 1-100
			helper.<NumberRangeArgumentType<Integer>, Integer>getArgument("age")
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

> [!WARNING]
> The documentation is still work in progress. Some parts may be missing or incomplete.

[Click here](https://darvil82.github.io/lanat-docs/acquire-lanat.html) to get started with Lanat, and to check out the
full documentation of the latest stable version.

Javadocs for the latest stable version is available [here](https://darvil82.github.io/Lanat/).


## FAQ

* ### Your logo has plenty of imperfections, please fix it.

	The logo couldn't be simpler. I made it in five minutes in Figma. I am a terrible designer.
	Also nothing is perfect. Heck, we are talking about software here, where bugs are
	the norm, so, I think it's fine.

	Also that isn't a question.

* ### Why the name "Lanat"?

	I had a tough time finding a name for this project. I wanted something short, easy to remember,
	and that sounded good. It had to be related to the project in some way.
	Sadly most names I came up with were already taken... Anyway, so Lanat is actually an acronym for
	**L**iterally **A**ll **N**ames **A**re **T**aken.
	Yeah, I'm good at naming things.

	* #### How is "Lanat" related to the project?

		It's not.