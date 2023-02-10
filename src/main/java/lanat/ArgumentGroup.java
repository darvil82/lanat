package lanat;

import lanat.utils.Resettable;
import lanat.utils.UtlString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArgumentGroup implements ArgumentAdder, ArgumentGroupAdder, Resettable, ParentCommandGetter, NamedWithDescription {
	public final @NotNull String name;
	public final @Nullable String description;
	private Command parentCommand;
	private @Nullable ArgumentGroup parentGroup;
	private final @NotNull List<@NotNull Argument<?, ?>> arguments = new ArrayList<>();
	private final @NotNull List<@NotNull ArgumentGroup> subGroups = new ArrayList<>();
	private boolean isExclusive = false;
	/**
	 * When set to true, indicates that one argument in this group has been used.
	 */
	private boolean argumentUsed = false;

	public ArgumentGroup(@NotNull String name, @Nullable String description) {
		this.name = UtlString.sanitizeName(name);
		this.description = description;
	}

	public ArgumentGroup(@NotNull String name) {
		this(name, null);
	}

	@Override
	public <T extends ArgumentType<TInner>, TInner>
	void addArgument(@NotNull Argument<T, TInner> argument) {
		this.arguments.add(argument);
		argument.setParentGroup(this);
	}

	@Override
	public @NotNull List<Argument<?, ?>> getArguments() {
		return Collections.unmodifiableList(this.arguments);
	}

	@Override
	public @NotNull List<ArgumentGroup> getSubGroups() {
		return Collections.unmodifiableList(this.subGroups);
	}

	@Override
	public void addGroup(@NotNull ArgumentGroup group) {
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
	void registerGroup(@NotNull Command parentCommand) {
		if (this.parentCommand != null) {
			throw new IllegalStateException("This group is already registered to a command.");
		}

		this.parentCommand = parentCommand;
		for (var argument : this.arguments) {
			parentCommand.addArgument(argument);
		}
		this.subGroups.forEach(g -> g.registerGroup(parentCommand));
	}

	@Override
	public Command getParentCommand() {
		return this.parentCommand;
	}

	private @Nullable ArgumentGroup checkExclusivity(@Nullable ArgumentGroup childCallee) {
		if (
			this.isExclusive && (
				this.subGroups.stream().filter(g -> g != childCallee).anyMatch(g -> g.argumentUsed)
					|| this.arguments.stream().anyMatch(a -> a.getUsageCount() > 0)
			)
		)
		{
			return this;
		}

		if (this.parentGroup != null)
			return this.parentGroup.checkExclusivity(this);

		return null;
	}

	public boolean isEmpty() {
		return this.arguments.isEmpty() && this.subGroups.isEmpty();
	}

	@Nullable ArgumentGroup checkExclusivity() {
		return this.checkExclusivity(null);
	}

	void setArgUsed() {
		this.argumentUsed = true;

		// set argUsed to true on all parents until reaching the groups root
		if (this.parentGroup != null)
			this.parentGroup.setArgUsed();
	}


	@Override
	public void resetState() {
		this.argumentUsed = false;
		this.arguments.forEach(Resettable::resetState);
	}

	@Override
	public @NotNull String getName() {
		return this.name;
	}

	@Override
	public @Nullable String getDescription() {
		return this.description;
	}
}


interface ArgumentGroupAdder {
	/**
	 * Adds an argument group to this element.
	 */
	void addGroup(@NotNull ArgumentGroup group);

	@NotNull List<@NotNull ArgumentGroup> getSubGroups();
}