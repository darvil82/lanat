package lanat.test.units;

import lanat.test.UnitTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTuples extends UnitTests {
	@Test
	@DisplayName("Test tuples")
	public void testTuples() {
		var value = this.parser.parse("--what [multiple values here]")
			.getResult()
			.<String>get("what");

		assertTrue(value.isPresent());
		assertEquals("(multiple), (values), (here)", value.get());
	}
}