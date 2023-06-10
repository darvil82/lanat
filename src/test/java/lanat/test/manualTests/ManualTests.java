package lanat.test.manualTests;

import lanat.ArgumentParser;
import lanat.CLInput;
import lanat.Command;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

public final class ManualTests {
	@Test
	public void main() {
		String input = "--help --stdin --string hello --number 67 sub-command -ccc";

		// write some stuff to stdin
		System.setIn(new ByteArrayInputStream("hello world\ngoodbye".getBytes()));

		var parsed = new ArgumentParser(CommandTemplateExample.class) {{
			this.addCommand(new Command(CommandTemplateExample.MySubCommand.class) {{
				this.addCommand(new Command(CommandTemplateExample.MySubCommand.AnotherSubCommand.class));
			}});
		}}
			.parse(CLInput.from(input))
			.into(CommandTemplateExample.class);

		parsed.string
			.defined(s -> System.out.println("Value is defined: " + s))
			.undefined(() -> System.out.println("undefined!"));

		System.out.println(parsed.number);
		System.out.println(parsed.subCommand.counter);
		System.out.println(parsed.subCommand.anotherSubCommand.counter);
		System.out.println(parsed.stdin);
	}
}