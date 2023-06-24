package lanat.commandTemplates;

import lanat.Argument;
import lanat.ArgumentParser;
import lanat.Command;
import lanat.CommandTemplate;
import org.jetbrains.annotations.NotNull;

/**
 * A default command template that adds the 'help' and 'version' arguments to the command.
 * <ul>
 * <li>The 'help' argument shows the help message of the command (provided by the {@link Command#getHelp()} method).</li>
 * <li>The 'version' argument shows the version of the program (provided by the {@link ArgumentParser#getVersion()} method).</li>
 * </ul>
 */
@Command.Define
public class DefaultCommandTemplate extends CommandTemplate {
	/*
	 * The reason we add these arguments here is so that they do not "physically" appear in the
	 * actual class that extends this one. 'help' and 'version' are just
	 * arguments that execute actions, and they not really provide any useful values.
	 */
	@InitDef
	public static void afterInit(@NotNull Command cmd) {
		cmd.addArgument(Argument.createOfBoolType("help")
			.onOk(t -> System.out.println(cmd.getHelp()))
			.withDescription("Shows this message.")
			.allowsUnique()
		);

		if (cmd instanceof ArgumentParser ap) {
			cmd.addArgument(Argument.createOfBoolType("version")
				.onOk(t -> System.out.println("Version: " + ap.getVersion()))
				.withDescription("Shows the version of this program.")
				.allowsUnique()
			);
		}
	}
}
