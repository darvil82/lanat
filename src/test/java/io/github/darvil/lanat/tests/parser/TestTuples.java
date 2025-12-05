package io.github.darvil.lanat.tests.parser;

import io.github.darvil.lanat.TupleChar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTuples extends TestParser {
	@Test
	@DisplayName("Test tuples")
	public void testTuples() {
		assertEquals("(multiple), (values), (here)", this.parseArg("what", "[multiple values here]"));
		assertEquals("(hello), (world)", this.parseArg("what", "['hello' 'world']"));
		assertEquals("(hello)", this.parser.parseGetValues("-what=['hello']").get("what").orElse(null));
		assertEquals("(hello), (world)", this.parser.parseGetValues("-what=['hello' world]").get("what").orElse(null));
	}

	@Test
	@DisplayName("Test other tuple chars")
	public void testOtherTupleChars() {
		TupleChar.current = TupleChar.PARENTHESIS;
		assertEquals("(multiple), (values), (here)", this.parseArg("what", "(multiple values here)"));
		TupleChar.current = TupleChar.ANGLE_BRACKETS;
		assertEquals("(multiple), (values), (here)", this.parseArg("what", "<multiple values here>"));
		TupleChar.current = TupleChar.SQUARE_BRACKETS; // reset
	}
}