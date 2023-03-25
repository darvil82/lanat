package lanat;

import fade.mirror.MField;
import lanat.argumentTypes.BooleanArgument;
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

import static fade.mirror.Mirror.mirror;

/**
 * <h2>Argument</h2>
 * <p>
 * An Argument specifies a value that the user can introduce to the command. This value will be parsed by the specified
 * {@link ArgumentType} each time the Argument is used. Once finished parsing, the value may be retrieved by using
 * {@link ParsedArguments#get(String)} on the {@link ParsedArguments} object returned by
 * {@link ArgumentParser#parse(CLInput)}.
 *
 * <p>
 * An Argument can be created using the factory methods available, like {@link Argument#create(String...)}.
 * </p>
 * <br><br>
 * <h3>Example:</h3>
 *
 * <p>
 * An Argument with the names "name" and "n" that will parse an integer value. There are several ways to create this
 * argument.
 * </p>
 * <h4>Using the factory methods:</h4>
 * <pre>
 * {@code
 *     Argument.create(ArgumentType.INTEGER(), "name", "n");
 *     Argument.create("name", ArgumentType.INTEGER())
 *         .addNames("n");
 * }
 * </pre>
 *
 * <h4>Using the constructors:</h4>
 * <pre>
 * {@code
 *     new Argument<>(ArgumentType.INTEGER(), "name", "n");
 *     new Argument<>("name", ArgumentType.INTEGER())
 *         .addNames("n");
 * }
 * </pre>
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

	/** The Command that this Argument belongs to. This should never be null after initialization. */
	private Command parentCommand;

	/**
	 * The ArgumentGroup that this Argument belongs to. If this Argument does not belong to any group, this may be
	 * null.
	 */
	private @Nullable ArgumentGroup parentGroup;

	// callbacks for error handling
	private @Nullable Consumer<@NotNull Argument<Type, TInner>> onErrorCallback;
	private @Nullable Consumer<@NotNull TInner> onCorrectCallback;

	/**
	 * The color that this Argument will have in places where it is displayed, such as the help message. By default, the
	 * color will be picked from the {@link Command#colorsPool} of the parent command at
	 * {@link Argument#setParentCommand(Command)}.
	 */
	private final @NotNull ModifyRecord<Color> representationColor = new ModifyRecord<>(null);


	/**
	 * The list of prefixes that can be used.
	 * <p>
	 * The {@link PrefixChar#AUTO} prefix will be automatically set depending on the Operating System.
	 * </p>
	 * @see PrefixChar#AUTO
	 * */
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
		 * This prefix will be automatically set depending on the Operating System.
		 * On Linux, it will be {@link PrefixChar#MINUS}, and on Windows, it will be {@link PrefixChar#SLASH}.
		 * */
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
		 * The constant fields of this class should be used instead of this method. Other characters
		 * could break compatibility with shells using special characters as prefixes, such as the <code>|</code> or <code>;</code> characters.
		 * </p>
		 * @param character the character that will be used as a prefix
		 */
		public static @NotNull PrefixChar fromCharUnsafe(char character) {
			if (Character.isWhitespace(character))
				throw new IllegalArgumentException("The character cannot be a whitespace character.");
			return new PrefixChar(character);
		}
	}

	public static class ArgumentBuilder<Type extends ArgumentType<TInner>, TInner> {
		private @NotNull String @Nullable [] names;
		private @Nullable String description;
		private @Nullable Type argType;
		private boolean obligatory = false,
			positional = false,
			allowUnique = false;
		private @Nullable TInner defaultValue;
		private @Nullable Consumer<@NotNull Argument<Type, TInner>> onErrorCallback;
		private @Nullable Consumer<@NotNull TInner> onCorrectCallback;
		private @Nullable PrefixChar prefixChar = PrefixChar.defaultPrefix;

		public ArgumentBuilder() {}

		@SuppressWarnings("unchecked")
		public static @NotNull <Type extends ArgumentType<TInner>, TInner>
		ArgumentBuilder<Type, TInner> fromField(@NotNull MField<?> field, @NotNull Argument.Define annotation) {
			final String[] names = annotation.names();
			final var argTypeCtor = mirror(annotation.type()).getConstructor();

			final var argumentBuilder = new ArgumentBuilder<Type, TInner>()
				.withNames(names.length == 0 ? new String[] { field.getName() } : names);

			if (argTypeCtor.isPresent()) {
				final var argType = argTypeCtor.get().invokeWithNoInstance();
				argumentBuilder.withArgType((Type)argType);
			}

			argumentBuilder.withPrefixChar(PrefixChar.fromCharUnsafe(annotation.prefix()));
			if (!annotation.description().isEmpty()) argumentBuilder.withDescription(annotation.description());
			if (annotation.obligatory()) argumentBuilder.obligatory();
			if (annotation.positional()) argumentBuilder.positional();
			if (annotation.allowUnique()) argumentBuilder.allowsUnique();

			return argumentBuilder;
		}

		public ArgumentBuilder<Type, TInner> withDescription(@NotNull String description) {
			this.description = description;
			return this;
		}

		public ArgumentBuilder<Type, TInner> obligatory() {
			this.obligatory = true;
			return this;
		}

		public ArgumentBuilder<Type, TInner> positional() {
			this.positional = true;
			return this;
		}

		public ArgumentBuilder<Type, TInner> allowsUnique() {
			this.allowUnique = true;
			return this;
		}

		public ArgumentBuilder<Type, TInner> withDefaultValue(TInner defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		/**
		 * Specify a function that will be called with the value introduced by the user.
		 * <p>
		 * By default this callback is called only if all commands succeed, but you can change this behavior with
		 * {@link Command#invokeCallbacksWhen(CallbacksInvocationOption)}
		 * </p>
		 * @param callback the function that will be called with the value introduced by the user.
		 */
		public ArgumentBuilder<Type, TInner> onOk(Consumer<TInner> callback) {
			this.onCorrectCallback = callback;
			return this;
		}

		/**
		 * Specify a function that will be called if an error occurs when parsing this argument.
		 * <p>
		 * <strong>Note</strong> that this callback is only called if the error was dispatched by this argument's type. That
		 * is,
		 * if the argument, for example, is obligatory, and the user does not specify a value, an error will be thrown, but
		 * this callback will not be called, as the error was not dispatched by this argument's type.
		 * </p>
		 * @param callback the function that will be called if an error occurs when parsing this argument.
		 */
		public ArgumentBuilder<Type, TInner> onErr(Consumer<Argument<Type, TInner>> callback) {
			this.onErrorCallback = callback;
			return this;
		}

		public ArgumentBuilder<Type, TInner> withPrefixChar(PrefixChar prefixChar) {
			this.prefixChar = prefixChar;
			return this;
		}

		public ArgumentBuilder<Type, TInner> withNames(String... names) {
			this.names = names;
			return this;
		}

		public ArgumentBuilder<Type, TInner> withArgType(Type argType) {
			this.argType = argType;
			return this;
		}

		public Argument<Type, TInner> build() {
			if (this.names == null || this.names.length == 0)
				throw new IllegalStateException("The argument must have at least one name.");

			if (this.argType == null)
				throw new IllegalStateException("The argument must have a type defined.");

			final Argument<Type, TInner> argument = new Argument<>(this.argType, this.names);
			argument.setDescription(this.description);
			argument.setObligatory(this.obligatory);
			argument.setPositional(this.positional);
			argument.setAllowUnique(this.allowUnique);
			argument.setDefaultValue(this.defaultValue);
			argument.setPrefix(this.prefixChar);
			argument.setOnErrorCallback(this.onErrorCallback);
			argument.setOnCorrectCallback(this.onCorrectCallback);
			return argument;
		}

		boolean hasName(@NotNull String name) {
			if (this.names == null)
				return false;

			for (String argName : this.names) {
				if (argName.equalsIgnoreCase(name))
					return true;
			}

			return false;
		}
	}

	private Argument(@NotNull Type type, @NotNull String... names) {
		this.argType = type;
		this.addNames(names);
	}

	public static <Type extends ArgumentType<TInner>, TInner>
	ArgumentBuilder<Type, TInner> create() {
		return new ArgumentBuilder<>();
	}

	/**
	 * Creates an argument with a {@link BooleanArgument} type.
	 * @param names the names of the argument. See {@link Argument#addNames(String...)} for more information.
	 */
	public static ArgumentBuilder<BooleanArgument, Boolean> create(@NotNull String... names) {
		return Argument.<BooleanArgument, Boolean>create().withNames(names).withArgType(new BooleanArgument());
	}

	/** Creates an argument with the specified name and type.
	 * @param name the name of the argument. See {@link Argument#addNames(String...)} for more information.
	 * @param argType the type of the argument. This is the subParser that will be used to parse the value/s this
	 * 	argument should receive.
	 * */
	public static <Type extends ArgumentType<TInner>, TInner>
	ArgumentBuilder<Type, TInner> create(@NotNull String name, @NotNull Type argType) {
		return Argument.create(argType, name);
	}

	/** Creates an argument with the specified type and names.
	 *  @param argType the type of the argument. This is the subParser that will be used to parse the value/s this
	 *  argument should receive.
	 *  @param names the names of the argument. See {@link Argument#addNames(String...)} for more information.
	 * */
	public static <Type extends ArgumentType<TInner>, TInner>
	ArgumentBuilder<Type, TInner> create(@NotNull Type argType, @NotNull String... names) {
		return Argument.<Type, TInner>create().withNames(names).withArgType(argType);
	}

	/** Creates an argument with the specified single character name and type.
	 * @param name the name of the argument. See {@link Argument#addNames(String...)} for more information.
	 * @param argType the type of the argument. This is the subParser that will be used to parse the value/s this
	 * */
	public static <Type extends ArgumentType<TInner>, TInner>
	ArgumentBuilder<Type, TInner> create(char name, @NotNull Type argType) {
		return Argument.create(argType, String.valueOf(name));
	}

	/** Creates an argument with the specified single character name, full name and type.
	 * <p>
	 * This is equivalent to calling <pre>{@code Argument.create(charName, argType).addNames(fullName)}</pre>
	 *
	 * @param charName the single character name of the argument.
	 * @param fullName the full name of the argument.
	 * @param argType the type of the argument. This is the subParser that will be used to parse the value/s this
	 * */
	public static <Type extends ArgumentType<TInner>, TInner>
	ArgumentBuilder<Type, TInner> create(char charName, @NotNull String fullName, @NotNull Type argType) {
		return Argument.create(argType).withNames(fullName, String.valueOf(charName));
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
	 * @param value the value that should be used if the user does not specify a value for this argument. If the
	 *  value is null, then no default value will be used.
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
			.map(UtlString::sanitizeName)
			.forEach(newName -> {
				if (this.names.contains(newName)) {
					throw new IllegalArgumentException("Name '" + newName + "' is already used by this argument.");
				}
				this.names.add(newName);
			});
	}

	@Override
	public @NotNull List<@NotNull String> getNames() {
		return Collections.unmodifiableList(this.names);
	}

	/** Sets the description of this argument. This description will be shown in the help message.
	 * @param description the description of this argument.
	 * */
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
	void setParentCommand(@NotNull Command parentCommand) {
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
	void setParentGroup(@NotNull ArgumentGroup parentGroup) {
		if (this.parentGroup != null) {
			throw new ArgumentAlreadyExistsException(this, this.parentGroup);
		}
		this.parentGroup = parentGroup;
	}

	/**
	 * Returns the {@link ArgumentGroup} that contains this argument, or null if it does not have one.
	 * @return the parent group of this argument, or null if it does not have one.
	 */
	public @Nullable ArgumentGroup getParentGroup() {
		return this.parentGroup;
	}

	/**
	 * The number of times this argument has been used in a command during parsing.
	 * @return the number of times this argument has been used in a command.
	 */
	public short getUsageCount() {
		return this.argType.usageCount;
	}

	/**
	 * The color that will be used to represent this argument in the help message.
	 * @return the color that will be used to represent this argument in the help message.
	 */
	public @NotNull Color getRepresentationColor() {
		return this.representationColor.get();
	}

	/**
	 * Sets the color that will be used to represent this argument in the help message.
	 * @param color the color that will be used to represent this argument in the help message.
	 */
	public void setRepresentationColor(@NotNull Color color) {
		this.representationColor.set(color);
	}


	/**
	 * Returns <code>true</code> if this argument is the help argument of its parent command.
	 * This just checks if the argument's name is "help" and if it is marked with {@link #setAllowUnique(boolean)}.
	 * @return <code>true</code> if this argument is the help argument of its parent command.
	 */
	public boolean isHelpArgument() {
		return this.getName().equals("help") && this.isUniqueAllowed();
	}

	/**
	 * Pass the specified values array to the argument type to parse it.
	 *
	 * @param values The values array that should be parsed.
	 * @param tokenIndex This is the global index of the token that is currently being parsed. Used when dispatching
	 * 	errors.
	 */
	public void parseValues(@NotNull String @NotNull [] values, short tokenIndex) {
		// check if the argument was used more times than it should
		if (++this.argType.usageCount > this.argType.getRequiredUsageCount().max()) {
			this.parentCommand.getParser()
				.addError(
					ParseError.ParseErrorType.ARG_INCORRECT_USAGES_COUNT,
					this, values.length, this.argType.getLastTokenIndex() + 1
				);
			return;
		}

		this.argType.setLastTokenIndex(tokenIndex);
		this.argType.parseAndUpdateValue(values);
	}

	/**
	 * {@link #parseValues(String[], short)} but passes in an empty values array to parse.
	 */
	public void parseValues(short tokenIndex) {
		this.parseValues(new String[0], tokenIndex);
	}

	/**
	 * This method is called when the command is finished parsing. <strong>And should only ever be called once
	 * (per parse).</strong>
	 * @return the final value parsed by the argument type, or the default value if the argument was not used.
	 */
	public @Nullable TInner finishParsing() {
		final TInner finalValue = this.argType.getFinalValue();
		final TInner defaultValue = this.defaultValue == null ? this.argType.getInitialValue() : this.defaultValue;

		/* no, | is not a typo. We don't want the OR operator to short-circuit, we want all of them to be evaluated
		 * because the methods have side effects (they add errors to the parser) */
		TInner returnValue = (finalValue == null | !this.finishParsingCheckExclusivity() | !this.finishParsingCheckUsageCount())
			? defaultValue
			: finalValue;

		this.argType.getErrorsUnderDisplayLevel().forEach(this.parentCommand.getParser()::addError);
		if (this.parentGroup != null) this.parentGroup.setArgUsed();

		// if the argument type has a value defined (even if it wasn't used), use that. Otherwise, use the default value
		return returnValue;
	}

	/**
	 * Checks if the argument was used the correct amount of times.
	 * @return <code>true</code> if the argument was used the correct amount of times.
	 */
	private boolean finishParsingCheckUsageCount() {
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
	 * @return <code>true</code> if there is no violation of exclusivity in the group hierarchy.
	 */
	private boolean finishParsingCheckExclusivity() {
		// check if the parent group of this argument is exclusive, and if so, check if any other argument in it has been used
		if (this.parentGroup == null || this.getUsageCount() == 0) return true;

		ArgumentGroup exclusivityResult = this.parentGroup.checkExclusivity(null);
		if (exclusivityResult == null) return true;

		this.parentCommand.getParser().addError(
			new ParseError(
				ParseError.ParseErrorType.MULTIPLE_ARGS_IN_EXCLUSIVE_GROUP_USED,
				this.argType.getLastTokenIndex(),
				this, this.argType.getLastReceivedValueCount()
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
	 * For example, if the prefix is <code>'-'</code> and the argument has the name <code>"help"</code>, this method will
	 * return <code>true</code> if the name is <code>"--help"</code>.
	 * </p>
	 * @param name the name to check
	 * @return <code>true</code> if the name matches, <code>false</code> otherwise.
	 */
	public boolean checkMatch(@NotNull String name) {
		final char prefixChar = this.getPrefix().character;
		return this.names.stream()
			.anyMatch(a -> name.equals("" + prefixChar + a) || name.equals("" + prefixChar + prefixChar + a));
	}

	/**
	 * Checks if this argument matches the given single character name.
	 * @see #checkMatch(String)
	 * @param name the name to check
	 * @return <code>true</code> if the name matches, <code>false</code> otherwise.
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
	 * Returns <code>true</code> if the argument specified by the given name is equal to this argument.
	 * <p>
	 * Equality is determined by the argument's name and the command it belongs to.
	 * </p>
	 * @param obj the argument to compare to
	 * @return <code>true</code> if the argument specified by the given name is equal to this argument
	 */
	public boolean equals(@NotNull Argument<?, ?> obj) {
		return Command.equalsByNamesAndParentCmd(this, obj);
	}

	/**
	 * Compares two arguments by the synopsis view priority order.
	 * <p>
	 * <b>Order:</b>
	 * Positional > Obligatory > Optional.
	 * </p>
	 *
	 * @param first the first argument to compare
	 * @param second the second argument to compare
	 * @return 0 if both arguments are equal, -1 if the first argument goes before the second, 1 if the second goes
	 * 	before the first.
	 */
	public static int compareByPriority(@NotNull Argument<?, ?> first, @NotNull Argument<?, ?> second) {
		return new MultiComparator<Argument<?, ?>>()
			.addPredicate(Argument::isPositional, 1)
			.addPredicate(Argument::isObligatory)
			.compare(first, second);
	}

	/**
	 * Sorts the given list of arguments by the synopsis view priority order.
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

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Define {
		String[] names() default {};
		String description() default "";
		Class<? extends ArgumentType<?>> type();
		char prefix() default '-';
		boolean obligatory() default false;
		boolean positional() default false;
		boolean allowUnique() default false;
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

	@Override
	public void setOnErrorCallback(@Nullable Consumer<@NotNull Argument<Type, TInner>> callback) {
		this.onErrorCallback = callback;
	}

	@Override
	public void setOnCorrectCallback(@Nullable Consumer<@NotNull TInner> callback) {
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


