import lanat.*;
import lanat.argumentTypes.Parseable;
import lanat.helpRepresentation.HelpFormatter;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ManualTests {
	public static void main(String[] args) {
		HelpFormatter.lineWrapMax = 110;
		HelpFormatter.debugLayout = true;

		ErrorFormatter.generator = new ErrorFormatter.ErrorFormatterGenerator() {
			@Override
			public @NotNull String generate() {
				final var errorLevel = this.getErrorLevel();
				return this.getErrorLevelFormatter()
					.setContents("[" + errorLevel + ", " + this.getTokensViewFormatting() + "]: ")
					+ this.getContentsSingleLine();
			}

			@Override
			protected @NotNull String generateTokensViewFormatting(ErrorFormatter.DisplayTokensOptions options) {
				return "(at token " + options.start() + ')';
			}
		};

		enum Something {
			ONE, TWO, THREE
		}

		final var argumentParser = new TestingParser("Testing") {{
			this.invokeCallbacksWhen(CallbacksInvocationOption.NO_ERROR_IN_ARGUMENT);

			this.addArgument(Argument.create("testing", ArgumentType.FROM_PARSEABLE(new TestClass()))
				.description("some description")
				.onOk(value -> System.out.println("ok: " + value))
			);

			this.addArgument(Argument.create("double", ArgumentType.TRY_PARSE(Double.class))
				.description("some description")
				.onOk(value -> System.out.println("ok: " + value))
			);

			this.addSubCommand(new Command("hello") {{
				this.addArgument(Argument.create("world", ArgumentType.INTEGER_RANGE(5, 10))
					.description("a range between 5 and 10")
					.onOk(value -> System.out.println("ok: " + value))
				);
			}});
		}};

		argumentParser.parseArgsExpectErrorPrint("--testing 23 --double foo hello --world 7");
	}
}


class TestClass implements Parseable<Integer> {

	@Override
	public @NotNull ArgValueCount getArgValueCount() {
		return ArgValueCount.ONE;
	}

	@Override
	public @Nullable Integer parseValues(@NotNull String @NotNull [] args) {
		return Integer.parseInt(args[0]);
	}

	@Override
	public @Nullable TextFormatter getRepresentation() {
		return null;
	}
}