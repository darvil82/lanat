package lanat.test.exampleTests;

import lanat.Argument;
import lanat.ArgumentGroup;
import lanat.Command;
import lanat.CommandTemplate;
import lanat.argumentTypes.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Optional;

@Command.Define(names = "my-program", description = "This is a <color=cyan><format=b,i>test program<format=reset>.")
public class CommandTemplateExample extends CommandTemplate.Default {
	@Argument.Define(argType = StringArgumentType.class, description = "This is a string argument.")
	public Optional<String> string;

	@Argument.Define(names = {"number", "n"}, description = "<desc=!.type>")
	public double number = 12;

	@Argument.Define(argType = StdinArgumentType.class)
	public String stdin;

	@Argument.Define(names = "arg1", argType = StringArgumentType.class)
	public String arg1;

	@Argument.Define(description = "<desc=!.type>. Must be executable.")
	public File file;

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
		helper.<FileArgumentType, File>arg("file")
			.withArgType(new FileArgumentType(true, FileArgumentType.FileType.REGULAR_FILE) {
				@Override
				protected boolean checkFile(@NotNull File file) {
					if (!super.checkFile(file)) return false;

					if (!file.canExecute()) {
						this.addError("File is not executable.");
						return false;
					}

					return true;
				}
			});
	}

	@InitDef
	public static void afterInit(@NotNull Command cmd) {
		cmd.addGroup(new ArgumentGroup("test-group") {{
			this.setRestricted(true);
			this.addArgument(cmd.getArgument("string"));
			this.addArgument(cmd.getArgument("number"));
		}});
	}

	@Override
	public void onValuesReceived() {
		System.out.println("This is the value of number: " + this.number);
	}

	@Command.Define(names = "sub-command", description = "This is a sub-command.")
	public static class MySubCommand extends CommandTemplate.Default {

		@Argument.Define(argType = CounterArgumentType.class, description = "This is a counter", names = "c")
		public int counter = 0;

		@CommandAccessor
		public AnotherSubCommand anotherSubCommand;

		@Override
		public void onValuesReceived() {
			System.out.println("This is the value of counter: " + this.counter);
		}

		@Command.Define(names = "another-sub-command", description = "This is a sub-command.")
		public static class AnotherSubCommand extends CommandTemplate {
			@Argument.Define(argType = CounterArgumentType.class, description = "This is a counter", names = "c")
			public int counter = 0;

			@Override
			public void onValuesReceived() {
				System.out.println("This is the value of counter: " + this.counter);
			}
		}
	}
}