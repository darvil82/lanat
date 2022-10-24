package argparser;

import java.util.ArrayList;

public class Command {
	protected final String name, description;
	protected ArrayList<Argument<?, ?>> arguments = new ArrayList<>();
	protected ArrayList<Command> subCommands = new ArrayList<>();

	public Command(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public <T extends ArgumentType<TInner>, TInner>
	void addArgument(Argument<T, TInner> argument) {
		if (this.arguments.stream().anyMatch(a -> a.equals(argument))) {
			throw new IllegalArgumentException("duplicate argument identifiers");
		}
		this.arguments.add(argument);
	}

	public void addSubCommand(Command cmd) {
		this.subCommands.add(cmd);
	}

	public String getHelp() {
		return "This is the help of the program.";
	}
}
