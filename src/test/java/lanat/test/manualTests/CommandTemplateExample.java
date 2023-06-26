package lanat.test.manualTests;

import lanat.Argument;
import lanat.ArgumentGroup;
import lanat.Command;
import lanat.CommandTemplate;
import lanat.argumentTypes.CounterArgument;
import lanat.argumentTypes.IntArgument;
import lanat.argumentTypes.StdinArgument;
import lanat.argumentTypes.StringArgument;
import lanat.commandTemplates.DefaultCommandTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Command.Define(names = "my-program", description = "This is a test program.")
public class CommandTemplateExample extends DefaultCommandTemplate {
	public CommandTemplateExample() {}

	@Argument.Define(argType = StringArgument.class, description = "This is a string argument.")
	public Optional<String> string;

	@Argument.Define(argType = IntArgument.class, description = "<desc=!.type>")
	public int number = 12;

	@Argument.Define(argType = StdinArgument.class)
	public String stdin;

	@Argument.Define(names = "arg1", argType = StringArgument.class)
	public String arg1;


	@Argument.Define(names = "arg1a", argType = StringArgument.class)
	public String arg1copy;


	@CommandAccessor
	public MySubCommand subCommand;

	@InitDef
	public static void afterInit(@NotNull Command cmd) {
		cmd.addGroup(new ArgumentGroup("test-group") {{
			this.addArgument(cmd.getArgument("string"));
			this.addArgument(cmd.getArgument("number"));
		}});
	}


	@Command.Define(names = "sub-command", description = "This is a sub-command.")
	public static class MySubCommand extends CommandTemplate {
		public MySubCommand() {}

		@Argument.Define(argType = CounterArgument.class, description = "This is a counter", names = "c")
		public int counter = 0;

		@CommandAccessor
		public AnotherSubCommand anotherSubCommand;

		@Command.Define(names = "another-sub-command", description = "This is a sub-command.")
		public static class AnotherSubCommand extends CommandTemplate {
			public AnotherSubCommand() {}

			@Argument.Define(argType = CounterArgument.class, description = "This is a counter", names = "c")
			public int counter = 0;
		}
	}
}