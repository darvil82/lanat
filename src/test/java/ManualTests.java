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
			this.addArgument(new Argument<>("testing", ArgumentType.STRING())
				.description("some description")
				.obligatory()
			);

			this.addGroup(new ArgumentGroup("a group") {{
				this.addArgument(Argument.simple("this is just a bool arg"));

				this.addArgument(new Argument<>("range", new IntRangeArgument(1, 10))
					.onOk((value) -> System.out.println("Range: " + value))
					.description("word ".repeat(50))
				);
				this.addArgument(new Argument<>("number", new EnumArgument<>(Something.ONE))
					.positional()
					.description("Pick a number")
				);
				this.addGroup(new ArgumentGroup("a subgroup") {{
					this.exclusive();
					this.addArgument(new Argument<>("string", ArgumentType.STRING())
						.description("a string")
					);
					this.addArgument(new Argument<>("bool", ArgumentType.BOOLEAN())
						.description("a bool")
					);
				}});
			}});
			this.addGroup(new ArgumentGroup("another group") {{
				this.exclusive();
				this.addArgument(new Argument<>("string2", ArgumentType.STRING())
					.description("a string")
					.onOk((value) -> System.out.println("String: " + value))
				);
				this.addArgument(new Argument<>("bool2", ArgumentType.BOOLEAN())
					.description("a bool")
				);
				this.addGroup(new ArgumentGroup("another subgroup") {{
					this.exclusive();
					this.addArgument(new Argument<>("string3", ArgumentType.STRING())
						.description("a string")
					);
					this.addArgument(new Argument<>("bool3", ArgumentType.BOOLEAN())
						.description("a bool")
					);
				}});
			}});
			this.addSubCommand(new Command("cmd") {{
				this.addArgument(Argument.simple("test-arg"));
			}});
		}};

		var parsedArgs = argumentParser.parseArgsExpectErrorPrint("--number five cmd --test-arg thing");
	}
}
