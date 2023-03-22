package lanat.test;

import lanat.Argument;
import lanat.ArgumentGroup;
import lanat.Command;
import lanat.CommandTemplate;
import lanat.argumentTypes.CounterArgument;
import lanat.argumentTypes.IntArgument;
import lanat.argumentTypes.StringArgument;
import lanat.commandTemplates.DefaultCommandTemplate;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public final class ManualTests {
	@Test
	public void main() {
//		HelpFormatter.lineWrapMax = 110;
//		HelpFormatter.debugLayout = true;
//		TextFormatter.debug = true;

		enum TestEnum {
			ONE, TWO, THREE
		}

		var parser = new TestingParser(MyProgram.class) {{
			final var that = this;

			this.addGroup(new ArgumentGroup("test-group") {{
				this.addArgument(that.getArgument("string"));
				this.addArgument(that.getArgument("number"));
			}});

			this.addCommand(new Command(MyProgram.MySubCommand.class));
		}};

		var parsed = parser.parse("--string hello --number 67 sub-command -ccc")
			.printErrors()
			.into(MyProgram.class);

		System.out.println(parsed.string);
		System.out.println(parsed.number);
		System.out.println(parsed.subCommand.counter);
	}
}

@Command.Define(names = "my-program", description = "This is a test program.")
class MyProgram extends DefaultCommandTemplate {
	public MyProgram() {}

	@Argument.Define(type = StringArgument.class, description = "This is a string argument.")
	public String string;

	@Argument.Define(type = IntArgument.class, description = "<desc=!.type>")
	public int number = 12;

	@CommandAccessor
	public MySubCommand subCommand;


	@Command.Define(names = "sub-command", description = "This is a sub-command.")
	public static class MySubCommand extends CommandTemplate {
		public MySubCommand() {}

		@Argument.Define(type = CounterArgument.class, description = "This is a counter", names = "c")
		public int counter = 0;
	}
}