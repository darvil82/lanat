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
		assertEquals("(hello), (world)", this.parseArgs("--what hello world").<String>get("what").orElse(null));
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
		assertEquals(4, parsedArgs.<Integer>get("subCommand.c").orElse(null));
		assertEquals(4, parsedArgs.<Integer>get("subCommand", "c").orElse(null));

		assertEquals(56, parsedArgs.<Integer>get("subCommand.another.number").orElse(null));
		assertEquals(56, parsedArgs.<Integer>get("subCommand", "another", "number").orElse(null));
	}

	@Test
	@DisplayName("Test values present or not")
	public void testDefinedCallbacks() {
		var parsedArgs = this.parseArgs("smth subCommand -cccc");
		final byte[] called = { 0 };

		parsedArgs.<Integer>get("subCommand.c").ifPresent(v -> {
			assertEquals(4, v);
			called[0]++;
		});

		parsedArgs.<Integer>get("subCommand.another.number").ifPresentOrElse((v) -> {}, () -> called[0]++);

		assertEquals(2, called[0]);
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