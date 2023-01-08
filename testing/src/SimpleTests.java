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
			addArgument(new Argument<>("simple test", ArgumentType.KEY_VALUES(new EnumArgument<>(Something.ONE))));
			addArgument(new Argument<>("range", new IntRangeArgument(1, 10)).onOk((value) -> System.out.println("Range: " + value)));
			addArgument(new Argument<>("enum", new EnumArgument<>(Something.ONE)));
			addArgument(new Argument<>("normal-int", ArgumentType.INTEGER()).defaultValue(89));
		}};

		final var pArgs = argumentParser.parseArgs("--range 0 --range -4 --range 14");
		System.out.println(pArgs.get("range").get());
	}
}
