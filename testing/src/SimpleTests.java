import argparser.*;
import argparser.utils.ErrorLevel;

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
		final var pArgs = argumentParser.parseArgs("[a] jawoid");
//
		System.out.println(pArgs.<Integer>get("subcommand.c").get());

		var whatArg = new String[1];
		if (pArgs.get("what").defined(whatArg)) {
			System.out.println("what: " + whatArg[0]);
		}
	}
}
