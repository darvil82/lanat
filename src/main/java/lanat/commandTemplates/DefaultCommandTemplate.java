package lanat.commandTemplates;

import lanat.Argument;
import lanat.ArgumentParser;
import lanat.Command;
import lanat.CommandTemplate;
import org.jetbrains.annotations.NotNull;

@Command.Define
public class DefaultCommandTemplate extends CommandTemplate {
	/*
	 * The reason we add these arguments here is so that they do not "physically" appear in the
	 * actual class that extends this one. 'help' and 'version' are just
	 * arguments that execute actions, and they not really provide any useful values.
	 */
	@InitDef
	public static void init(@NotNull Command cmd) {
		cmd.addArgument(Argument.create("help")
			.onOk(t -> System.out.println(cmd.getHelp()))
			.withDescription("Shows this message.")
			.allowsUnique()
		);

		if (cmd instanceof ArgumentParser ap) {
			cmd.addArgument(Argument.create("version")
				.onOk(t -> System.out.println("Version: " + ap.getVersion()))
				.withDescription("Shows the version of this program.")
				.allowsUnique()
			);
		}
	}
}
