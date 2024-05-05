package lanat;

import lanat.argumentTypes.ActionArgumentType;
import lanat.argumentTypes.DummyArgumentType;
import lanat.exceptions.ArgumentAlreadyExistsException;
import lanat.parsing.errors.handlers.ArgumentTypeError;
import lanat.parsing.errors.handlers.ParseErrors;
import lanat.utils.*;
import lanat.utils.errors.ErrorCallbacks;
import lanat.utils.errors.ErrorContainer;
import lanat.utils.errors.ErrorLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.color.Color;
import utils.ModifyRecord;
import utils.MultiComparator;
import utils.UtlString;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;


/**
 * <h2>Argument</h2>
 * <p>
 * An Argument specifies a value that the user can introduce to the command. This value will be parsed by the specified
 * {@link ArgumentType} each time the Argument is used. Once finished parsing, the value may be retrieved by using
 * {@link ParseResult#get(String...)} on the {@link ParseResult} object returned by
 * {@link ArgumentParser#parse(CLInput)}.
 *
 * <p>
 * An Argument can be created using the factory methods available, like {@link Argument#createOfActionType(String...)}.
 * </p>
 * <br><br>
 * <h3>Example:</h3>
 *
 * <p>
 * An Argument with the names "name" and 'n' that will parse an integer value. In order to create an Argument, you need
 * to call any of the static factory methods available, like {@link Argument#createOfActionType(String...)}. These methods
 * will return an {@link ArgumentBuilder} object, which can be used to specify the Argument's properties easily.
 * </p>
 * <pre>
 * {@code
 *     Argument.create(new IntegerArgumentType(), "name", "n");
 *     Argument.create(new IntegerArgumentType()).names("name", "n");
 * }
 * </pre>
 *
 *
 * <h3>Argument usage</h3>
 * The argument can be used in the following ways:
 * <ul>
 * <li>
 * Specifying the whole name with the prefix: {@code "--name 32"} or {@code "-name 32"}.
 * <p>
 * An equals sign can be used instead of a space: {@code "--name=32"} or {@code "-name=32"}.
 * </p>
 * </li>
 * <li>Since the argument in the example above has a single character name, this is also possible: {@code "-n32"}.</li>
 * </ul>
 *
 * @param <Type> the {@link ArgumentType} subclass that will parse the value passed to the argument
 * @param <TInner> the actual type of the value passed to the argument
 * @see Command#addArgument(Argument)
 * @see Group
 * @see ArgumentParser
 */
public class Argument<Type extends ArgumentType<TInner>, TInner>
	implements ErrorContainer<ArgumentTypeError>,
		ErrorCallbacks<TInner, Argument<Type, TInner>>,
		Resettable,
		CommandUser,
		GroupUser,
		MultipleNamesAndDescription
{
	/**
	 * The type of this argument. This is the subParser that will be used to parse the value/s this argument should
	 * receive.
	 */
	public final @NotNull Type type;
	private @NotNull Argument.Prefix prefix = Prefix.DEFAULT;
	private @NotNull List<@NotNull String> names = new ArrayList<>(1);
	private @Nullable String description;
	private boolean required = false,
		positional = false,
		unique = false,
		visible = true;

	private @Nullable TInner defaultValue;

	/** The Command that this Argument belongs to. This should never be {@code null} after initialization. */
	private Command parentCommand;

	/**
	 * The Group that this Argument belongs to. If this Argument does not belong to any group, this may be
	 * {@code null}.
	 */
	private @Nullable Group parentGroup;

	// callbacks for error handling
	private @Nullable Consumer<@NotNull Argument<Type, TInner>> onErrorCallback;
	private @Nullable Consumer<@NotNull TInner> onCorrectCallback;

	/**
	 * The color that this Argument will have in places where it is displayed, such as the help message. By default, the
	 * color will be picked from the {@link Command#colorsPool} of the parent command at
	 * {@link Argument#registerToCommand(Command)}.
	 */
	private final @NotNull ModifyRecord<Color> representationColor = ModifyRecord.empty();


	/**
	 * Creates an argument with the specified type and names.
	 * @param type the type of the argument. This is the subParser that will be used to parse the value/s this
	 * 	argument should receive.
	 * @param names the names of the argument. See {@link Argument#setNames(List)} for more information.
	 */
	protected Argument(@NotNull Type type, @NotNull String... names) {
		this.type = type;
		this.setNames(List.of(names));
	}

	/**
	 * Creates an argument builder with no type or names.
	 * @param <Type> the {@link ArgumentType} subclass that will parse the value passed to the argument
	 * @param <TInner> the actual type of the value passed to the argument
	 */
	public static <Type extends ArgumentType<TInner>, TInner>
	ArgumentBuilder<Type, TInner> create() {
		return new ArgumentBuilder<>();
	}

	/**
	 * Creates an argument builder with the specified type and names.
	 * @param type the type of the argument. This is the subParser that will be used to parse the value/s this
	 * 	argument should receive.
	 * @param names the names of the argument. See {@link Argument#setNames(List)} for more information.
	 */
	public static <Type extends ArgumentType<TInner>, TInner>
	ArgumentBuilder<Type, TInner> create(@NotNull Type type, @NotNull String... names) {
		return Argument.<Type, TInner>create().names(names).type(type);
	}

	/**
	 * Creates an argument builder with the specified type and names.
	 * @param type the type of the argument. This is the subParser that will be used to parse the value/s this
	 * 	argument should receive.
	 * @param names the names of the argument. See {@link Argument#setNames(List)} for more information.
	 */
	public static <Type extends ArgumentType<TInner>, TInner>
	ArgumentBuilder<Type, TInner> create(@NotNull Builder<Type> type, @NotNull String... names) {
		return Argument.create(type.build(), names);
	}

	/**
	 * Creates an argument builder with an {@link ActionArgumentType} type.
	 * @param names the names of the argument. See {@link Argument#setNames(List)} for more information.
	 */
	public static ArgumentBuilder<ActionArgumentType, Boolean> createOfActionType(@NotNull String... names) {
		return Argument.create(new ActionArgumentType()).names(names);
	}

	/**
	 * Marks the argument as required. This means that this argument should <b>always</b> be used by the user.
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

	/**
	 * Returns {@code true} if this argument is required.
	 * @return {@code true} if this argument is required.
	 * @see #setRequired(boolean)
	 */
	public boolean isRequired() {
		return this.required;
	}

	/**
	 * Marks the argument as positional. This means that the value of this argument may be specified directly without
	 * indicating a name of this argument. The positional place where it should be placed is defined by the order of
	 * creation of the argument definitions.
	 * <ul>
	 *    <li>Note that an argument marked as positional can still be used by specifying a name.
	 * </ul>
	 */
	public void setPositional(boolean positional) {
		if (positional && this.type.getValueCountBounds().end() == 0) {
			throw new IllegalArgumentException("An argument that does not accept values cannot be positional");
		}
		this.positional = positional;
	}

	/**
	 * Returns {@code true} if this argument is positional.
	 * @return {@code true} if this argument is positional.
	 * @see #setPositional(boolean)
	 */
	public boolean isPositional() {
		return this.positional;
	}

	/**
	 * Specify the prefix of this argument. By default, this is {@link Prefix#DEFAULT}. If this argument is used in an
	 * argument name list (-abc), the prefix that will be valid is any against all the arguments specified in that name
	 * list.
	 * <p>
	 * Note that, for ease of use, the prefixes defined in {@link Prefix#COMMON_PREFIXES} are also valid.
	 *
	 * @param prefix the prefix that should be used for this argument.
	 * @see Prefix
	 */
	public void setPrefix(@NotNull Argument.Prefix prefix) {
		this.prefix = prefix;
	}

	/**
	 * Returns the prefix of this argument.
	 *
	 * @return the prefix of this argument.
	 */
	public @NotNull Argument.Prefix getPrefix() {
		return this.prefix;
	}

	/**
	 * Specifies that this argument has priority over other arguments, even if they are required. This means that if
	 * an argument in a command is set as required, but one argument with {@link #unique} was used, then the
	 * unused required argument will not throw an error.
	 * <p>
	 * Note that if a unique argument is used, no other argument will be allowed to be used.
	 */
	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	/**
	 * Returns {@code true} if this argument has priority over other arguments, even if they are required.
	 * @return {@code true} if this argument has priority over other arguments, even if they are required.
	 * @see #setUnique(boolean)
	 */
	public boolean isUnique() {
		return this.unique;
	}

	/**
	 * The value that should be used if the user does not specify a value for this argument. If the argument does not
	 * accept values, this value will be ignored.
	 *
	 * @param value the value that should be used if the user does not specify a value for this argument. If the value
	 * 	is {@code null}, then no default value will be used.
	 */
	public void setDefaultValue(@Nullable TInner value) {
		this.defaultValue = value;
	}

	/**
	 * Returns the default value of this argument.
	 * @return the default value of this argument.
	 * @see #setDefaultValue(Object)
	 */
	public @Nullable TInner getDefaultValue() {
		return this.defaultValue;
	}

	/**
	 * Specifies whether this argument should appear in the help message or not. By default, this is {@code true}.
	 * @param visible {@code true} if this argument should appear in the help message, {@code false} otherwise.
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Returns {@code true} if this argument is visible.
	 * @return {@code true} if this argument is visible.
	 * @see #setVisible(boolean)
	 */
	public boolean isVisible() {
		return this.visible;
	}

	/**
	 * {@inheritDoc}
	 * This is useful if you want the same argument to be used with multiple different
	 * names.
	 * <br><br>
	 * <p>
	 * Single character names can be used in argument name lists (e.g. {@code -abc}), each alphabetic character
	 * being an argument name, that is, {@code -a -b -c}.
	 * </p>
	 *
	 * @param names the names that should be added to this argument.
	 */
	@Override
	public void setNames(@NotNull List<@NotNull String> names) {
		if (names.isEmpty())
			throw new IllegalArgumentException("at least one name must be specified");

		for (var name : names)
			UtlString.requireValidName(name);

		UtlMisc.requireUniqueElements(
			names, n -> new IllegalArgumentException("Name '" + n + "' is already used by this argument"
		));

		this.names = Collections.unmodifiableList(names);

		// now let the parent command and group know that this argument has been modified. This is necessary to check
		// for duplicate names

		if (this.parentCommand != null)
			this.parentCommand.checkUniqueArguments();

		if (this.parentGroup != null)
			this.parentGroup.checkUniqueArguments();
	}

	@Override
	public @NotNull List<@NotNull String> getNames() {
		return Collections.unmodifiableList(this.names);
	}

	/**
	 * Sets the description of this argument. This description will be shown in the help message.
	 *
	 * @param description the description of this argument.
	 */
	@Override
	public void setDescription(@Nullable String description) {
		this.description = description;
	}

	@Override
	public @Nullable String getDescription() {
		return this.description;
	}

	/**
	 * Sets the parent command of this argument. This is called when adding the Argument to a command at
	 * {@link Command#addArgument(Argument)}
	 */
	@Override
	public void registerToCommand(@NotNull Command parentCommand) {
		if (this.parentCommand != null)
			throw new ArgumentAlreadyExistsException(this, this.parentCommand);

		this.parentCommand = parentCommand;
		this.representationColor.setIfNotModified(parentCommand.colorsPool.next());
	}

	@Override
	public Command getParentCommand() {
		return this.parentCommand;
	}

	/**
	 * Sets the parent group of this argument. This is called when adding the Argument to a group at
	 * {@link Group#addArgument(Argument)}
	 * This will also call {@link Argument#registerToCommand(Command)} if the parent command has not been set yet.
	 */
	@Override
	public void registerToGroup(@NotNull Group parentGroup) {
		if (this.parentGroup != null)
			throw new ArgumentAlreadyExistsException(this, this.parentGroup);

		this.parentGroup = parentGroup;
	}

	/**
	 * Returns the {@link Group} that contains this argument, or {@code null} if it does not have one.
	 *
	 * @return the parent group of this argument, or {@code null} if it does not have one.
	 */
	@Override
	public @Nullable Group getParentGroup() {
		return this.parentGroup;
	}

	/**
	 * The number of times this argument has been used in a command during parsing.
	 *
	 * @return the number of times this argument has been used in a command.
	 */
	public int getUsageCount() {
		return this.type.usageCount;
	}

	/**
	 * The color that will be used to represent this argument in the help message.
	 *
	 * @return the color that will be used to represent this argument in the help message.
	 */
	public @NotNull Color getRepresentationColor() {
		return this.representationColor.get();
	}

	/**
	 * Sets the color that will be used to represent this argument in the help message.
	 *
	 * @param color the color that will be used to represent this argument in the help message.
	 */
	public void setRepresentationColor(@NotNull Color color) {
		this.representationColor.set(color);
	}

	/**
	 * This method is called when the command is finished parsing. <strong>This method should only ever be called once (per
	 * parse).</strong>
	 *
	 * @return the final value parsed by the argument type, or the default value if the argument was not used.
	 */
	public @Nullable TInner finishParsing() {
		final TInner finalValue = this.type.getFinalValue();
		final TInner defaultValue = this.defaultValue == null
			? this.type.getInitialValue()
			: this.defaultValue;

		/* no, | is not a typo. We don't want the OR operator to short-circuit, we want all of them to be evaluated
		 * because the methods have side effects (they add errors to the parser) */
		TInner returnValue = (finalValue == null | !this.finishParsing$checkGroupRestrictions() | !this.finishParsing$checkUsageCount())
			? defaultValue
			: finalValue;

		if (this.parentGroup != null && this.getUsageCount() >= 1)
			this.parentGroup.setArgUsed();

		// if the argument type has a value defined (even if it wasn't used), use that. Otherwise, use the default value
		return returnValue;
	}

	/**
	 * Checks if the argument was used the correct amount of times.
	 *
	 * @return {@code true} if the argument was used the correct amount of times.
	 */
	private boolean finishParsing$checkUsageCount() {
		final var usageCount = this.getUsageCount();
		final var uniqueArgReceivedValue = this.parentCommand.getRoot().uniqueArgumentWasUsed(this);

		if (usageCount == 0) {
			// is required so throw error
			// if its required but some unique argument was used, then we don't need to throw an error
			if (this.required && !uniqueArgReceivedValue) {
				this.parentCommand.getParser().addError(new ParseErrors.RequiredArgumentNotUsedError(this));
			}

			// here if the argument is optional and was not used, so we can just return
			return false;
		}

		var lastTokensIndexAndOffset = this.type.getLastParseState().getIndexAndOffset();
		var usageCountIsInvalid = !this.type.getUsageCountBounds().containsInclusive(usageCount);

		// some unique argument was used, so throw an error
		if (uniqueArgReceivedValue)
			this.parentCommand.getParser()
				.addError(new ParseErrors.UniqueArgumentUsedError(lastTokensIndexAndOffset, this));

		// make sure that the argument was used the minimum number of times specified
		if (usageCountIsInvalid)
			this.parentCommand.getParser()
				.addError(new ParseErrors.IncorrectUsagesCountError(lastTokensIndexAndOffset, this));

		return !usageCountIsInvalid && !uniqueArgReceivedValue;
	}

	/**
	 * Checks if the argument is part of a restricted group, and if so, checks if there is any violation of restrictions
	 * in the group hierarchy.
	 * @return {@code true} if there is no violation of restrictions in the group hierarchy.
	 */
	private boolean finishParsing$checkGroupRestrictions() {
		// check if the parent group of this argument is restricted, and if so, check if any other argument in it has been used
		if (this.parentGroup == null || this.getUsageCount() == 0) return true;

		Group restrictionViolator = this.parentGroup.getRestrictionViolator(null);
		if (restrictionViolator == null) return true;

		this.parentCommand.getParser().addError(new ParseErrors.MultipleArgsInRestrictedGroupUsedError(
			this.type.getLastParseState().getIndexAndOffset(), restrictionViolator
		));
		return false;
	}

	/**
	 * Checks if this argument matches the given name, including the prefix.
	 * <p>
	 * For example, if the prefix is {@code '-'} and the argument has the name {@code "help"}, this method
	 * will return {@code true} if the name is {@code "--help"}.
	 * </p>
	 *
	 * @param name the name to check
	 * @return {@code true} if the name matches, {@code false} otherwise.
	 */
	public boolean checkMatch(@NotNull String name) {
		var argPrefix = this.getPrefix().getCharacter();

		if (name.charAt(0) != argPrefix) return false;

		return this.hasName(Argument.removePrefix(name, argPrefix));
	}

	/**
	 * Checks if this argument matches the given single character name.
	 *
	 * @param name the name to check
	 * @return {@code true} if the name matches, {@code false} otherwise.
	 * @see #checkMatch(String)
	 */
	public boolean checkMatch(char name) {
		return this.hasName(Character.toString(name));
	}

	/**
	 * Executes the correct or the error callback depending on whether the argument has errors or not.
	 * <p>
	 * The correct callback is only executed if the argument was used and has no errors.
	 *
	 * @param okValue the value to pass to the correct callback
	 */
	// no worries about casting here, it will always receive the correct type
	@SuppressWarnings("unchecked")
	void invokeCallbacks(@Nullable Object okValue) {
		if (this.hasExitErrors()) {
			// invoke the error callback
			this.invokeCallbacks();
			return;
		}

		if (okValue == null
			|| this.onCorrectCallback == null
			|| this.getUsageCount() == 0
			|| !this.parentCommand.shouldExecuteCorrectCallback()
		) return;

		this.onCorrectCallback.accept((@NotNull TInner)okValue);

	}

	/**
	 * Returns {@code true} if the argument specified by the given name is equal to this argument.
	 * <p>
	 * Equality is determined by the argument's name and the command it belongs to.
	 * </p>
	 *
	 * @param obj the argument to compare to
	 * @return {@code true} if the argument specified by the given name is equal to this argument
	 */
	@Override
	public boolean equals(@NotNull Object obj) {
		if (obj instanceof Argument<?, ?> arg)
			return UtlMisc.equalsByNamesAndParentCmd(this, arg);
		return false;
	}

	/**
	 * Compares two arguments by the synopsis view priority order.
	 * <p>
	 * <b>Order:</b>
	 * Positional > Unique > Required > Optional.
	 * </p>
	 *
	 * @param first the first argument to compare
	 * @param second the second argument to compare
	 * @return 0 if both arguments are equal, -1 if the first argument goes before the second, 1 if the second goes
	 * 	before the first.
	 */
	public static int compareByPriority(@NotNull Argument<?, ?> first, @NotNull Argument<?, ?> second) {
		return new MultiComparator<Argument<?, ?>>()
			.addPredicate(Argument::isPositional, 2)
			.addPredicate(Argument::isUnique, 1)
			.addPredicate(Argument::isRequired)
			.compare(first, second);
	}

	/**
	 * Sorts the given list of arguments by the synopsis view priority order.
	 *
	 * @param args the arguments to sort
	 * @return the sorted list
	 * @see #compareByPriority(Argument, Argument)
	 */
	public static @NotNull List<Argument<?, ?>> sortByPriority(@NotNull List<@NotNull Argument<?, ?>> args) {
		return args.stream()
			.sorted(Argument::compareByPriority)
			.toList();
	}

	@Override
	public void resetState() {
		this.type.resetState();
	}

	/**
	 * Returns {@code true} if this argument is not part of any command or group.
	 * @return {@code true} if this argument is not part of any command or group.
	 */
	public boolean isOrphan() {
		return this.parentCommand == null && this.parentGroup == null;
	}

	@Override
	public @NotNull String toString() {
		var buff = new StringBuilder();

		buff.append("Argument<%s>{names=%s, prefix='%c', defaultValue=%s".formatted(
			this.type.getClass().getSimpleName(), this.names, this.getPrefix().getCharacter(), this.defaultValue
		));

		var options = new ArrayList<String>(4);
		if (this.required) options.add("required");
		if (this.positional) options.add("positional");
		if (this.unique) options.add("unique");
		if (!this.visible) options.add("hidden");

		if (!options.isEmpty()) {
			buff.append(", (");
			buff.append(String.join(", ", options));
			buff.append(")");
		}

		buff.append('}');

		return buff.toString();
	}


	// ------------------------------------------------ Error Handling ------------------------------------------------
	// just act as a proxy to the type error handling

	@Override
	public void addError(@NotNull ArgumentTypeError error) {
		this.type.addError(error);
	}

	@Override
	public @NotNull List<@NotNull ArgumentTypeError> getErrorsUnderExitLevel() {
		return this.type.getErrorsUnderExitLevel();
	}

	@Override
	public @NotNull List<@NotNull ArgumentTypeError> getErrorsUnderDisplayLevel() {
		return this.type.getErrorsUnderDisplayLevel();
	}

	@Override
	public boolean hasExitErrors() {
		return this.type.hasExitErrors();
	}

	@Override
	public boolean hasDisplayErrors() {
		return this.type.hasDisplayErrors();
	}

	@Override
	public void setMinimumDisplayErrorLevel(@NotNull ErrorLevel level) {
		this.type.setMinimumDisplayErrorLevel(level);
	}

	@Override
	public @NotNull ModifyRecord<@NotNull ErrorLevel> getMinimumDisplayErrorLevel() {
		return this.type.getMinimumDisplayErrorLevel();
	}

	@Override
	public void setMinimumExitErrorLevel(@NotNull ErrorLevel level) {
		this.type.setMinimumExitErrorLevel(level);
	}

	@Override
	public @NotNull ModifyRecord<@NotNull ErrorLevel> getMinimumExitErrorLevel() {
		return this.type.getMinimumExitErrorLevel();
	}

	/**
	 * Specify a function that will be called if an error occurs when parsing this argument.
	 * <p>
	 * <strong>Note</strong> that this callback is only called if the error was dispatched by this argument's type.
	 * That
	 * is, if the argument, for example, is required, and the user does not specify a value, an error will be
	 * thrown, but this callback will not be called, as the error was not dispatched by this argument's type.
	 * </p>
	 *
	 * @param callback the function that will be called if an error occurs when parsing this argument.
	 */
	@Override
	public void setOnErrorCallback(@Nullable Consumer<@NotNull Argument<Type, TInner>> callback) {
		this.onErrorCallback = callback;
	}

	/**
	 * Specify a function that will be called with the value introduced by the user.
	 * <p>
	 * By default this callback is called only if all commands succeed, but you can change this behavior with
	 * {@link Command#setCallbackInvocationOption(Command.CallbackInvocationOption)}
	 * </p>
	 *
	 * @param callback the function that will be called with the value introduced by the user.
	 */
	@Override
	public void setOnOkCallback(@Nullable Consumer<@NotNull TInner> callback) {
		this.onCorrectCallback = callback;
	}

	/**
	 * <b>NOTE:</b> Only invokes the error callback! Use {@link Argument#invokeCallbacks(Object)} for invoking both.
	 */
	@Override
	public void invokeCallbacks() {
		if (this.onErrorCallback == null) return;
		this.onErrorCallback.accept(this);
	}

	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Removes the prefix from the given name. If the name does not have the prefix, then it will be returned as is.
	 * <p>
	 * Example: {@code removePrefix("--name", '-')} or {@code removePrefix("-name", '-')} will return {@code "name"}.
	 * @param name the name to remove the prefix from
	 * @param character the prefix character to remove
	 * @return the name without the prefix
	 */
	public static @NotNull String removePrefix(@NotNull String name, char character) {
		if (name.length() == 1) return name; // if the name is a single character, then it can't have a prefix

		// if the first character is not the prefix, then it can't have a prefix
		if (name.charAt(0) != character) return name;

		// here we know that the first character is the prefix, so we remove it
		if (name.charAt(1) != character) return name.substring(1);

		// if the second character is also the prefix, then we remove both
		return name.substring(2);
	}

	/**
	 * Used in {@link CommandTemplate}s to specify the properties of an argument belonging to the command.
	 * @see CommandTemplate
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Define {
		/** @see Argument#setNames(List)  */
		@NotNull String[] names() default { };

		/** @see Argument#setDescription(String) */
		@NotNull String description() default "";

		/** @see ArgumentBuilder#type(ArgumentType) */
		@NotNull Class<? extends ArgumentType<?>> type() default DummyArgumentType.class;

		/**
		 * Specifies the prefix character for this argument.
		 * <p>
		 * By default, this is set to the value of {@link Prefix#defaultPrefix}.
		 * @see Argument#setPrefix(Prefix)
		 * */
		@NotNull Argument.Prefix prefix() default Prefix.DEFAULT;

		/** @see Argument#setRequired(boolean) */
		boolean required() default false;

		/** @see Argument#setPositional(boolean) */
		boolean positional() default false;

		/** @see Argument#setUnique(boolean) */
		boolean unique() default false;

		/** @see Argument#setVisible(boolean) */
		boolean visible() default true;

		/**
		 * The name of the group this argument will be added to; in the case the named group does not exist then it
		 * will be created.
		 * If multiple arguments have the same group name, they will be added to the same group.
		 */
		@NotNull String group() default "";
	}


	/**
	 * Specifies the prefix character for an {@link Argument}.
	 */
	public enum Prefix {
		/** The minus sign (-). */
		MINUS('-'),
		/** The plus sign (+). */
		PLUS('+'),
		/** The slash (/). */
		SLASH('/'),
		/** The at sign (@). */
		AT('@'),
		/** The percent sign (%). */
		PERCENT('%'),
		/** The caret (^). */
		CARET('^'),
		/** The exclamation mark (!). */
		EXCLAMATION('!'),
		/** The tilde (~). */
		TILDE('~'),
		/** The question mark (?). */
		QUESTION('?'),
		/** The equals sign (=). */
		EQUALS('='),
		/** The colon (:). */
		COLON(':'),

		/** Automatically set depending on the Operating System. On Linux, it will be
		 * {@link Prefix#MINUS}, and on Windows, it will be {@link Prefix#SLASH}. */
		AUTO(System.getProperty("os.name").toLowerCase().contains("win") ? SLASH : MINUS),

		/** Set to the value of {@link Prefix#getDefaultPrefix()}. */
		DEFAULT;


		private final @Nullable Character character;
		private static @NotNull Argument.Prefix defaultPrefix = Prefix.AUTO;

		/** Prefixes that a user may be familiar with. */
		public static final @NotNull Prefix[] COMMON_PREFIXES = { MINUS, SLASH };


		Prefix(char character) {
			this.character = character;
		}

		Prefix() {
			this.character = null;
		}

		Prefix(Prefix prefix) {
			this.character = prefix.character;
		}

		/**
		 * Sets the default prefix character. This is used when the prefix is set to {@link Prefix#DEFAULT}.
		 * @param prefix the new default prefix character
		 * @throws IllegalArgumentException if the prefix character is {@link Prefix#DEFAULT}
		 */
		public static void setDefaultPrefix(@NotNull Argument.Prefix prefix) {
			if (prefix == DEFAULT)
				throw new IllegalArgumentException("Cannot set the default prefix to DEFAULT");

			Prefix.defaultPrefix = prefix;
		}

		/**
		 * Returns the default prefix character. This is used when the prefix is set to {@link Prefix#DEFAULT}.
		 * @return the default prefix character
		 */
		public static @NotNull Argument.Prefix getDefaultPrefix() {
			return Prefix.defaultPrefix;
		}

		/**
		 * Returns the character that represents this prefix. If this prefix is {@link Prefix#DEFAULT}, then the
		 * default prefix character ({@link #getDefaultPrefix}) will be returned.
		 * @return the character that represents this prefix.
		 */
		public char getCharacter() {
			// this can never recurse because the default prefix is never DEFAULT
			return Objects.requireNonNullElseGet(this.character, () -> Prefix.defaultPrefix.getCharacter());
		}

		/**
		 * Returns a {@link Prefix} that can't be {@link Prefix#DEFAULT} from the given {@link Prefix}.
		 * If the given prefix is {@link Prefix#DEFAULT}, then the default prefix will be returned.
		 * @param prefix the prefix character
		 * @return the given prefix character if it is not {@link Prefix#DEFAULT}, or the default prefix otherwise.
		 */
		public static @NotNull Argument.Prefix getFromMaybeDefault(@NotNull Argument.Prefix prefix) {
			return prefix == DEFAULT ? Prefix.getDefaultPrefix() : prefix;
		}

		@Override
		public String toString() {
			return String.valueOf(this.getCharacter());
		}
	}
}