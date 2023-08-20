package lanat.test.manualTests;

import lanat.utils.displayFormatter.FormatOption;
import org.junit.jupiter.api.Test;

public final class ManualTests {
	@Test
	public void main() {
		System.out.println(FormatOption.BOLD + "Hello World!" + FormatOption.BOLD.reset() + " This is bold text.");
//		String input = "--help --stdin --string hello --number 67 sub-command -ccc";
//
//		// write some stuff to stdin
//		System.setIn(new ByteArrayInputStream("hello world\ngoodbye".getBytes()));
//
//		var parsed = new ArgumentParser(CommandTemplateExample.class) {{
//			this.addCommand(new Command(CommandTemplateExample.MySubCommand.class) {{
//				this.addCommand(new Command(CommandTemplateExample.MySubCommand.AnotherSubCommand.class));
//			}});
//		}}
//			.parse(CLInput.from(input))
//			.into(CommandTemplateExample.class);
//
//		parsed.string
//			.ifPresentOrElse(
//				s -> System.out.println("String is present: " + s),
//				() -> System.out.println("String is not present")
//			);
//
//		System.out.println(parsed.number);
//		System.out.println(parsed.subCommand.counter);
//		System.out.println(parsed.subCommand.anotherSubCommand.counter);
//		System.out.println(parsed.stdin);
	}
}