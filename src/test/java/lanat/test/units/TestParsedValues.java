package lanat.test.units;

import lanat.ParsedArgumentsRoot;
import lanat.exceptions.ArgumentNotFoundException;
import lanat.test.UnitTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestParsedValues extends UnitTests {
	private ParsedArgumentsRoot parseArgs(String args) {
		return this.parser.parseGetValues(args);
	}

	@Test
	@DisplayName("Test the get() method")
	public void testGetSimple() {
		assertEquals("(hello), (world)", this.parseArgs("--what hello world").<String>get("what").get());
	}

	@Test
	@DisplayName("Exception thrown when querying an invalid argument")
	public void testUnknownArg() {
		assertThrows(
			ArgumentNotFoundException.class,
			() -> this.parseArgs("--what hello world").<String>get("not-there")
		);
	}

	@Test
	@DisplayName("Test querying parsed values from arguments in Sub-Commands")
	public void testNestedArguments() {
		var parsedArgs = this.parseArgs("smth subCommand -cccc another 56");
		assertEquals(4, parsedArgs.<Integer>get("subCommand.c").get());
		assertEquals(4, parsedArgs.<Integer>get("subCommand", "c").get());

		assertEquals(56, parsedArgs.<Integer>get("subCommand.another.number").get());
		assertEquals(56, parsedArgs.<Integer>get("subCommand", "another", "number").get());
	}

	@Test
	@DisplayName("Test the defined() callbacks")
	public void testDefinedCallbacks() {
		var parsedArgs = this.parseArgs("smth subCommand -cccc");
		final byte[] called = { 0 };

		parsedArgs.<Integer>get("subCommand.c").defined(v -> {
			assertEquals(4, v);
			called[0]++;
		});

		parsedArgs.<Integer>get("subCommand.another.number").undefined(() -> called[0]++);

		assertEquals(2, called[0]);
	}

	@Test
	@DisplayName("Test the toOptional() method")
	public void testToOptional() {
		var parsedArgs = this.parseArgs("smth subCommand -cccc");
		parsedArgs.get("subCommand.c").asOptional().ifPresent(v -> assertEquals(4, v));
		parsedArgs.get("subCommand.another.number").asOptional().ifPresent(v -> assertEquals(56, v));
		assertTrue(parsedArgs.get("subCommand.c").asOptional().isPresent());
	}

	@Test
	@DisplayName("Test the defined() overload for single-value arrays")
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

	@Test
	@DisplayName("Test the forward value")
	public void testForwardValue() {
		{
			Optional<String> parsedArgs = this.parseArgs("foo -- hello world").getForwardValue();
			assertTrue(parsedArgs.isPresent());
			assertEquals("hello world", parsedArgs.get());
		}

		{
			Optional<String> parsedArgs = this.parseArgs("foo").getForwardValue();
			assertFalse(parsedArgs.isPresent());
		}
	}
}