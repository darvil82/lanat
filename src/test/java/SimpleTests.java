import argparser.*;
import argparser.argumentTypes.*;
import argparser.utils.displayFormatter.TextFormatter;

import java.util.Arrays;

public final class SimpleTests {
	public static void main(String[] args) {
		HelpFormatter.lineWrapMax = 110;
//		TextFormatter.debug = true;

		enum Something {
			ONE, TWO, THREE
		}

		final var argumentParser = new TestingParser("Testing") {{
			addArgument(new Argument<>("range", new IntRangeArgument(1, 10))
				.onOk((value) -> System.out.println("Range: " + value))
				.description("a ".repeat(50))
			);
			addArgument(new Argument<>("number", new EnumArgument<>(Something.ONE))
				.positional()
				.description("Pick a number")
			);
			addArgument(new Argument<>("normal-int", ArgumentType.INTEGER())
				.defaultValue(78)
				.obligatory()
				.description("just a normal int lmao")
			);
			addSubCommand(new Command("cmd") {{
				addArgument(Argument.simple("test-arg"));
			}});
		}};

		var parsedArgs = argumentParser.parseArgsExpectErrorPrint("--help");
	}
}
