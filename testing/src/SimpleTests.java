import argparser.*;
import argparser.argumentTypes.EnumArgument;
import argparser.argumentTypes.IntRangeArgument;

public class SimpleTests {
	public static void main(String[] args) {
		HelpFormatter.lineWrapMax = 1000;

		enum Something {
			ONE, TWO, THREE
		}

		final var argumentParser = new ArgumentParser("Testing", "Some description") {{
			addArgument(new Argument<>("simple test", ArgumentType.KEY_VALUES(ArgumentType.INTEGER())));
			addArgument(new Argument<>("range", new IntRangeArgument(1, 10)));
			addArgument(new Argument<>("enum", new EnumArgument<>(Something.ONE)));
		}};


		final var pArgs = argumentParser.parseArgs("--help");
		System.out.println(pArgs.get("enum").get());
	}
}
