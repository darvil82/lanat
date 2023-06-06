package lanat.test.manualTests;

import lanat.ArgumentParser;
import lanat.CLInput;
import org.junit.jupiter.api.Test;

public final class ManualTests {
	@Test
	public void main() {
		String input = "--help --string hello --number 67 sub-command -ccc";

		var parsed = ArgumentParser.parseFromInto(CommandTemplateExample.class, CLInput.from(input));

		parsed.string
			.defined(s -> System.out.println("Value is defined: " + s))
			.undefined(() -> System.out.println("undefined!"));

		System.out.println(parsed.number);
		System.out.println(parsed.subCommand.counter);
		System.out.println(parsed.subCommand.anotherSubCommand.counter);
	}
}