import argparser.*;
import argparser.argumentTypes.EnumArgument;
import argparser.argumentTypes.IntRangeArgument;
import argparser.utils.UtlString;
import argparser.utils.displayFormatter.Color;
import argparser.utils.displayFormatter.TextFormatter;

import java.util.Arrays;
import java.util.Scanner;

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
		System.out.println(pArgs.get("range").get());

//		var test = new TextFormatter("(")
//				.setColor(Color.BLUE)
//				.concat("a", "b")
//				.concat(new TextFormatter("c")
//						.setColor(Color.RED))
//				.concat(")").toString();
//
//		System.out.println(test);
//		System.out.println(test.replace(String.valueOf(TextFormatter.ESC), new TextFormatter("ESC").setColor(Color.BRIGHT_GREEN).toString()));
	}
}
