package lanat.tests.parser;

import lanat.exceptions.ArgumentAlreadyExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class TestMisc extends TestParser {
	@Test
	@DisplayName("check duplicate names in arguments")
	public void testDuplicateNames() {
		assertThrows(
			ArgumentAlreadyExistsException.class,
			() -> this.parser.getArgument("what").addNames("a"),
			"check duplicate names after initialization"
		);
	}

	@Test
	@DisplayName("check error codes are correct")
	public void testErrorCodes() {
		// test first command failing (its error code is 0b0100)
		assertEquals(0b0100, this.parser.parse("").getErrorCode());

		// test sub-command failing (its error code is 0b0010)
		assertEquals(0b0110, this.parser.parse("subCommand -s").getErrorCode());

		// test innermost sub-command failing (its error code is 0b0001)
		assertEquals(0b0111, this.parser.parse("subCommand another").getErrorCode());

		// test sub-command2 failing (its error code is 0b1000)
		assertEquals(0b1100, this.parser.parse("subCommand2 hello").getErrorCode());
	}

	@Test
	@DisplayName("Test backslash escapes")
	public void testBackslashEscaping() {
		assertEquals("([hello]), ('world')", this.parseArg("what", "\\[hello\\] \\'world\\'"));
		assertEquals("(\\)", this.parseArg("what", "\\"));
		assertEquals("(test\\)", this.parseArg("what", "test\\"));
	}
}
