import argparser.*;
import argparser.argumentTypes.EnumArgument;
import argparser.argumentTypes.IntRangeArgument;
import argparser.helpRepresentation.HelpFormatter;

public final class ManualTests {
	public static void main(String[] args) {
		HelpFormatter.lineWrapMax = 110;
		HelpFormatter.debugLayout = true;

		ErrorFormatter.generator = new ErrorFormatter.ErrorFormatterGenerator() {
			@Override
			public String generate() {
				final var errorLevel = this.getErrorLevel();
				return this.getErrorLevelFormatter()
					.setContents("[" + errorLevel + ", " + this.getTokensViewFormatting() + "]: ")
					+ this.getContentsSingleLine();
			}

			@Override
			protected String generateTokensViewFormatting(ErrorFormatter.DisplayTokensOptions options) {
				return "(at token " + options.start() + ')';
			}
		};

		enum Something {
			ONE, TWO, THREE
		}

		final var argumentParser = new TestingParser("Testing") {{
			this.addArgument(Argument.create("testing", ArgumentType.STRING())
				.description("some description")
				.obligatory()
			);

			this.addGroup(new ArgumentGroup("a group") {{
				this.addArgument(Argument.create("this is just a bool arg"));

				this.addArgument(Argument.create("range", new IntRangeArgument(1, 10))
					.onOk((value) -> System.out.println("Range: " + value))
					.description("word ".repeat(50))
				);
				this.addArgument(Argument.create("number", new EnumArgument<>(Something.ONE))
					.positional()
					.description("Pick a number")
				);
				this.addGroup(new ArgumentGroup("a subgroup") {{
					this.exclusive();
					this.addArgument(Argument.create("string", ArgumentType.STRING())
						.description("a string")
					);
					this.addArgument(Argument.create("bool", ArgumentType.BOOLEAN())
						.description("a bool")
					);
				}});
			}});
			this.addGroup(new ArgumentGroup("another group") {{
				this.exclusive();
				this.addArgument(Argument.create("string2", ArgumentType.STRING())
					.description("a string")
					.onOk((value) -> System.out.println("String: " + value))
				);
				this.addArgument(Argument.create("bool2", ArgumentType.BOOLEAN())
					.description("a bool")
				);
				this.addGroup(new ArgumentGroup("another subgroup") {{
					this.exclusive();
					this.addArgument(Argument.create("string3", ArgumentType.STRING())
						.description("a string")
					);
					this.addArgument(Argument.create("bool3", ArgumentType.BOOLEAN())
						.description("a bool")
					);
				}});
			}});
			this.addSubCommand(new Command("cmd") {{
				this.addArgument(Argument.create("test-arg"));
			}});
		}};

		var parsedArgs = argumentParser.parseArgsExpectErrorPrint("--number five cmd --test-arg thing");
	}
}
