package lanat.test;

import lanat.*;
import lanat.argumentTypes.TupleArgumentType;
import lanat.helpRepresentation.HelpFormatter;
import lanat.utils.Range;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


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


class TestingParser extends ArgumentParser {
	public TestingParser(String programName, String description) {
		super(programName, description);
	}

	public TestingParser(String programName) {
		super(programName);
	}

	public List<String> parseGetErrors(String args) {
		return this.parse(args).getErrors();
	}

	public @NotNull ParsedArgumentsRoot parseGetValues(@NotNull String args) {
		var res = this.parse(args).getParsedArguments();
		assertNotNull(res, "The result of the parsing was null (Arguments have failed)");
		return res;
	}
}


public class UnitTests {
	protected TestingParser parser;

	static {
		HelpFormatter.lineWrapMax = 1000; // just so we don't have to worry about line wrapping
		TextFormatter.enableSequences = false; // just so we don't have to worry about color codes
	}

	public void setParser() {
		this.parser = new TestingParser("Testing") {{
			this.addArgument(Argument.create("what", new StringJoiner())
				.positional()
				.obligatory()
			);
			this.addArgument(Argument.create("double-adder", new RestrictedDoubleAdder()));
			this.addArgument(Argument.create("a", ArgumentType.STRING()));
			this.addCommand(new Command("subCommand") {{
				this.addArgument(Argument.create("c", ArgumentType.COUNTER()));
				this.addArgument(Argument.create('s', "more-strings", new StringJoiner()));
				this.addCommand(new Command("another") {{
					this.addArgument(Argument.create("ball", new StringJoiner()));
					this.addArgument(Argument.create("number", ArgumentType.INTEGER()).positional().obligatory());
				}});
			}});
		}};
	}

	@BeforeEach
	public final void setup() {
		this.setParser();
	}

	/**
	 * Shorthand for parsing arguments and getting the value of an argument. Same as
	 * <pre>
	 * {@code this.parser.parseArgs("--%s %s".formatted(arg, values)).<T>get(arg).get();}
	 * </pre>
	 */
	protected <T> T parseArg(@NotNull String arg, @NotNull String values) {
		return this.parser.parseGetValues("--%s %s".formatted(arg.trim(), values)).<T>get(arg).get();
	}

	/**
	 * Shorthand for checking if an argument value is not present. Same as
	 * <pre>
	 * {@code assertNull(this.parser.parseArgs("").get(arg).get());}
	 * </pre>
	 */
	protected void assertNotPresent(@NotNull String arg) {
		assertNull(this.parser.parseGetValues("").get(arg).get());
	}
}