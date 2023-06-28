package lanat.test.units;

import lanat.CLInput;
import lanat.exceptions.ArgumentAlreadyExistsException;
import lanat.test.UnitTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestMisc extends UnitTests {
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
		assertEquals(0b0100, this.parser.parse(CLInput.from("")).getErrorCode());

		// test sub-command failing (its error code is 0b0010)
		assertEquals(0b0110, this.parser.parse(CLInput.from("subCommand -s")).getErrorCode());

		// test innermost sub-command failing (its error code is 0b0001)
		assertEquals(0b0111, this.parser.parse(CLInput.from("subCommand another")).getErrorCode());

		// test sub-command2 failing (its error code is 0b1000)
		assertEquals(0b1100, this.parser.parse(CLInput.from("subCommand2 hello")).getErrorCode());
	}
}
