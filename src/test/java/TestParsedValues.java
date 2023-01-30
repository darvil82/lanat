import argparser.ParsedArguments;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestParsedValues extends UnitTests {
	private ParsedArguments parseArgs(String args) {
		return this.getParser().parseArgs(args);
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
		final byte[] called = { 0 };

		pArgs.<Integer>get("subcommand.c").defined(v -> {
			assertEquals(4, v);
			called[0]++;
		});

		pArgs.<Integer>get("subcommand.another.number").undefined(() -> called[0]++);

		assertEquals(2, called[0]);
	}
}