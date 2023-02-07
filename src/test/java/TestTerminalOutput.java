import lanat.utils.UtlString;
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
	public void testFirstObligatoryArgument() {
		this.assertErrorOutput("subcommand", """
			ERROR
			Testing <- subcommand
			Obligatory argument 'what' not used.""");
	}

	@Test
	public void testLastObligatoryArgument() {
		this.assertErrorOutput("foo subcommand another", """
			ERROR
			Testing foo subcommand another <-
			Obligatory argument 'number' for command 'another' not used.""");
	}

	@Test
	public void testExceedValueCount() {
		this.assertErrorOutput("--what [1 2 3 4 5 6 7 8 9 10]", """
			ERROR
			Testing --what [ 1 2 3 4 5 6 7 8 9 10 ]
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 10.""");
	}


	@Test
	public void testMissingValue() {
		this.assertErrorOutput("--what", """
			ERROR
			Testing --what <-
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 0.""");
	}

	@Test
	public void testMissingValueBeforeToken() {
		this.assertErrorOutput("--what subcommand", """
			ERROR
			Testing --what <- subcommand
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 0.""");
	}

	@Test
	public void testMissingValueWithTuple() {
		this.assertErrorOutput("--what []", """
			ERROR
			Testing --what [ ]
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 0.""");
	}

	@Test
	public void testInvalidArgumentTypeValue() {
		this.assertErrorOutput("foo subcommand another bar", """
			ERROR
			Testing foo subcommand another bar
			Invalid integer value: 'bar'.""");
	}

	@Test
	public void testUnmatchedToken() {
		this.assertErrorOutput("[foo] --unknown", """
			WARNING
			Testing [ foo ] --unknown
			Token '--unknown' does not correspond with a valid argument, value, or command.""");
	}
}