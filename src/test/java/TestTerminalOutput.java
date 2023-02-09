import lanat.utils.UtlString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTerminalOutput extends UnitTests {
	private void assertErrorOutput(String args, String expected) {
		final var errors = this.parser.parseArgsExpectError(args);
		// remove all the decorations to not make the tests a pain to write
		assertEquals(
			expected,
			UtlString.removeSequences(errors.get(0))
				// the reason we replace \r here is that windows uses CRLF (I hate windows)
				.replaceAll(" *[│─└┌\r] ?", "")
				.strip()
		);
		System.out.printf("Test error output:\n%s", errors.get(0));
	}

	@Test
	@DisplayName("Arrow points to the root command name on first obligatory argument missing")
	public void testFirstObligatoryArgument() {
		this.assertErrorOutput("subcommand", """
			ERROR
			Testing <- subcommand
			Obligatory argument 'what' not used.""");
	}

	@Test
	@DisplayName("Arrow points to the last token on last obligatory argument missing")
	public void testLastObligatoryArgument() {
		this.assertErrorOutput("foo subcommand another", """
			ERROR
			Testing foo subcommand another <-
			Obligatory argument 'number' for command 'another' not used.""");
	}

	@Test
	@DisplayName("Tuple is highlighted correctly")
	public void testExceedValueCount() {
		this.assertErrorOutput("--what [1 2 3 4 5 6 7 8 9 10]", """
			ERROR
			Testing --what [ 1 2 3 4 5 6 7 8 9 10 ]
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 10.""");
	}


	@Test
	@DisplayName("Arrow points to the last token on last argument missing value")
	public void testMissingValue() {
		this.assertErrorOutput("--what", """
			ERROR
			Testing --what <-
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 0.""");
	}

	@Test
	@DisplayName("Arrow points to correct token on missing value before token")
	public void testMissingValueBeforeToken() {
		this.assertErrorOutput("--what subcommand", """
			ERROR
			Testing --what <- subcommand
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 0.""");
	}

	@Test
	@DisplayName("Empty tuple highlighted correctly when values are missing")
	public void testMissingValueWithTuple() {
		this.assertErrorOutput("--what []", """
			ERROR
			Testing --what [ ]
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 0.""");
	}

	@Test
	@DisplayName("Test invalid argument type value")
	public void testInvalidArgumentTypeValue() {
		this.assertErrorOutput("foo subcommand another bar", """
			ERROR
			Testing foo subcommand another bar
			Invalid integer value: 'bar'.""");
	}

	@Test
	@DisplayName("Test unmatched token")
	public void testUnmatchedToken() {
		this.assertErrorOutput("[foo] --unknown", """
			WARNING
			Testing [ foo ] --unknown
			Token '--unknown' does not correspond with a valid argument, value, or command.""");
	}
}