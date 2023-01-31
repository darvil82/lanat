import argparser.*;
import argparser.argumentTypes.TupleArgumentType;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


class StringJoiner extends TupleArgumentType<String> {
	public StringJoiner() {
		super(new ArgValueCount(1, 3), "");
	}

	@Override
	public String parseValues(String[] args) {
		return "(" + String.join("), (", args) + ")";
	}
}


class TestingParser extends ArgumentParser {
	public TestingParser(String programName) {
		super(programName);
	}

	public List<String> parseArgsExpectError(String args) {
		return this.__parseArgsNoExit(args).second();
	}

	public ParsedArgumentsRoot parseArgsExpectErrorPrint(String args) {
		final var parsed = this.__parseArgsNoExit(args);
		System.out.println(String.join("\n", parsed.second()));
		return parsed.first();
	}

	@Override
	public ParsedArgumentsRoot parseArgs(String args) {
		var res = this.__parseArgsNoExit(args).first();
		assertNotNull(res, "The result of the parsing was null (Arguments have failed)");
		return res;
	}
}


public class UnitTests {
	protected TestingParser parser;

	public void setParser() {
		this.parser = new TestingParser("Testing") {{
			addArgument(new Argument<>("what", new StringJoiner())
				.positional()
				.obligatory()
			);
			addArgument(new Argument<>("a", ArgumentType.BOOLEAN()));
			addSubCommand(new Command("subcommand") {{
				addArgument(new Argument<>("c", ArgumentType.COUNTER()));
				addArgument(new Argument<>('s', "more-strings", new StringJoiner()));
				addSubCommand(new Command("another") {{
					addArgument(new Argument<>("ball", new StringJoiner()));
					addArgument(new Argument<>("number", ArgumentType.INTEGER()).positional().obligatory());
				}});
			}});
		}};
	}

	@BeforeEach
	public void setup() {
		this.setParser();
	}

	/**
	 * Shorthand for parsing arguments and getting the value of an argument.
	 * Same as
	 * <pre>
	 * {@code this.parser.parseArgs("--%s %s".formatted(arg, values)).<T>get(arg).get();}
	 * </pre>
	 */
	protected <T> T parseArg(String arg, String values) {
		return this.parser.parseArgs("--%s %s".formatted(arg.trim(), values)).<T>get(arg).get();
	}

	/**
	 * Shorthand for checking if an argument value is not present.
	 * Same as
	 * <pre>
	 * {@code assertNull(this.parser.parseArgs("").get(arg).get());}
	 * </pre>
	 */
	protected void assertNotPresent(String arg) {
		assertNull(this.parser.parseArgs("").get(arg).get());
	}
}