import argparser.*;
import argparser.argumentTypes.*;
import argparser.HelpFormatter;
import argparser.utils.UtlString;

public final class SimpleTests {
	public static void main(String[] args) {
		HelpFormatter.lineWrapMax = 110;
//		TextFormatter.debug = true;

		enum Something {
			ONE, TWO, THREE
		}

		final var argumentParser = new TestingParser("Testing") {{
			addGroup(new ArgumentGroup("a group") {{
				addArgument(new Argument<>("range", new IntRangeArgument(1, 10))
						.onOk((value) -> System.out.println("Range: " + value))
						.description("word ".repeat(123))
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
				addGroup(new ArgumentGroup("a subgroup") {{
					exclusive();
					addArgument(new Argument<>("string", ArgumentType.STRING())
							.description("a string")
					);
					addArgument(new Argument<>("bool", ArgumentType.BOOLEAN())
							.description("a bool")
					);
				}});
			}});
			addSubCommand(new Command("cmd") {{
				addArgument(Argument.simple("test-arg"));
			}});
		}};

		var parsedArgs = argumentParser.parseArgsExpectErrorPrint("--help");
	}
}
