import argparser.*;
import argparser.argumentTypes.TupleArgumentType;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;


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
	private TestingParser parser;

	public TestingParser getParser() {
		return parser;
	}

	@BeforeEach
	public void setup() {
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
}