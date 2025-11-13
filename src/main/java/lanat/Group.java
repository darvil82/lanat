package lanat;

import lanat.exceptions.GroupAlreadyExistsException;
import lanat.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <h2>Argument Group</h2>
 * <p>
 * Represents a group of arguments. This is used to group arguments together, and to set a restriction between them.
 * When a group is restricted, it means that only one argument in it can be used at a time.
 * <p>
 * Groups can also be used to simply indicate arguments that are related to each other, and to set a description
 * to this relation. This is useful for the help message representation.
 * <p>
 * Groups can be nested, meaning that a group can contain other groups. This is useful for setting restrictions between
 * arguments that are in different groups. For example, given the following group tree:
 * <pre>
 *            +-----------------------+
 *            |  Group 1 (restricted) |
 *            |-----------------------|
 *            | Argument 1            |
 *            +-----------------------+
 *                       |
 *          +---------------------------+
 *          |                           |
 *  +---------------+      +-------------------------+
 *  |    Group 2    |      |   Group 3 (restricted)  |
 *  |---------------|      |-------------------------|
 *  | Argument 2.1  |      | Argument 3.1            |
 *  | Argument 2.2  |      | Argument 3.2            |
 *  +---------------+      +-------------------------+
 * </pre>
 * <ul>
 * <li>
 * If {@code Argument 1} is used, then none of the arguments in the child groups can be used, because {@code Group 1}
 * is restricted.
 * </li>
 * <li>
 * If {@code Argument 3.1} is used, then none of the arguments in the rest of the tree can be used, because
 * both {@code Group 3} and its parent {@code Group 1} are restricted.
 * </li>
 * <li>
 * If {@code Argument 2.1} is used, {@code Argument 2.2} can still be used, because {@code Group 2} is not restricted.
 * No other arguments in the tree can be used though.
 * </li>
 * </ul>
 */
public class Group
	implements ArgumentAdder,
		GroupAdder,
		CommandUser,
		GroupUser,
		Resettable,
		NamedWithDescription,
		ParentElementGetter<Group>
{
	private final @NotNull String name;
	private @Nullable String description;

	/** The parent command of this group. This is set when the group is added to a command. */
	private Command parentCommand;

	/** The parent group of this group. This is set when the group is added to another group. */
	private @Nullable Group parentGroup;

	/**
	 * The reason we keep references to the Arguments instead of just calling {@link Command#addArgument(Argument)} for
	 * each one added to this group is because at parsing, we might need to know which arguments were used in this
	 * group.
	 * <br><br>
	 * Sure, we could just use {@link Command#getArguments()}, but that would mean that we would have
	 * to iterate through all the arguments in there for filtering ours, which is probably worse.
	 */
	private final @NotNull List<@NotNull Argument<?, ?>> arguments = new ArrayList<>();

	/**
	 * We need to later set the parent command of all group children after initialization, so we keep a reference to
	 * them.
	 */
	private final @NotNull List<@NotNull Group> subGroups = new ArrayList<>(0);
	private boolean isRestricted = false;

	/**
	 * When set to {@code true}, indicates that one argument in this group has been used. This is used when later
	 * checking for restrictions in the groups tree at {@link Group#getRestrictionViolator(Group)}
	 */
	private boolean argumentUsed = false;


	/**
	 * Creates a new Argument Group with the given name and description.
	 * The name and descriptions are basically only used for the help message.
	 * @param name The name of the group. Must be a unique name among all groups in the same command.
	 * @param description The description of the group.
	 */
	public Group(@NotNull String name, @Nullable String description) {
		this.name = UtlMisc.requireValidName(name);
		this.description = description;
	}

	/**
	 * Creates a new Argument Group with the given name and no description.
	 * @param name The name of the group. Must be a unique name among all groups in the same command.
	 */
	public Group(@NotNull String name) {
		this(name, null);
	}


	@Override
	public <Type extends ArgumentType<TInner>, TInner>
	void addArgument(@NotNull Argument<Type, TInner> argument) {
		this.arguments.add(argument);
		this.checkUniqueArguments();
	}

	@Override
	public @NotNull List<Argument<?, ?>> getArguments() {
		return Collections.unmodifiableList(this.arguments);
	}

	@Override
	public @NotNull List<Group> getGroups() {
		return Collections.unmodifiableList(this.subGroups);
	}

	@Override
	public void addGroup(@NotNull Group group) {
		if (group == this) {
			throw new IllegalArgumentException("A group cannot be added to itself");
		}

		group.registerToGroup(this);
		this.subGroups.add(group);
		this.checkUniqueGroups();
	}

	@Override
	public void registerToGroup(@NotNull Group parentGroup) {
		if (this.parentGroup != null) {
			throw new GroupAlreadyExistsException(this, this.parentGroup);
		}

		this.parentGroup = parentGroup;
	}

	/**
	 * Sets this group's parent command, and also passes all its arguments to the command.
	 */
	@Override
	public void registerToCommand(@NotNull Command parentCommand) {
		if (this.parentCommand != null) {
			throw new GroupAlreadyExistsException(this, this.parentCommand);
		}

		this.parentCommand = parentCommand;
	}

	/**
	 * Links all the groups in this tree to their parent command.
	 * <p>
	 * This makes sure subgroups are properly linked if the user added them by using anonymous classes.
	 * When executing the instance block of a group subclass, the parent command is not set yet.
	 */
	void linkHierarchyToCommand(@NotNull Command parentCommand) {
		this.parentCommand = parentCommand;

		// now register all arguments properly
		this.arguments.stream()
			.filter(arg -> arg.getParentGroup() == null) // only register arguments that have no parent group yet
			.forEach(arg -> {
				// make sure to attach it to the parent command if it has no parent command set yet
				if (arg.getParentCommand() == null)
					parentCommand.addArgument(arg);

				arg.registerToGroup(this);
			});

		this.subGroups.forEach(group -> group.linkHierarchyToCommand(parentCommand));
	}

	@Override
	public Command getParentCommand() {
		return this.parentCommand;
	}

	@Override
	public @Nullable Group getParentGroup() {
		return this.parentGroup;
	}

	/**
	 * Sets this group to be restricted, meaning that only one argument in it can be used at a time.
	 * @see Group#isRestricted()
	 */
	public void setRestricted(boolean isRestricted) {
		this.isRestricted = isRestricted;
	}

	/**
	 * Returns {@code true} if this group is restricted.
	 * @return {@code true} if this group is restricted.
	 * @see Group#setRestricted(boolean)
	 */
	public boolean isRestricted() {
		return this.isRestricted;
	}

	/**
	 * Checks if there is any violation of restrictions in this group's tree, from this group to the root. This is done
	 * by checking if this or any of the group's siblings have been used (except for the childCallee, which is the group
	 * that called this method). If none of them have been used, the parent group is checked, and so on.
	 *
	 * @param childCallee The group that called this method. This is used to avoid checking the group that called this
	 * 	method, because it is the one that is being checked for restriction. This can be {@code null} if this is
	 * 	the first call to this method.
	 * @return The group that caused the violation, or {@code null} if there is no violation.
	 */
	@Nullable
	Group getRestrictionViolator(@Nullable Group childCallee) {
		if (
			this.isRestricted && (
				this.argumentUsed || this.subGroups.stream().filter(g -> g != childCallee).anyMatch(g -> g.argumentUsed)
			)
		)
			return this;

		if (this.parentGroup != null)
			return this.parentGroup.getRestrictionViolator(this);

		return null;
	}

	/**
	 * Returns {@code true} if this group has no arguments and no subgroups.
	 * @return {@code true} if this group has no arguments and no subgroups.
	 */
	public boolean isEmpty() {
		return this.arguments.isEmpty() && this.subGroups.isEmpty();
	}


	/**
	 * Marks that an argument in this group has been used. This is used to later check for restrictions.
	 * This also marks the parent group as used, and so on until reaching the root of the groups tree, thus marking the
	 * path of the used argument.
	 */
	void setArgUsed() {
		this.argumentUsed = true;

		// set argUsed to {@code true} on all parents until reaching the groups root
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

	/**
	 * Sets the description of this group. The description is used to be displayed in the help message.
	 * @param description The description of this group.
	 */
	public void setDescription(@NotNull String description) {
		this.description = description;
	}

	@Override
	public @Nullable String getDescription() {
		return this.description;
	}

	@Override
	public Group getParent() {
		return this.parentGroup;
	}

	@Override
	public boolean equals(@NotNull Object obj) {
		if (obj == this) return true;
		if (obj instanceof Group group)
			return this.parentCommand == group.parentCommand && this.name.equals(group.name);
		return false;
	}

	@Override
	public String toString() {
		return "Group{name='%s', description='%s', arguments=%s, sub-groups=%s, restricted=%s}"
			.formatted(this.name, this.description, this.arguments, this.subGroups, this.isRestricted);
	}
}