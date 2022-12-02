import argparser.*;
import argparser.argumentTypes.KeyValuesArgument;
import argparser.utils.ErrorLevel;

import java.util.HashMap;

public class SimpleTests {
	public static void main(String[] args) {

		final var argumentParser = new ArgumentParser("SimpleTesting") {{
			addSubCommand(new Command("subcommand") {{
				addArgument(new Argument<>("test", ArgumentType.STRING()).obligatory());
			}});
		}};

//		var pArgs = argumentParser.parseArgs("-fff --test hii subcommand --nose <x.1 y.347 z.43423> another --test 'this is a test' what");
		final var pArgs = argumentParser.parseArgs("");

	}
}
