import argparser.*;
import argparser.displayFormatter.TextFormatter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;


class StringJoiner extends ArgumentType<String> {
	@Override
	public ArgValueCount getNumberOfArgValues() {
		return new ArgValueCount(1, 3);
	}

	@Override
	public void parseValues(String[] args) {
		this.setValue("(" + String.join("), (", args) + ")");
	}
}


class TestingParser extends ArgumentParser {
	public TestingParser(String programName) {
		super(programName);
	}

	public void parseArgsExpectError(String args) {
		this.__parseArgsNoExit(args);
	}

	@Override
	public ParsedArguments parseArgs(String args) {
		var res = this.__parseArgsNoExit(args).first();
		assertNotNull(res, "The result of the parsing was null (Arguments have failed)");
		return res;
	}
}



public class UnitTests {
	private TestingParser parser;

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


	@Nested
	class ParsedValues {
		private ParsedArguments parseArgs(String args) {
			return parser.parseArgs(args);
		}

		@Test
		public void testGetSimple() {
			assertEquals("(hello), (world)", this.parseArgs("--what hello world").<String>get("what").get());
		}

		@Test
		public void testUnknownArg() {
			assertThrows(
				IllegalArgumentException.class,
				() -> this.parseArgs("--what hello world").<String>get("not-there")
			);
		}

		@Test
		public void testNestedArguments() {
			var pArgs = this.parseArgs("smth subcommand -cccc another 56");
			assertEquals(4, pArgs.<Integer>get("subcommand.c").get());
			assertEquals(4, pArgs.<Integer>get("subcommand", "c").get());

			assertEquals(56, pArgs.<Integer>get("subcommand.another.number").get());
			assertEquals(56, pArgs.<Integer>get("subcommand", "another", "number").get());
		}

		@Test
		public void testDefinedCallbacks() {
			var pArgs = this.parseArgs("smth subcommand -cccc");
			final byte[] called = {0};

			pArgs.<Integer>get("subcommand.c").defined(v -> {
				assertEquals(4, v);
				called[0]++;
			});

			pArgs.<Integer>get("subcommand.another.number").undefined(() -> called[0]++);

			assertEquals(2, called[0]);
		}
	}


	@Nested
	class TerminalOutput {
		private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
		private final PrintStream originalErr = System.err;


		@AfterEach
		public void restoreStreams() {
			System.setErr(originalErr);
		}

		@BeforeEach
		public void setStreams() {
			System.setErr(new PrintStream(errContent));
		}

		private void assertErrorOutput(String args, String expected) {
			UnitTests.this.parser.parseArgsExpectError(args);
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
			Testing <- subcommand
			Obligatory argument 'what' not used.""");
		}

		@Test
		public void testLastObligatoryArgument() {
			assertErrorOutput("foo subcommand another", """
			ERROR
			Testing foo subcommand another <-
			Obligatory argument 'number' for command 'another' not used.""");
		}

		@Test
		public void testExceedValueCount() {
			assertErrorOutput("--what [1 2 3 4 5 6 7 8 9 10]", """
			ERROR
			Testing --what [ 1 2 3 4 5 6 7 8 9 10 ]
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 10.""");
		}


		@Test
		public void testMissingValue() {
			assertErrorOutput("--what", """
			ERROR
			Testing --what <-
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 0.""");
		}

		@Test
		public void testMissingValueBeforeToken() {
			assertErrorOutput("--what subcommand", """
			ERROR
			Testing --what <- subcommand
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 0.""");
		}

		@Test
		public void testMissingValueWithTuple() {
			assertErrorOutput("--what []", """
			ERROR
			Testing --what [ ]
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 0.""");
		}

		@Test
		public void testInvalidArgumentTypeValue() {
			assertErrorOutput("foo subcommand another bar", """
			ERROR
			Testing foo subcommand another bar
			Invalid integer value: 'bar'.""");
		}

		@Test
		public void testUnmatchedToken() {
			assertErrorOutput("[foo] --unknown", """
			WARNING
			Testing [ foo ] --unknown
			Token '--unknown' does not correspond with a valid argument, value, or command.""");
		}
	}


}