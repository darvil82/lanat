package lanat.test.units;

import lanat.test.UnitTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTerminalOutput extends UnitTests {
	private void assertErrorOutput(String args, String expected) {
		final var errors = this.parser.parseGetErrors(args);
		System.out.printf("Test error output:\n%s", errors.get(0));

		// remove all the decorations to not make the tests a pain to write
		assertEquals(
			expected,
			errors.get(0)
				// the reason we replace \r here is that windows uses CRLF (I hate windows)
				.replaceAll(" *[│─└┌\r] ?", "")
				.strip()
		);
	}

	@Test
	@DisplayName("Arrow points to the root command name on first required argument missing")
	public void testFirstRequiredArgument() {
		this.assertErrorOutput("subCommand", """
			ERROR
			Testing <- subCommand
			Required argument 'what' not used.""");
	}

	@Test
	@DisplayName("Arrow points to the last token on last required argument missing")
	public void testLastRequiredArgument() {
		this.assertErrorOutput("foo subCommand another", """
			ERROR
			Testing foo subCommand another <-
			Required argument 'number' for command 'another' not used.""");
	}

	@Test
	@DisplayName("Tuple is highlighted correctly")
	public void testExceedValueCountTuple() {
		this.assertErrorOutput("--what [1 2 3 4 5 6 7 8 9 10]", """
			ERROR
			Testing --what -> [ 1 2 3 4 5 6 7 8 9 10 ] <-
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
		this.assertErrorOutput("--what subCommand", """
			ERROR
			Testing --what <- subCommand
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 0.""");
	}

	@Test
	@DisplayName("Empty tuple highlighted correctly when values are missing")
	public void testMissingValueWithTuple() {
		this.assertErrorOutput("--what []", """
			ERROR
			Testing --what -> [ ] <-
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 0.""");
	}

	@Test
	@DisplayName("Test invalid argument type value")
	public void testInvalidArgumentTypeValue() {
		this.assertErrorOutput("foo subCommand another bar", """
			ERROR
			Testing foo subCommand another bar <-
			Invalid Integer value: 'bar'.""");
	}

	@Test
	@DisplayName("Test unmatched token")
	public void testUnmatchedToken() {
		this.assertErrorOutput("[foo] --unknown", """
			WARNING
			Testing [ foo ] --unknown <-
			Token '--unknown' does not correspond with a valid argument, argument list, value, or command.""");
	}

	@Test
	@DisplayName("Test incorrect usage count")
	public void testIncorrectUsageCount() {
		this.assertErrorOutput("foo --double-adder 5.0", """
			ERROR
			Testing foo -> --double-adder 5.0 <-
			Argument 'double-adder' was used an incorrect amount of times.
			Expected from 2 to 4 usages, but was used 1 time.""");

		this.assertErrorOutput("foo --double-adder 5.0 --double-adder 5.0 --double-adder 5.0 --double-adder 5.0 --double-adder 5.0", """
			ERROR
			Testing foo --double-adder 5.0 --double-adder 5.0 --double-adder 5.0 --double-adder 5.0 -> --double-adder 5.0 <-
			Argument 'double-adder' was used an incorrect amount of times.
			Expected from 2 to 4 usages, but was used 5 times.""");
	}
}