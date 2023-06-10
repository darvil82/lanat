package lanat.test.units;

import lanat.exceptions.ArgumentAlreadyExistsException;
import lanat.test.UnitTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestMisc extends UnitTests {
	@Test
	@DisplayName("check duplicate names in arguments")
	public void duplicateNames() {
		assertThrows(
			ArgumentAlreadyExistsException.class,
			() -> this.parser.getArgument("what").addNames("a"),
			"check duplicate names after initialization"
		);
	}
}
