package lanat.test.units.commandTemplates;

import lanat.ArgumentParser;
import lanat.CLInput;
import lanat.CommandTemplate;
import lanat.exceptions.CommandTemplateException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestFromInto {
	private static <T extends CommandTemplate> @NotNull T parseFromInto(
		@NotNull Class<T> templateClass,
		@NotNull CLInput input
	) {
		return ArgumentParser.parseFromInto(templateClass, input, ArgumentParser.AfterParseOptions::printErrors);
	}

	@Test
	@DisplayName("test parseFromInto method")
	public void testParseFromInto() {
		final var result = TestFromInto.parseFromInto(
			CmdTemplates.CmdTemplate1.class, CLInput.from("--number 56 --text hello -f cmd1-1 --number 54.0")
		);

		assertTrue(result.flag);
		assertEquals(56, result.number);
		assertEquals("hello", result.text);
		assertEquals(54.0f, result.cmd2.number);
	}

	@Test
	@DisplayName("test command template with sub-command but no accessor")
	public void testCmdTemplate2() {
		assertThrows(CommandTemplateException.class, () ->
			TestFromInto.parseFromInto(
				CmdTemplates.CmdTemplate2.class, CLInput.from("")
			)
		);
	}
}