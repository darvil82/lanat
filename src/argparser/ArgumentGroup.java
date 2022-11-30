package argparser;

import java.util.ArrayList;
import java.util.List;

public class ArgumentGroup implements IArgumentAdder {
	public final String name;
	public final String description;
	private Command parentCommand;
	private ArgumentGroup parentGroup;
	private final List<Argument<?, ?>> arguments = new ArrayList<>();

	public ArgumentGroup(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public ArgumentGroup(String name) {
		this(name, null);
	}

	@Override
	public <T extends ArgumentType<TInner>, TInner>
	void addArgument(Argument<T, TInner> argument) {
		this.arguments.add(argument);
	}

	public void addGroup(ArgumentGroup group) {
		if (group.parentGroup != null) {
			throw new IllegalArgumentException("Group already has a parent.");
		}

		group.parentGroup = this;
		group.parentCommand = this.parentCommand;
	}

	/**
	 * Sets this group's parent command, and also passes all its arguments to the command.
	 */
	void registerGroup(Command parentCommand) {
		if (this.parentCommand != null) {
			throw new IllegalStateException("This group is already registered to a command.");
		}

		this.parentCommand = parentCommand;
		for (var argument : this.arguments) {
			parentCommand.addArgument(argument);
		}
		this.arguments.clear();
	}
}
