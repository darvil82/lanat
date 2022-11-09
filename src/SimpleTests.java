import argparser.Argument;
import argparser.ArgumentParser;
import argparser.ArgumentType;
import argparser.Command;

public class SimpleTests {
	public static void main(String[] args) {
		var argParser = new ArgumentParser("SimpleTesting") {{
			addArgument(new Argument<>("what", new StringJoiner())
				.callback(t -> System.out.println("wow look a string: '" + t + "'"))
				.positional()
				.obligatory()
				.errorCode(0x01)
			);
			addArgument(new Argument<>("a", ArgumentType.BOOLEAN()));
			addSubCommand(new Command("subcommand") {{
				addArgument(new Argument<>("c", ArgumentType.COUNTER()));
				addArgument(new Argument<>('s', "more-strings", new StringJoiner()));
				addSubCommand(new Command("another") {{
					addArgument(new Argument<>("ball", new StringJoiner()));
					addArgument(new Argument<>("number", ArgumentType.INTEGER()).positional().obligatory());
				}});
			}});
		}};
		argParser.parseArgs("12 subcommand another --foobar");
	}
}
