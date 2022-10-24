import argparser.*;

class StringJoiner extends ArgumentType<String> {
	@Override
	public ArgValueCount getNumberOfArgValues() {
		return new ArgValueCount(0, 3);
	}

	@Override
	public void parseArgValues(String[] args) {
		this.value = "(" + String.join("), (", args) + ")";
	}
}

public class Testing {
	public static void main(String[] args) throws Exception {
		new ArgumentParser("Testing") {{
			addArgument(
				new Argument<>("string", new StringJoiner())
					.callback(t -> System.out.println("wow look a string: '" + t + "'"))
					.positional()
			);
			addArgument(new Argument<>("arg", ArgumentType.BOOLEAN()).callback(System.out::println));
			addSubCommand(new Command("stuff", "") {{
				addArgument(new Argument<>("c", ArgumentType.COUNTER()).callback(System.out::println));
			}});
		}}.parseArgs("['test test' 'another one'] --arg".split(" "));
	}
}