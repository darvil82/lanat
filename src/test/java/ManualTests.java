import lanat.ArgValueCount;
import lanat.Argument;
import lanat.ArgumentType;
import lanat.ErrorFormatter;
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
			this.addArgument(Argument.create("testing", ArgumentType.FROM_PARSEABLE(new TestClass()))
				.description("some description")
			);
			this.addArgument(Argument.create("double", ArgumentType.TRY_PARSE(Double.class))
				.description("some description")
				.obligatory()
				.onOk(value -> System.out.println("ok: " + value))
			);
		}};

		var parsedArgs = argumentParser.parseArgsExpectErrorPrint("--double 56.123");
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