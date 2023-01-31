import argparser.ParsedArguments;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestParsedValues extends UnitTests {
	private ParsedArguments parseArgs(String args) {
		return this.parser.parseArgs(args);
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
		var parsedArgs = this.parseArgs("smth subcommand -cccc another 56");
		assertEquals(4, parsedArgs.<Integer>get("subcommand.c").get());
		assertEquals(4, parsedArgs.<Integer>get("subcommand", "c").get());

		assertEquals(56, parsedArgs.<Integer>get("subcommand.another.number").get());
		assertEquals(56, parsedArgs.<Integer>get("subcommand", "another", "number").get());
	}

	@Test
	public void testDefinedCallbacks() {
		var parsedArgs = this.parseArgs("smth subcommand -cccc");
		final byte[] called = { 0 };

		parsedArgs.<Integer>get("subcommand.c").defined(v -> {
			assertEquals(4, v);
			called[0]++;
		});

		parsedArgs.<Integer>get("subcommand.another.number").undefined(() -> called[0]++);

		assertEquals(2, called[0]);
	}

	@Test
	public void testToOptional() {
		var parsedArgs = this.parseArgs("smth subcommand -cccc");
		parsedArgs.get("subcommand.c").asOptional().ifPresent(v -> assertEquals(4, v));
		parsedArgs.get("subcommand.another.number").asOptional().ifPresent(v -> assertEquals(56, v));
		assertTrue(parsedArgs.get("subcommand.c").asOptional().isPresent());
	}

	@Test
	public void testArrayDefinedMethod() {
		var parsedArgs = this.parseArgs("foo bar qux");

		{
			String[] value = new String[1];

			if (parsedArgs.get("what").defined(value)) {
				assertEquals(value[0], "(foo), (bar), (qux)");
			} else {
				fail("The value was not defined");
			}
		}

		{
			String[] value = new String[1];

			if (parsedArgs.get("a").defined(value)) {
				fail("The value was defined");
			}
			assertNull(value[0]);
		}

		{
			String[] value = new String[4];

			assertThrows(
				IllegalArgumentException.class,
				() -> parsedArgs.get("what").defined(value)
			);
		}
	}
}