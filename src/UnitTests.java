import argparser.*;
import argparser.displayFormatter.TextFormatter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;


class StringJoiner extends ArgumentType<String> {
	@Override
	public ArgValueCount getNumberOfArgValues() {
		return new ArgValueCount(1, 3);
	}

	@Override
	public void parseValues(String[] args) {
		this.value = "(" + String.join("), (", args) + ")";
	}
}


class TestingParser extends ArgumentParser {
	public TestingParser(String programName) {
		super(programName);
	}

	public ParsedArguments parseArgs(String args) {
		return this.__parseArgsNoExit(args).first();
	}
}

public class UnitTests {
	private ArgumentParser parser;

	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	private final PrintStream originalErr = System.err;

	@AfterEach
	public void restoreStreams() {
		System.setErr(originalErr);
	}

	@BeforeEach
	public void setUp() {
		System.setErr(new PrintStream(errContent));

		this.parser = new TestingParser("Testing") {{
			addArgument(new Argument<>("what", new StringJoiner())
				.onOk(t -> System.out.println("wow look a string: '" + t + "'"))
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

	private void assertErrorOutput(String args, String expected) {
		this.parser.parseArgs(args);
		// remove all the decorations to not make the tests a pain to write
		assertEquals(
			expected,
			TextFormatter.removeSequences(errContent.toString())
				// the reason we replace \r here is that windows uses CRLF (I hate windows)
				.replaceAll(" *[│─└┌\r] ?", "")
				.strip()
		);
		System.out.printf("Test error output:\n%s", errContent);
	}

	@Test
	public void testFirstObligatoryArgument() {
		assertErrorOutput("subcommand", """
			ERROR
			<- subcommand
			Obligatory argument 'what' not used.""");
	}

	@Test
	public void testLastObligatoryArgument() {
		assertErrorOutput("foo subcommand another", """
			ERROR
			foo subcommand another <-
			Obligatory argument 'number' for command 'another' not used.""");
	}

	@Test
	public void testExceedValueCount() {
		assertErrorOutput("--what [1 2 3 4 5 6 7 8 9 10]", """
			ERROR
			--what [ 1 2 3 4 5 6 7 8 9 10 ]
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 10.""");
	}


	@Test
	public void testMissingValue() {
		assertErrorOutput("--what", """
			ERROR
			--what <-
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 0.""");
	}

	@Test
	public void testMissingValueBeforeToken() {
		assertErrorOutput("--what subcommand", """
			ERROR
			--what <- subcommand
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 0.""");
	}

	@Test
	public void testMissingValueWithTuple() {
		assertErrorOutput("--what []", """
			ERROR
			--what [ ]
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 0.""");
	}

	@Test
	public void testInvalidArgumentTypeValue() {
		assertErrorOutput("foo subcommand another bar", """
			ERROR
			foo subcommand another bar
			Invalid integer value: 'bar'.""");
	}

	@Test
	public void testUnmatchedToken() {
		assertErrorOutput("[foo] --unknown", """
			WARNING
			[ foo ] --unknown
			Token '--unknown' does not correspond with a valid argument, value, or command.""");
	}
}