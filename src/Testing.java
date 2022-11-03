import argparser.*;

class StringJoiner extends ArgumentType<String> {
	@Override
	public ArgValueCount getNumberOfArgValues() {
		return new ArgValueCount(1, 3);
	}

	@Override
	public void parseArgValues(String[] args) {
		this.value = "(" + String.join("), (", args) + ")";
	}
}

public class Testing {
	public static void main(String[] args) {
		new ArgumentParser("Testing") {{
			addArgument(new Argument<>("what", new StringJoiner())
				.callback(t -> System.out.println("wow look a string: '" + t + "'"))
				.positional()
				.obligatory()
			);
			addArgument(new Argument<>("a", ArgumentType.BOOLEAN()));
			addSubCommand(new Command("stuff") {{
				addArgument(new Argument<>("c", ArgumentType.COUNTER()));
				addArgument(new Argument<>('s', "string", new StringJoiner()).positional());
				addSubCommand(new Command("another") {{
					addArgument(new Argument<>("ball", new StringJoiner()));
					addArgument(new Argument<>("number", ArgumentType.INTEGER()).positional().obligatory());
				}});
			}});
		}}.parseArgs("stuff -s hola another");
	}
}