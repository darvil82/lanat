package argparser;

import argparser.utils.Resettable;
import argparser.utils.UtlString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArgumentGroup implements ArgumentAdder, ArgumentGroupAdder, Resettable {
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
	public Argument<?, ?>[] getArguments() {
		return arguments.toArray(Argument[]::new);
	}

	@Override
	public ArgumentGroup[] getSubGroups() {
		return subGroups.toArray(ArgumentGroup[]::new);
	}

	@Override
	public void addGroup(ArgumentGroup group) {
		if (group.parentGroup != null) {
			throw new IllegalArgumentException("Group already has a parent.");
		}

		if (this.subGroups.stream().anyMatch(g -> g.name.equals(group.name))) {
			throw new IllegalArgumentException("duplicate group identifier '" + group.name + "'");
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

	public boolean isExclusive() {
		return this.isExclusive;
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
				this.subGroups.stream().filter(g -> g != childCallee).anyMatch(g -> g.argumentUsed)
				|| this.arguments.stream().anyMatch(a -> a.getUsageCount() > 0)
			)
		) {
			return this;
		}

		if (this.parentGroup != null)
			return this.parentGroup.checkExclusivity(this);

		return null;
	}

	public boolean isEmpty() {
		return this.arguments.isEmpty() && this.subGroups.isEmpty();
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

	/**
	 * Appends the representation of this group tree to the given string builder.
	 */
	public void getRepresentation(StringBuilder sb) {
		// its empty, nothing to append
		if (this.isEmpty()) return;

		// if this group isn't exclusive, we just want to append the arguments, basically
		if (this.isExclusive)
			sb.append('(');

		List<Argument<?, ?>> arguments = this.arguments;
		for (int i = 0; i < arguments.size(); i++) {
			Argument<?, ?> arg = arguments.get(i);
			sb.append(arg.getRepresentation());
			if (i < arguments.size() - 1) {
				sb.append(' ');
				if (this.isExclusive)
					sb.append('|').append(' ');
			}
		}

		List<ArgumentGroup> groups = this.subGroups.stream().filter(g -> !g.isEmpty()).toList();

		if (!arguments.isEmpty() && !groups.isEmpty()) {
			sb.append(' ');
			if (this.isExclusive)
				sb.append("| ");
		}

		for (int i = 0; i < groups.size(); i++) {
			ArgumentGroup group = groups.get(i);
			group.getRepresentation(sb); // append the group's representation recursively
			if (i < groups.size() - 1) {
				sb.append(' ');
				if (this.isExclusive)
					sb.append('|').append(' ');
			}
		}

		if (this.isExclusive)
			sb.append(')');
	}

	@Override
	public void resetState() {
		this.argumentUsed = false;
		this.arguments.forEach(Resettable::resetState);
	}
}


interface ArgumentGroupAdder {
	/**
	 * Adds an argument group to this element.
	 */
	void addGroup(ArgumentGroup group);
	ArgumentGroup[] getSubGroups();
}