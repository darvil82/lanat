import argparser.*;

public class SimpleTests {
	public static void main(String[] args) {

		final var argumentParser = new ArgumentParser("Testing") {{
			addArgument(new Argument<>("what", new StringJoiner())
				.onOk(t -> System.out.println("wow look a string: '" + t + "'"))
				.positional()
				.obligatory()
			);
			addSubCommand(new Command("subcommand") {{
				addArgument(new Argument<>("c", ArgumentType.COUNTER()));
				addArgument(new Argument<>('s', "more-strings", new StringJoiner()));
				addSubCommand(new Command("another") {{
					addArgument(new Argument<>("ball", new StringJoiner()));
					addArgument(new Argument<>("number", ArgumentType.INTEGER()).positional().obligatory());
				}});
			}});
		}};

//		var pArgs = argumentParser.parseArgs("-fff --test hii subcommand --nose <x.1 y.347 z.43423> another --test 'this is a test' what");
//		final var pArgs = argumentParser.parseArgs("--help");
		final var pArgs = argumentParser.parseArgs("--what [a b c d] baller subcommand -ccc -s another 1234  peter");
//
		System.out.println(pArgs.<String>get("what").get());
	}
}
