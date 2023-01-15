import argparser.Argument;
import argparser.ArgumentParser;
import argparser.ArgumentType;
import argparser.HelpFormatter;
import argparser.argumentTypes.*;

import java.util.Arrays;

public final class SimpleTests {
	public static void main(String[] args) {
		HelpFormatter.lineWrapMax = 1000;

		enum Something {
			ONE, TWO, THREE
		}

		final var argumentParser = new TestingParser("Testing") {{
			addArgument(new Argument<>("range", new IntRangeArgument(1, 10))
				.onOk((value) -> System.out.println("Range: " + value))
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
		}};

		var parsedArgs = argumentParser.parseArgsExpectErrorPrint("--number six");
	}
}
