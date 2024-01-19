package lanat.test.units;

import lanat.test.UnitTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTerminalOutput extends UnitTests {
	private void assertErrorOutput(String args, String expected) {
		final var errors = this.parser.parseGetErrors(args);
		System.out.printf("Test error output:%n%s%n", String.join(System.lineSeparator(), errors));

		// remove all the decorations to not make the tests a pain to write
		assertTrue(
			errors.stream()
				.map(e -> e.replaceAll(" *[│─└┌\r] ?", "").strip())
				.toList()
				.contains(expected)
		);
	}

	private void assertNoErrorOutput(String args) {
		final var errors = this.parser.parseGetErrors(args);
		System.out.printf("Test error output:%n%s%n", String.join(System.lineSeparator(), errors));
		assertTrue(errors.isEmpty());
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
			Testing --what [ -> 1 2 3 4 5 6 7 8 9 10 <- ]
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
		this.assertErrorOutput("foo --double-adder [5.0]", """
			ERROR
			Testing foo -> --double-adder [ 5.0 ] <-
			Argument 'double-adder' was used an incorrect amount of times.
			Expected from 2 to 4 usages, but was used 1 time.""");

		this.assertErrorOutput("foo --double-adder 5.0 --double-adder 5.0 --double-adder 5.0 --double-adder 5.0 --double-adder 5.0", """
			ERROR
			Testing foo --double-adder 5.0 --double-adder 5.0 --double-adder 5.0 --double-adder 5.0 -> --double-adder 5.0 <-
			Argument 'double-adder' was used an incorrect amount of times.
			Expected from 2 to 4 usages, but was used 5 times.""");
	}

	@Test
	@DisplayName("Test group restriction error")
	public void testGroupRestrictionError() {
		this.assertErrorOutput("foo subCommand2 --extra --c 5", """
			ERROR
			Testing foo subCommand2 --extra -> --c 5 <-
			Multiple arguments in restricted group 'restricted-group' used.""");
	}

	@Test
	@DisplayName("Test space required error")
	public void testSpaceRequiredError() {
		this.assertErrorOutput("[foo]--what 1", """
			ERROR
			Testing [foo->]-<--what 1
			A space is required between these characters.""");

		this.assertErrorOutput("foo --what'1'", """
			ERROR
			Testing foo --wha->t'<-1'
			A space is required between these characters.""");

		this.assertErrorOutput("'foo'--what 1", """
			ERROR
			Testing 'foo->'-<--what 1
			A space is required between these characters.""");

		this.assertNoErrorOutput("[foo]");
		this.assertNoErrorOutput("--what='1'");
	}

	@Test
	@DisplayName("Test tuple already open error")
	public void testTupleAlreadyOpenError() {
		this.assertErrorOutput("test subCommand [1 [2 3", """
			ERROR
			Testing test subCommand [1 ->[<-2 3
			Tuple already open.""");
	}

	@Test
	@DisplayName("Test tuple not closed error")
	public void testTupleNotClosedError() {
		this.assertErrorOutput("--what [1 2 3", """
			ERROR
			Testing --what ->[1 2 3<-
			Tuple not closed.""");
	}

	@Test
	@DisplayName("Test unexpected tuple close error")
	public void testUnexpectedTupleCloseError() {
		this.assertErrorOutput("--what 1]", """
			ERROR
			Testing --what 1->]<-
			Unexpected tuple close.""");
	}

	@Test
	@DisplayName("Test string not closed error")
	public void testStringNotClosedError() {
		this.assertErrorOutput("--what '1 2 3", """
			ERROR
			Testing --what ->'1 2 3<-
			String not closed.""");
	}
}