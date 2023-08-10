package lanat;

import lanat.argumentTypes.BooleanArgumentType;
import lanat.argumentTypes.DummyArgumentType;
import lanat.exceptions.ArgumentAlreadyExistsException;
import lanat.parsing.errors.CustomError;
import lanat.parsing.errors.ParseError;
import lanat.utils.*;
import lanat.utils.displayFormatter.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;


/**
 * <h2>Argument</h2>
 * <p>
 * An Argument specifies a value that the user can introduce to the command. This value will be parsed by the specified
 * {@link ArgumentType} each time the Argument is used. Once finished parsing, the value may be retrieved by using
 * {@link ParsedArguments#get(String)} on the {@link ParsedArguments} object returned by
 * {@link ArgumentParser#parse(CLInput)}.
 *
 * <p>
 * An Argument can be created using the factory methods available, like {@link Argument#createOfBoolType(String...)}.
 * </p>
 * <br><br>
 * <h3>Example:</h3>
 *
 * <p>
 * An Argument with the names "name" and 'n' that will parse an integer value. In order to create an Argument, you need
 * to call any of the static factory methods available, like {@link Argument#createOfBoolType(String...)}. These methods
 * will return an {@link ArgumentBuilder} object, which can be used to specify the Argument's properties easily.
 * </p>
 * <pre>
 * {@code
 *     Argument.create('n', "name", ArgumentType.INTEGER());
 *     Argument.create("name", ArgumentType.INTEGER())
 *         .addNames("n");
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
 * @see ArgumentGroup
 * @see ArgumentParser
 */
public class Argument<Type extends ArgumentType<TInner>, TInner>
	implements ErrorsContainer<CustomError>,
	ErrorCallbacks<TInner,
		Argument<Type, TInner>>,
	Resettable,
	CommandUser,
	ArgumentGroupUser,
	MultipleNamesAndDescription
{
	/**
	 * The type of this argument. This is the subParser that will be used to parse the value/s this argument should
	 * receive.
	 */
	public final @NotNull Type argType;
	private PrefixChar prefixChar = PrefixChar.defaultPrefix;
	private final @NotNull List<@NotNull String> names = new ArrayList<>();
	private @Nullable String description;
	private boolean obligatory = false,
		positional = false,
		allowUnique = false;

	private @Nullable TInner defaultValue;

	/** The Command that this Argument belongs to. This should never be {@code null} after initialization. */
	private Command parentCommand;

	/**
	 * The ArgumentGroup that this Argument belongs to. If this Argument does not belong to any group, this may be
	 * {@code null}.
	 */
	private @Nullable ArgumentGroup parentGroup;

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
	 * The list of prefixes that can be used.
	 * <p>
	 * The {@link PrefixChar#AUTO} prefix will be automatically set depending on the Operating System.
	 * </p>
	 *
	 * @see PrefixChar#AUTO
	 */
	public static class PrefixChar {
		public static final PrefixChar MINUS = new PrefixChar('-');
		public static final PrefixChar PLUS = new PrefixChar('+');
		public static final PrefixChar SLASH = new PrefixChar('/');
		public static final PrefixChar AT = new PrefixChar('@');
		public static final PrefixChar PERCENT = new PrefixChar('%');
		public static final PrefixChar CARET = new PrefixChar('^');
		public static final PrefixChar EXCLAMATION = new PrefixChar('!');
		public static final PrefixChar TILDE = new PrefixChar('~');
		public static final PrefixChar QUESTION = new PrefixChar('?');
		public static final PrefixChar EQUALS = new PrefixChar('=');
		public static final PrefixChar COLON = new PrefixChar(':');

		/**
		 * This prefix will be automatically set depending on the Operating System. On Linux, it will be
		 * {@link PrefixChar#MINUS}, and on Windows, it will be {@link PrefixChar#SLASH}.
		 */
		public static final PrefixChar AUTO = System.getProperty("os.name").toLowerCase().contains("win") ? SLASH : MINUS;


		public final char character;
		public static @NotNull PrefixChar defaultPrefix = PrefixChar.MINUS;

		private PrefixChar(char character) {
			this.character = character;
		}

		/**
		 * Creates a new PrefixChar with the specified non-whitespace character.
		 * <p>
		 * <strong>NOTE:<br></strong>
		 * The constant fields of this class should be used instead of this method. Other characters could break
		 * compatibility with shells using special characters as prefixes, such as the <code>|</code> or <code>;</code>
		 * characters.
		 * </p>
		 *
		 * @param character the character that will be used as a prefix
		 */
		public static @NotNull PrefixChar fromCharUnsafe(char character) {
			if (Character.isWhitespace(character))
				throw new IllegalArgumentException("The character cannot be a whitespace character.");
			return new PrefixChar(character);
		}
	}

	Argument(@NotNull Type type, @NotNull String... names) {
		this.argType = type;
		this.addNames(names);
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
	 *
	 * @param argType the type of the argument. This is the subParser that will be used to parse the value/s this
	 * 	argument should receive.
	 * @param names the names of the argument. See {@link Argument#addNames(String...)} for more information.
	 */
	public static <Type extends ArgumentType<TInner>, TInner>
	ArgumentBuilder<Type, TInner> create(@NotNull Type argType, @NotNull String... names) {
		return Argument.<Type, TInner>create().withNames(names).withArgType(argType);
	}

	/**
	 * Creates an argument builder with the specified single character name and type.
	 *
	 * @param name the name of the argument. See {@link Argument#addNames(String...)} for more information.
	 * @param argType the type of the argument. This is the subParser that will be used to parse the value/s this
	 */
	public static <Type extends ArgumentType<TInner>, TInner>
	ArgumentBuilder<Type, TInner> create(@NotNull Type argType, char name) {
		return Argument.create(argType, String.valueOf(name));
	}

	/**
	 * Creates an argument builder with the specified single character name, full name and type.
	 * <p>
	 * This is equivalent to calling <pre>{@code Argument.create(charName, argType).addNames(fullName)}</pre>
	 *
	 * @param charName the single character name of the argument.
	 * @param fullName the full name of the argument.
	 * @param argType the type of the argument. This is the subParser that will be used to parse the value/s this
	 */
	public static <Type extends ArgumentType<TInner>, TInner>
	ArgumentBuilder<Type, TInner> create(@NotNull Type argType, char charName, @NotNull String fullName) {
		return Argument.create(argType).withNames(fullName, String.valueOf(charName));
	}

	/**
	 * Creates an argument builder with a {@link BooleanArgumentType} type.
	 *
	 * @param names the names of the argument. See {@link Argument#addNames(String...)} for more information.
	 */
	public static ArgumentBuilder<BooleanArgumentType, Boolean> createOfBoolType(@NotNull String... names) {
		return Argument.create(new BooleanArgumentType()).withNames(names);
	}


	/**
	 * Marks the argument as obligatory. This means that this argument should <b>always</b> be used by the user.
	 */
	public void setObligatory(boolean obligatory) {
		this.obligatory = obligatory;
	}

	public boolean isObligatory() {
		return this.obligatory;
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
		if (positional && this.argType.getRequiredArgValueCount().max() == 0) {
			throw new IllegalArgumentException("An argument that does not accept values cannot be positional");
		}
		this.positional = positional;
	}

	public boolean isPositional() {
		return this.positional;
	}

	/**
	 * Specify the prefix of this argument. By default, this is {@link PrefixChar#MINUS}. If this argument is used in an
	 * argument name list (-abc), the prefix that will be valid is any against all the arguments specified in that name
	 * list.
	 *
	 * @param prefixChar the prefix that should be used for this argument.
	 * @see PrefixChar
	 */
	public void setPrefix(PrefixChar prefixChar) {
		this.prefixChar = prefixChar;
	}

	/**
	 * Returns the prefix of this argument.
	 *
	 * @return the prefix of this argument.
	 */
	public PrefixChar getPrefix() {
		return this.prefixChar;
	}

	/**
	 * Specifies that this argument has priority over other arguments, even if they are obligatory. This means that if
	 * an argument in a command is set as obligatory, but one argument with {@link #allowUnique} was used, then the
	 * unused obligatory argument will not throw an error.
	 */
	public void setAllowUnique(boolean allowUnique) {
		this.allowUnique = allowUnique;
	}

	public boolean isUniqueAllowed() {
		return this.allowUnique;
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
	 * Add more names to this argument. This is useful if you want the same argument to be used with multiple different
	 * names.
	 * <br><br>
	 * <p>
	 * Single character names can be used in argument name lists (e.g. <code>-abc</code>), each alphabetic character
	 * being an argument name, that is, <code>-a -b -c</code>.
	 * </p>
	 *
	 * @param names the names that should be added to this argument.
	 */
	@Override
	public void addNames(@NotNull String... names) {
		Arrays.stream(names)
			.map(UtlString::requireValidName)
			.peek(n -> {
				if (this.names.contains(n))
					throw new IllegalArgumentException("Name '" + n + "' is already used by this argument.");
			})
			.forEach(this.names::add);

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
		if (this.parentCommand != null) {
			throw new ArgumentAlreadyExistsException(this, this.parentCommand);
		}

		this.parentCommand = parentCommand;
		this.representationColor.setIfNotModified(parentCommand.colorsPool.next());
	}

	@Override
	public Command getParentCommand() {
		return this.parentCommand;
	}

	/**
	 * Sets the parent group of this argument. This is called when adding the Argument to a group at
	 * {@link ArgumentGroup#addArgument(Argument)}
	 */
	@Override
	public void registerToGroup(@NotNull ArgumentGroup parentGroup) {
		if (this.parentGroup != null) {
			throw new ArgumentAlreadyExistsException(this, this.parentGroup);
		}

		this.parentGroup = parentGroup;
	}

	/**
	 * Returns the {@link ArgumentGroup} that contains this argument, or {@code null} if it does not have one.
	 *
	 * @return the parent group of this argument, or {@code null} if it does not have one.
	 */
	@Override
	public @Nullable ArgumentGroup getParentGroup() {
		return this.parentGroup;
	}

	/**
	 * The number of times this argument has been used in a command during parsing.
	 *
	 * @return the number of times this argument has been used in a command.
	 */
	public short getUsageCount() {
		return this.argType.usageCount;
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
	 * Returns {@code true} if this argument is the help argument of its parent command. This just checks if the
	 * argument's name is "help" and if it is marked with {@link #setAllowUnique(boolean)}.
	 *
	 * @return {@code true} if this argument is the help argument of its parent command.
	 */
	public boolean isHelpArgument() {
		return this.getName().equals("help") && this.isUniqueAllowed();
	}

	/**
	 * Pass the specified values array to the argument type to parse it.
	 *
	 * @param tokenIndex This is the global index of the token that is currently being parsed. Used when dispatching
	 * 	errors.
	 * @param values The value array that should be parsed.
	 */
	public void parseValues(short tokenIndex, @NotNull String... values) {
		// check if the argument was used more times than it should
		if (++this.argType.usageCount > this.argType.getRequiredUsageCount().max()) {
			this.parentCommand.getParser()
				.addError(
					ParseError.ParseErrorType.ARG_INCORRECT_USAGES_COUNT,
					this, values.length, this.argType.getLastTokenIndex() + 1
				);
			return;
		}

		this.argType.parseAndUpdateValue(tokenIndex, values);
	}

	/**
	 * This method is called when the command is finished parsing. <strong>And should only ever be called once (per
	 * parse).</strong>
	 *
	 * @return the final value parsed by the argument type, or the default value if the argument was not used.
	 */
	public @Nullable TInner finishParsing() {
		final TInner finalValue = this.argType.getValue();
		final TInner defaultValue = this.defaultValue == null ? this.argType.getInitialValue() : this.defaultValue;

		/* no, | is not a typo. We don't want the OR operator to short-circuit, we want all of them to be evaluated
		 * because the methods have side effects (they add errors to the parser) */
		TInner returnValue = (finalValue == null | !this.finishParsing$checkExclusivity() | !this.finishParsing$checkUsageCount())
			? defaultValue
			: finalValue;

		this.argType.getErrorsUnderDisplayLevel().forEach(this.parentCommand.getParser()::addError);
		if (this.parentGroup != null) this.parentGroup.setArgUsed();

		// if the argument type has a value defined (even if it wasn't used), use that. Otherwise, use the default value
		return returnValue;
	}

	/**
	 * Checks if the argument was used the correct amount of times.
	 *
	 * @return {@code true} if the argument was used the correct amount of times.
	 */
	private boolean finishParsing$checkUsageCount() {
		if (this.getUsageCount() == 0) {
			if (this.obligatory && !this.parentCommand.uniqueArgumentReceivedValue()) {
				this.parentCommand.getParser().addError(
					ParseError.ParseErrorType.OBLIGATORY_ARGUMENT_NOT_USED, this, 0
				);
				return false;
			}
			// make sure that the argument was used the minimum amount of times specified
		} else if (this.argType.usageCount < this.argType.getRequiredUsageCount().min()) {
			this.parentCommand.getParser()
				.addError(ParseError.ParseErrorType.ARG_INCORRECT_USAGES_COUNT, this, 0);
			return false;
		}
		return true;
	}

	/**
	 * Checks if the argument is part of an exclusive group, and if so, checks if there is any violation of exclusivity
	 * in the group hierarchy.
	 *
	 * @return {@code true} if there is no violation of exclusivity in the group hierarchy.
	 */
	private boolean finishParsing$checkExclusivity() {
		// check if the parent group of this argument is exclusive, and if so, check if any other argument in it has been used
		if (this.parentGroup == null || this.getUsageCount() == 0) return true;

		ArgumentGroup exclusivityResult = this.parentGroup.checkExclusivity(null);
		if (exclusivityResult == null) return true;

		this.parentCommand.getParser().addError(
			new ParseError(
				ParseError.ParseErrorType.MULTIPLE_ARGS_IN_EXCLUSIVE_GROUP_USED,
				this.argType.getLastTokenIndex(),
				this, this.argType.getLastReceivedValuesNum()
			)
			{{
				this.setArgumentGroup(exclusivityResult);
			}}
		);
		return false;
	}

	/**
	 * Checks if this argument matches the given name, including the prefix.
	 * <p>
	 * For example, if the prefix is <code>'-'</code> and the argument has the name <code>"help"</code>, this method
	 * will return {@code true} if the name is <code>"--help"</code>.
	 * </p>
	 *
	 * @param name the name to check
	 * @return {@code true} if the name matches, {@code false} otherwise.
	 */
	public boolean checkMatch(@NotNull String name) {
		final char prefixChar = this.getPrefix().character;
		return this.names.stream()
			.anyMatch(a -> name.equals("" + prefixChar + a) || name.equals("" + prefixChar + prefixChar + a));
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
			|| (!this.allowUnique && this.parentCommand.uniqueArgumentReceivedValue())
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
	 * Allows Unique > Positional > Obligatory > Optional.
	 * </p>
	 *
	 * @param first the first argument to compare
	 * @param second the second argument to compare
	 * @return 0 if both arguments are equal, -1 if the first argument goes before the second, 1 if the second goes
	 * 	before the first.
	 */
	public static int compareByPriority(@NotNull Argument<?, ?> first, @NotNull Argument<?, ?> second) {
		return new MultiComparator<Argument<?, ?>>()
			.addPredicate(Argument::isUniqueAllowed, 2)
			.addPredicate(Argument::isPositional, 1)
			.addPredicate(Argument::isObligatory)
			.compare(first, second);
	}

	/**
	 * Sorts the given list of arguments by the synopsis view priority order.
	 *
	 * @param args the arguments to sort
	 * @return the sorted list
	 * @see #compareByPriority(Argument, Argument)
	 */
	public static List<Argument<?, ?>> sortByPriority(@NotNull List<@NotNull Argument<?, ?>> args) {
		return new ArrayList<>(args) {{
			this.sort(Argument::compareByPriority);
		}};
	}

	@Override
	public void resetState() {
		this.argType.resetState();
	}

	@Override
	public @NotNull String toString() {
		return "Argument<%s>[names=%s, prefix='%c', obligatory=%b, positional=%b, allowUnique=%b, defaultValue=%s]"
			.formatted(
				this.argType.getClass().getSimpleName(), this.names, this.getPrefix().character, this.obligatory,
				this.positional, this.allowUnique, this.defaultValue
			);
	}

	/**
	 * Used in {@link CommandTemplate}s to specify the properties of an argument belonging to the command.
	 * @see CommandTemplate
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Define {
		/** @see Argument#addNames(String...) */
		String[] names() default { };

		/** @see Argument#setDescription(String) */
		String description() default "";

		/** @see ArgumentBuilder#withArgType(ArgumentType) */
		Class<? extends ArgumentType<?>> argType() default DummyArgumentType.class;

		/**
		 * Specifies the prefix character for this argument. This uses {@link PrefixChar#fromCharUnsafe(char)}.
		 * @see Argument#setPrefix(PrefixChar)
		 * */
		char prefix() default '-';

		/** @see Argument#setObligatory(boolean) */
		boolean obligatory() default false;

		/** @see Argument#setPositional(boolean) */
		boolean positional() default false;

		/** @see Argument#setAllowUnique(boolean) */
		boolean allowsUnique() default false;
	}


	// ------------------------------------------------ Error Handling ------------------------------------------------
	// just act as a proxy to the type error handling

	@Override
	public @NotNull List<@NotNull CustomError> getErrorsUnderExitLevel() {
		return this.argType.getErrorsUnderExitLevel();
	}

	@Override
	public @NotNull List<@NotNull CustomError> getErrorsUnderDisplayLevel() {
		return this.argType.getErrorsUnderDisplayLevel();
	}

	@Override
	public boolean hasExitErrors() {
		return this.argType.hasExitErrors() || !this.getErrorsUnderExitLevel().isEmpty();
	}

	@Override
	public boolean hasDisplayErrors() {
		return this.argType.hasDisplayErrors() || !this.getErrorsUnderDisplayLevel().isEmpty();
	}

	@Override
	public void setMinimumDisplayErrorLevel(@NotNull ErrorLevel level) {
		this.argType.setMinimumDisplayErrorLevel(level);
	}

	@Override
	public @NotNull ModifyRecord<@NotNull ErrorLevel> getMinimumDisplayErrorLevel() {
		return this.argType.getMinimumDisplayErrorLevel();
	}

	@Override
	public void setMinimumExitErrorLevel(@NotNull ErrorLevel level) {
		this.argType.setMinimumExitErrorLevel(level);
	}

	@Override
	public @NotNull ModifyRecord<@NotNull ErrorLevel> getMinimumExitErrorLevel() {
		return this.argType.getMinimumExitErrorLevel();
	}

	/**
	 * Specify a function that will be called if an error occurs when parsing this argument.
	 * <p>
	 * <strong>Note</strong> that this callback is only called if the error was dispatched by this argument's type.
	 * That
	 * is, if the argument, for example, is obligatory, and the user does not specify a value, an error will be
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
	 * {@link Command#setCallbackInvocationOption(CallbacksInvocationOption)}
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
}


