package lanat;

import lanat.exceptions.ArgumentGroupAlreadyExistsException;
import lanat.utils.Resettable;
import lanat.utils.UtlString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArgumentGroup
	implements ArgumentAdder,
		ArgumentGroupAdder,
		Resettable,
		CommandUser,
		NamedWithDescription,
		ParentElementGetter<ArgumentGroup>
{
	public final @NotNull String name;
	public final @Nullable String description;
	private Command parentCommand;
	private @Nullable ArgumentGroup parentGroup;

	/**
	 * The reason we keep references to the Arguments instead of just calling {@link Command#addArgument(Argument)} for
	 * each one added to this group is because at parsing, we might need to know which arguments were used in this
	 * group.
	 * <br><br>
	 * Sure, we could just use {@link Command#arguments}, but that would mean that we would have to iterate through all
	 * the arguments in there for filtering ours, which is probably worse.
	 */
	private final @NotNull List<@NotNull Argument<?, ?>> arguments = new ArrayList<>();

	/**
	 * We need to later set the parent command of all group children after initialization, so we keep a reference to
	 * them.
	 */
	private final @NotNull List<@NotNull ArgumentGroup> subGroups = new ArrayList<>();
	private boolean isExclusive = false;

	/**
	 * When set to <code>true</code>, indicates that one argument in this group has been used. This is used when later checking for
	 * exclusivity in the groups tree at {@link ArgumentGroup#checkExclusivity(ArgumentGroup)}
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
			throw new ArgumentGroupAlreadyExistsException(group, group.parentGroup);
		}

		if (this.subGroups.stream().anyMatch(g -> g.name.equals(group.name))) {
			throw new ArgumentGroupAlreadyExistsException(group, group);
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
			throw new ArgumentGroupAlreadyExistsException(this, this.parentCommand);
		}

		this.parentCommand = parentCommand;
		for (var argument : this.arguments) {
			parentCommand.addArgument(argument);
		}
		this.subGroups.forEach(g -> g.registerGroup(parentCommand));
	}

	@Override
	public @NotNull Command getParentCommand() {
		return this.parentCommand;
	}

	/**
	 * Checks if there is any violation of exclusivity in this group's tree, from this group to the root.
	 * This is done by checking if this or any of the group's siblings have been used (except for the childCallee, which is
	 * the group that called this method). If none of them have been used, the parent group is checked, and so on.
	 * @param childCallee The group that called this method. This is used to avoid checking the group that called this
	 * 	method, because it is the one that is being checked for exclusivity. This can be <code>null</code> if this is the
	 * 	first call to this method.
	 * @return The group that caused the violation, or <code>null</code> if there is no violation.
	 */
	@Nullable ArgumentGroup checkExclusivity(@Nullable ArgumentGroup childCallee) {
		if (
			this.isExclusive && (
				this.argumentUsed || this.subGroups.stream().filter(g -> g != childCallee).anyMatch(g -> g.argumentUsed)
			)
		)
			return this;

		if (this.parentGroup != null)
			return this.parentGroup.checkExclusivity(this);

		return null;
	}

	public boolean isEmpty() {
		return this.arguments.isEmpty() && this.subGroups.isEmpty();
	}


	void setArgUsed() {
		this.argumentUsed = true;

		// set argUsed to <code>true</code> on all parents until reaching the groups root
		if (this.parentGroup != null)
			this.parentGroup.setArgUsed();
	}


	@Override
	public void resetState() {
		// we don't need to reset the state of the arguments, because they are reset when the command is reset
		this.argumentUsed = false;
	}

	@Override
	public @NotNull String getName() {
		return this.name;
	}

	@Override
	public @Nullable String getDescription() {
		return this.description;
	}

	@Override
	public ArgumentGroup getParent() {
		return this.parentGroup;
	}
}


