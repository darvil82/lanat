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
			addArgument(new Argument<>("range", new IntRangeArgument(1, 10)).onOk((value) -> System.out.println("Range: " + value)));
			addArgument(new Argument<>("enum", new EnumArgument<>(Something.ONE)).positional());
			addArgument(new Argument<>("normal-int", ArgumentType.INTEGER()).defaultValue(89).obligatory());
		}};

		final var pArgs = argumentParser.parseArgs("--help");
//		final var pArgs = argumentParser.parseArgs("--range 5 --enum two --normal-int 213");
		System.out.println(pArgs.get("range").get());
	}
}
