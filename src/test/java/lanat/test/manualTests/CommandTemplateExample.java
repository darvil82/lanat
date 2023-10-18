package lanat.test.manualTests;

import lanat.Argument;
import lanat.ArgumentGroup;
import lanat.Command;
import lanat.CommandTemplate;
import lanat.argumentTypes.CounterArgumentType;
import lanat.argumentTypes.NumberRangeArgumentType;
import lanat.argumentTypes.StdinArgumentType;
import lanat.argumentTypes.StringArgumentType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Command.Define(names = "my-program", description = "This is a <color=cyan><format=b,i>test program<format=reset>.")
public class CommandTemplateExample extends CommandTemplate.Default {
	public CommandTemplateExample() {}

	@Argument.Define(argType = StringArgumentType.class, description = "This is a string argument.")
	public Optional<String> string;

	@Argument.Define(description = "<desc=!.type>")
	public double number = 12;

	@Argument.Define(argType = StdinArgumentType.class)
	public String stdin;

	@Argument.Define(names = "arg1", argType = StringArgumentType.class)
	public String arg1;


	@Argument.Define(names = "arg1a", argType = StringArgumentType.class)
	public String arg1copy;

	@Argument.Define
	public Byte[] bytes;

	@CommandAccessor
	public MySubCommand subCommand;

	@InitDef
	public static void beforeInit(@NotNull CommandBuildHelper helper) {
		helper.<NumberRangeArgumentType<Double>, Double>arg("number")
			.withArgType(new NumberRangeArgumentType<>(5.5, 15.89));
	}

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

		@Argument.Define(argType = CounterArgumentType.class, description = "This is a counter", names = "c")
		public int counter = 0;

		@CommandAccessor
		public AnotherSubCommand anotherSubCommand;

		@Command.Define(names = "another-sub-command", description = "This is a sub-command.")
		public static class AnotherSubCommand extends CommandTemplate {
			public AnotherSubCommand() {}

			@Argument.Define(argType = CounterArgumentType.class, description = "This is a counter", names = "c")
			public int counter = 0;
		}
	}
}