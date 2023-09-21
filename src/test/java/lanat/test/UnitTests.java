package lanat.test;

import lanat.Argument;
import lanat.ArgumentType;
import lanat.Command;
import lanat.argumentTypes.CounterArgumentType;
import lanat.argumentTypes.IntegerArgumentType;
import lanat.argumentTypes.StringArgumentType;
import lanat.argumentTypes.TupleArgumentType;
import lanat.helpRepresentation.HelpFormatter;
import lanat.utils.Range;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertTrue;


class StringJoiner extends TupleArgumentType<String> {
	public StringJoiner() {
		super(Range.from(1).to(3), "");
	}

	@Override
	public String parseValues(String @NotNull [] args) {
		return "(" + String.join("), (", args) + ")";
	}
}

class RestrictedDoubleAdder extends ArgumentType<Double> {
	public RestrictedDoubleAdder() {
		super(0.0);
	}

	@Override
	public @Nullable Double parseValues(@NotNull String @NotNull [] args) {
		return Double.parseDouble(args[0]) + this.getValue();
	}

	@Override
	public @NotNull Range getRequiredUsageCount() {
		return Range.from(2).to(4);
	}
}


public class UnitTests {
	protected TestingParser parser;

	static {
		HelpFormatter.lineWrapMax = 1000; // just so we don't have to worry about line wrapping
		TextFormatter.enableSequences = false; // just so we don't have to worry about color codes
	}

	protected TestingParser setParser() {
		return new TestingParser("Testing") {{
			this.setErrorCode(0b0100);
			this.addArgument(Argument.create(new StringJoiner(), "what")
				.positional()
				.required()
			);
			this.addArgument(Argument.create(new RestrictedDoubleAdder(), "double-adder"));
			this.addArgument(Argument.create(new StringArgumentType(), "a"));

			this.addCommand(new Command("subCommand") {{
				this.setErrorCode(0b0010);
				this.addArgument(Argument.create(new CounterArgumentType(), "c"));
				this.addArgument(Argument.create(new StringJoiner(), 's', "more-strings"));

				this.addCommand(new Command("another") {{
					this.setErrorCode(0b0001);
					this.addArgument(Argument.create(new StringJoiner(), "ball"));
					this.addArgument(Argument.create(new IntegerArgumentType(), "number").positional().required());
				}});
			}});

			this.addCommand(new Command("subCommand2") {{
				this.setErrorCode(0b1000);
				this.addArgument(Argument.create(new IntegerArgumentType(), 'c').positional());
			}});
		}};
	}

	@BeforeEach
	public final void setup() {
		this.parser = this.setParser();
	}

	/**
	 * Shorthand for parsing arguments and getting the value of an argument. Same as
	 * <pre>
	 * {@code this.parser.parseArgs("--%s %s".formatted(arg, values)).<T>get(arg).get();}
	 * </pre>
	 */
	protected <T> T parseArg(@NotNull String arg, @NotNull String values) {
		return this.parser.parseGetValues("--%s %s".formatted(arg.strip(), values)).<T>get(arg).orElse(null);
	}

	/**
	 * Shorthand for checking if an argument value is not present. Same as
	 * <pre>
	 * {@code assertNull(this.parser.parseArgs("").get(arg).get());}
	 * </pre>
	 */
	protected void assertNotPresent(@NotNull String arg) {
		assertTrue(this.parser.parseGetValues("").get(arg).isEmpty());
	}
}