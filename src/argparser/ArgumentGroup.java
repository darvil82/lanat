package argparser;

import argparser.utils.UtlString;

import java.util.ArrayList;
import java.util.List;

public class ArgumentGroup implements ArgumentAdder, ArgumentGroupAdder {
	public final String name;
	public final String description;
	private Command parentCommand;
	private ArgumentGroup parentGroup;
	private final List<Argument<?, ?>> arguments = new ArrayList<>();
	private final List<ArgumentGroup> subGroups = new ArrayList<>();
	private boolean isExclusive = false;
	/** When set to true, indicates that one argument in this group has been used. */
	private boolean argumentUsed = false;

	public ArgumentGroup(String name, String description) {
		this.name = UtlString.sanitizeName(name);
		this.description = description;
	}

	public ArgumentGroup(String name) {
		this(name, null);
	}

	@Override
	public <T extends ArgumentType<TInner>, TInner>
	void addArgument(Argument<T, TInner> argument) {
		this.arguments.add(argument);
		argument.setParentGroup(this);
	}

	@Override
	public void addGroup(ArgumentGroup group) {
		if (group.parentGroup != null) {
			throw new IllegalArgumentException("Group already has a parent.");
		}

		group.parentGroup = this;
		group.parentCommand = this.parentCommand;
		this.subGroups.add(group);
	}

	/**
	 * Sets this group to be exclusive, meaning that only one argument in it can be used.
	 */
	public void exclusive() {
		this.isExclusive = true;
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
		this.subGroups.forEach(g -> g.registerGroup(parentCommand));
	}

	private ArgumentGroup checkExclusivity(ArgumentGroup childCallee) {
		if (
			this.isExclusive && (
				this.subGroups.stream().filter(g -> g != childCallee && g.isExclusive).anyMatch(g -> g.argumentUsed)
				|| this.arguments.stream().anyMatch(a -> a.getUsageCount() > 0)
			)
		) {
			return this;
		}

		if (this.parentGroup != null)
			return this.parentGroup.checkExclusivity(this);

		return null;
	}

	ArgumentGroup checkExclusivity() {
		return this.checkExclusivity(null);
	}

	void setArgUsed() {
		this.argumentUsed = true;

		// set argUsed to true on all parents until reaching the groups root
		if (this.parentGroup != null)
			this.parentGroup.setArgUsed();
	}

	int getNestingLevel() {
		ArgumentGroup current = this;
		int level = 0;

		while (current != null) {
			level++;
			current = current.parentGroup;
		};

		return level;
	}
}


interface ArgumentGroupAdder {
	/**
	 * Adds an argument group to this element.
	 */
	void addGroup(ArgumentGroup group);
}