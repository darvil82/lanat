import argparser.*;
import argparser.argumentTypes.*;
import argparser.helpRepresentation.HelpFormatter;

public final class SimpleTests {
	public static void main(String[] args) {
		HelpFormatter.lineWrapMax = 110;
		HelpFormatter.debugLayout = true;
//		TextFormatter.debug = true;

		enum Something {
			ONE, TWO, THREE
		}

		final var argumentParser = new TestingParser("Testing") {{
			addArgument(new Argument<>("a shit", ArgumentType.STRING()));
			addGroup(new ArgumentGroup("a group") {{
				addArgument(new Argument<>("range", new IntRangeArgument(1, 10))
						.onOk((value) -> System.out.println("Range: " + value))
						.description("word ".repeat(123))
				);
				addArgument(new Argument<>("number", new EnumArgument<>(Something.ONE))
						.positional()
						.description("Pick a number")
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
			addGroup(new ArgumentGroup("another group") {{
				exclusive();
				addArgument(new Argument<>("string2", ArgumentType.STRING())
					.description("a string")
					.onOk((value) -> System.out.println("String: " + value))
				);
				addArgument(new Argument<>("bool2", ArgumentType.BOOLEAN())
					.description("a bool")
				);
				addGroup(new ArgumentGroup("another subgroup") {{
					exclusive();
					addArgument(new Argument<>("string3", ArgumentType.STRING())
						.description("a string")
					);
					addArgument(new Argument<>("bool3", ArgumentType.BOOLEAN())
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
