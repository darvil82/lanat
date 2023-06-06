package lanat.test.manualTests;

import lanat.*;
import lanat.argumentTypes.CounterArgument;
import lanat.argumentTypes.IntArgument;
import lanat.argumentTypes.StringArgument;
import lanat.commandTemplates.DefaultCommandTemplate;
import org.jetbrains.annotations.NotNull;

@Command.Define(names = "my-program", description = "This is a test program.")
public class CommandTemplateExample extends DefaultCommandTemplate {
	public CommandTemplateExample() {}

	@Argument.Define(type = StringArgument.class, description = "This is a string argument.")
	public ParsedArgumentValue<String> string;

	@Argument.Define(type = IntArgument.class, description = "<desc=!.type>")
	public int number = 12;

	@CommandAccessor
	public MySubCommand subCommand;

	@InitDef
	public static void init(@NotNull Command cmd) {
		cmd.addGroup(new ArgumentGroup("test-group") {{
			this.addArgument(cmd.getArgument("string"));
			this.addArgument(cmd.getArgument("number"));
		}});
	}


	@Command.Define(names = "sub-command", description = "This is a sub-command.")
	public static class MySubCommand extends CommandTemplate {
		public MySubCommand() {}

		@Argument.Define(type = CounterArgument.class, description = "This is a counter", names = "c")
		public int counter = 0;

		@CommandAccessor
		public AnotherSubCommand anotherSubCommand;

		@Command.Define(names = "another-sub-command", description = "This is a sub-command.")
		public static class AnotherSubCommand extends CommandTemplate {
			public AnotherSubCommand() {}

			@Argument.Define(type = CounterArgument.class, description = "This is a counter", names = "c")
			public int counter = 0;
		}
	}
}