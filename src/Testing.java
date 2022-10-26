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
	public static void main(String[] args) throws Exception {
		new ArgumentParser("Testing") {{
			addArgument(
				new Argument<>("string", new StringJoiner())
					.callback(t -> System.out.println("wow look a string: '" + t + "'"))
					.positional()
			);
			addArgument(new Argument<>("arg", ArgumentType.BOOLEAN()).callback(System.out::println));
			addSubCommand(new Command("stuff", "") {{
				addArgument(new Argument<>("c", ArgumentType.COUNTER()));
				addArgument(new Argument<>("string", new StringJoiner()).positional().callback(System.out::println));
				addSubCommand(new Command("shit", "") {{
					addArgument(new Argument<>("ball", ArgumentType.BOOLEAN()).callback(System.out::println));
				}});
			}});
			addSubCommand(new Command("foo", "") {{
				addArgument(new Argument<>("qux", ArgumentType.INTEGER()).callback(System.out::println));
			}});
		}}.parseArgs("[hey whats up] stuff --qux [123] shit --ball");
	}
}