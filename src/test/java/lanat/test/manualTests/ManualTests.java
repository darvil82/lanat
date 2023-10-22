package lanat.test.manualTests;

import lanat.ArgumentParser;
import lanat.CLInput;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

public final class ManualTests {
	@Test
	public void main() {
		String input = "  ";

		// write some stuff to stdin
		System.setIn(new ByteArrayInputStream("hello world\ngoodbye".getBytes()));

		var parsed = ArgumentParser.parseFromInto(CommandTemplateExample.class, CLInput.from(input));

		parsed.string
			.ifPresentOrElse(
				s -> System.out.println("String is present: " + s),
				() -> System.out.println("String is not present")
			);

		System.out.println(parsed.number);
		System.out.println(parsed.subCommand.counter);
		System.out.println(parsed.subCommand.anotherSubCommand.counter);
		System.out.println(parsed.stdin);
		System.out.println(Arrays.toString(parsed.bytes));
	}
}