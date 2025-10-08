package lanat.tests.parser;

import lanat.Argument;
import lanat.ArgumentType;
import lanat.Command;
import lanat.Group;
import lanat.argumentTypes.CounterArgumentType;
import lanat.argumentTypes.IntegerArgumentType;
import lanat.argumentTypes.StringArgumentType;
import lanat.helpRepresentation.HelpFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import textFormatter.TextFormatter;
import utils.Range;

import static org.junit.jupiter.api.Assertions.assertTrue;


class StringJoiner extends ArgumentType<String> {
	@Override
	public @NotNull Range getValueCountBounds() {
		return Range.from(1).to(3);
	}

	@Override
	public String parseValues(String @NotNull [] values) {
		return "(" + String.join("), (", values) + ")";
	}
}

class RestrictedDoubleAdder extends ArgumentType<Double> {
	public RestrictedDoubleAdder() {
		super(0.0);
	}

	@Override
	public @Nullable Double parseValues(@NotNull String @NotNull [] values) {
		return Double.parseDouble(values[0]) + this.getValue();
	}

	@Override
	public @NotNull Range getUsageCountBounds() {
		return Range.from(2).to(4);
	}

	@Override
	public @NotNull String getName() {
		return "doubleSum";
	}
}


public class TestParser {
	protected TestingParser parser;

	static {
		HelpFormatter.setLineWrapMax((short)1000); // just so we don't have to worry about line wrapping
		TextFormatter.enableSequences = false; // just so we don't have to worry about color codes

		// prefix char is set to auto by default (make sure lanat.tests run in windows too)
		Argument.Prefix.setDefaultPrefix(Argument.Prefix.MINUS);
	}

	protected TestingParser setParser() {
		return new TestingParser("Testing") {{
			this.setErrorCode(0b0100);
			this.addArgument(Argument.create(new StringJoiner(), "what")
				.positional(true)
				.required(true)
			);
			this.addArgument(Argument.create(new RestrictedDoubleAdder(), "double-adder"));
			this.addArgument(Argument.create(new StringArgumentType(), "a"));
			this.addArgument(Argument.create(new IntegerArgumentType(), "integer").defaultValue(34));

			this.addCommand(new Command("subCommand") {{
				this.setErrorCode(0b0010);
				this.addArgument(Argument.create(new CounterArgumentType(), "c"));
				this.addArgument(Argument.create(new StringJoiner(), "s", "more-strings"));

				this.addCommand(new Command("another") {{
					this.setErrorCode(0b0001);
					this.addArgument(Argument.create(new StringJoiner(), "ball"));
					this.addArgument(Argument.create(new IntegerArgumentType(), "number").positional(true).required(true));
				}});
			}});

			this.addCommand(new Command("subCommand2") {{
				this.setErrorCode(0b1000);

				this.addGroup(new Group("restricted-group") {{
					this.setRestricted(true);
					this.addArgument(Argument.createOfActionType("extra"));
					this.addArgument(Argument.create(new IntegerArgumentType(), "c").positional(true));
				}});
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
	 * {@code this.parser.parseGetValues("--%s %s".formatted(arg, values)).<T>get(arg).get();}
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