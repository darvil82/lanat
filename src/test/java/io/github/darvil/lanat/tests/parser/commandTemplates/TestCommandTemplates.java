package io.github.darvil.lanat.tests.parser.commandTemplates;

import io.github.darvil.lanat.Command;
import io.github.darvil.lanat.exceptions.ArgumentNotFoundException;
import io.github.darvil.lanat.tests.parser.TestParser;
import io.github.darvil.lanat.tests.parser.TestingParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestCommandTemplates extends TestParser {
	@Override
	protected TestingParser setParser() {
		return new TestingParser(CmdTemplates.CmdTemplate1.class) {{
			this.addCommand(new Command(CmdTemplates.CmdTemplate1.CmdTemplate1_1.class));
		}};
	}

	@Test
	@DisplayName("assert CmdTemplate1 instance is created and arguments are set")
	public void testCmdTemplate1() {
		assertEquals(56, this.<Integer>parseArg("number", "56"));
		assertEquals("hello", this.<String>parseArg("text", "hello"));
		assertTrue(this.<Boolean>parseArg("name1", ""));
		assertTrue(this.<Boolean>parseArg("f", ""));
		assertThrows(
			ArgumentNotFoundException.class,
			() -> this.parseArg("flag", "")
		);
	}

	@Test
	@DisplayName("test into method")
	public void testInto() {
		final var result = this.parser.parse("--number 56 --text hello -f")
			.into(CmdTemplates.CmdTemplate1.class);

		assertTrue(result.flag);
		assertEquals(56, result.number);
		assertEquals("hello", result.text);
		assertNull(result.cmd2.number);
	}

	@Test
	@DisplayName("test ParsedArgumentValue wrapper")
	public void testParsedArgumentValue() {
		final var result = this.parser.parse("cmd1-1 --number2 14")
			.into(CmdTemplates.CmdTemplate1.class);

		assertTrue(result.cmd2.number2.isPresent());
		assertEquals(14, result.cmd2.number2.get());

		assertTrue(
			this.parser.parse("")
				.into(CmdTemplates.CmdTemplate1.class).cmd2.number2.isEmpty()
		);
	}

	@Test
	@DisplayName("test default values for arguments")
	public void testDefaultValues() {
		{
			final var result = this.parser.parse("")
				.into(CmdTemplates.CmdTemplate1.class);

			assertTrue(result.numberParsedArgValue.isPresent());
			assertEquals(0, result.numberParsedArgValue.get());
		}

		{
			final var result = this.parser.parse("--numberParsedArgValue 56")
				.into(CmdTemplates.CmdTemplate1.class);

			assertTrue(result.numberParsedArgValue.isPresent());
			assertEquals(56, result.numberParsedArgValue.get());
		}
	}
}