package lanat;

import lanat.argumentTypes.BooleanArgument;
import lanat.parsing.errors.CustomError;
import lanat.parsing.errors.ParseError;
import lanat.utils.*;
import lanat.utils.displayFormatter.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * <h2>Argument</h2>
 *
 * @see Command#addArgument(Argument)
 * @see ArgumentGroup
 *
 * @param <Type> the ArgumentType subclass that will parse the value passed to the argument
 * @param <TInner> the actual type of the value passed to the argument
 */
public class Argument<Type extends ArgumentType<TInner>, TInner>
	implements MinimumErrorLevelConfig<CustomError>, ErrorCallbacks<TInner, Argument<Type, TInner>>, Resettable,
	ParentCommandGetter, NamedWithDescription
{
	/** The type of this argument. This is the subParser that will be used to
	 * parse the value/s this argument should receive. */
	public final @NotNull Type argType;
	private PrefixChar prefixChar = PrefixChar.MINUS;
	private final @NotNull List<@NotNull String> names = new ArrayList<>();
	private @Nullable String description;
	private boolean obligatory = false, positional = false, allowUnique = false;
	private @Nullable TInner defaultValue;

	/** The Command that this Argument belongs to. This should never be null after initialization. */
	private Command parentCmd;

	/** The ArgumentGroup that this Argument belongs to. If this Argument does not belong to any group, this
	 * may be null. */
	private @Nullable ArgumentGroup parentGroup;

	// callbacks for error handling
	private @Nullable Consumer<@NotNull Argument<Type, TInner>> onErrorCallback;
	private @Nullable Consumer<@NotNull TInner> onCorrectCallback;

	/** The color that this Argument will have in places where it is displayed, such as the help message.
	 * By default, the color will be picked from the {@link Command#colorsPool} of the parent command at
	 * {@link Argument#setParentCmd(Command)}. */
	private final @NotNull ModifyRecord<Color> representationColor = new ModifyRecord<>(null);


	/** The list of prefixes that can be used. */
	public enum PrefixChar {
		MINUS('-'),
		PLUS('+'),
		SLASH('/'),
		AT('@'),
		PERCENT('%'),
		CARET('^'),
		EXCLAMATION('!'),
		TILDE('~'),
		QUESTION('?'),
		EQUALS('='),
		COLON(':');

		public final char character;

		PrefixChar(char character) {
			this.character = character;
		}
	}


	/** Creates an argument with the specified type and names. */
	public Argument(@NotNull Type argType, @NotNull String... names) {
		this.addNames(names);
		this.argType = argType;
	}

	/** Creates an argument with the specified name and type. */
	public Argument(@NotNull String name, @NotNull Type argType) {
		this(argType, name);
	}

	/**
	 * Creates an argument with a {@link BooleanArgument} type.
	 */
	public static Argument<BooleanArgument, Boolean> create(@NotNull String... names) {
		return new Argument<>(ArgumentType.BOOLEAN(), names);
	}

	/** Creates an argument with the specified name and type. */
	public static <Type extends ArgumentType<TInner>, TInner>
	Argument<Type, TInner> create(@NotNull String name, @NotNull Type argType) {
		return new Argument<>(argType, name);
	}

	/** Creates an argument with the specified type and names. */
	public static <Type extends ArgumentType<TInner>, TInner>
	Argument<Type, TInner> create(@NotNull Type argType, @NotNull String... names) {
		return new Argument<>(argType, names);
	}

	/** Creates an argument with the specified single character name and type. */
	public static <Type extends ArgumentType<TInner>, TInner>
	Argument<Type, TInner> create(char name, @NotNull Type argType) {
		return new Argument<>(argType, String.valueOf(name));
	}

	/** Creates an argument with the specified single character name, full name and type. */
	public static <Type extends ArgumentType<TInner>, TInner>
	Argument<Type, TInner> create(char charName, @NotNull String fullName, @NotNull Type argType) {
		return new Argument<>(argType, String.valueOf(charName), fullName);
	}


	/**
	 * Marks the argument as obligatory. This means that this argument should <b>always</b> be used
	 * by the user.
	 */
	public Argument<Type, TInner> obligatory() {
		this.obligatory = true;
		return this;
	}

	public boolean isObligatory() {
		return this.obligatory;
	}

	/**
	 * Marks the argument as positional. This means that the value of this argument may be specified directly
	 * without indicating a name of this argument. The positional place where it should be placed is
	 * defined by the order of creation of the argument definitions.
	 * <ul>
	 *    <li>Note that an argument marked as positional can still be used by specifying a name.
	 * </ul>
	 */
	public Argument<Type, TInner> positional() {
		if (this.argType.getArgValueCount().max == 0) {
			throw new IllegalArgumentException("An argument that does not accept values cannot be positional");
		}
		this.positional = true;
		return this;
	}

	public boolean isPositional() {
		return this.positional;
	}

	/**
	 * Specify the prefix of this argument. By default, this is {@link PrefixChar#MINUS}. If this argument is used in an
	 * argument name list (-abc), the prefix that will be valid is any against all the arguments specified
	 * in that name list.
	 * @see PrefixChar
	 */
	public Argument<Type, TInner> prefix(PrefixChar prefixChar) {
		this.prefixChar = prefixChar;
		return this;
	}

	public PrefixChar getPrefix() {
		return this.prefixChar;
	}

	/**
	 * Specifies that this argument has priority over other arguments, even if they are obligatory.
	 * This means that if an argument in a command is set as obligatory, but one argument with {@link #allowUnique}
	 * was used, then the unused obligatory argument will not throw an error.
	 */
	public Argument<Type, TInner> allowUnique() {
		this.allowUnique = true;
		return this;
	}

	public boolean isUniqueAllowed() {
		return this.allowUnique;
	}

	/**
	 * The value that should be used if the user does not specify a value for this argument. If the argument
	 * does not accept values, this value will be ignored.
	 */
	public Argument<Type, TInner> defaultValue(@NotNull TInner value) {
		this.defaultValue = value;
		return this;
	}

	/**
	 * Add more names to this argument. This is useful if you want the same argument to be used with multiple
	 * different names.
	 * <hr><hr>
	 * <p>
	 *     Single character names can be used in argument name lists (e.g. <code>-abc</code>), each alphabetic
	 * 	   character being an argument name, that is, <code>-a -b -c</code>.
	 * </p>
	 */
	public Argument<Type, TInner> addNames(@NotNull String... names) {
		Arrays.stream(names)
			.map(UtlString::sanitizeName)
			.forEach(newName -> {
				if (this.names.contains(newName)) {
					throw new IllegalArgumentException("Name '" + newName + "' is already used by this argument.");
				}
				this.names.add(newName);
			});
		return this;
	}

	public boolean hasName(String name) {
		return this.names.contains(name);
	}

	public @NotNull List<@NotNull String> getNames() {
		return Collections.unmodifiableList(this.names);
	}

	/** Sets the description of this argument. This description will be shown in the help message. */
	public Argument<Type, TInner> description(@NotNull String description) {
		this.description = description;
		return this;
	}

	/** Returns the name of this argument. If multiple names are defined, the longest name will be returned. */
	@Override
	public @NotNull String getName() {
		if (this.names.size() == 1)
			return this.names.get(0);

		return new ArrayList<>(this.getNames()) {{
			this.sort((a, b) -> b.length() - a.length());
		}}.get(0);
	}

	@Override
	public @Nullable String getDescription() {
		return this.description;
	}

	/** Sets the parent command of this argument. This is called when adding the Argument to a command at
	 * {@link Command#addArgument(Argument)} */
	void setParentCmd(@NotNull Command parentCmd) {
		if (this.parentCmd != null) {
			throw new IllegalStateException("Argument already added to a command");
		}
		this.parentCmd = parentCmd;
		this.representationColor.setIfNotModified(parentCmd.colorsPool.next());
	}

	@Override
	public @NotNull Command getParentCommand() {
		return this.parentCmd;
	}

	/** Sets the parent group of this argument. This is called when adding the Argument to a group at
	 * {@link ArgumentGroup#addArgument(Argument)} */
	void setParentGroup(@NotNull ArgumentGroup parentGroup) {
		if (this.parentGroup != null) {
			throw new IllegalStateException("Argument already added to a group");
		}
		this.parentGroup = parentGroup;
	}

	public @Nullable ArgumentGroup getParentGroup() {
		return this.parentGroup;
	}

	public short getUsageCount() {
		return this.argType.usageCount;
	}

	public @NotNull Color getRepresentationColor() {
		return this.representationColor.get();
	}

	public void setRepresentationColor(@NotNull Color color) {
		this.representationColor.set(color);
	}


	public boolean isHelpArgument() {
		return this.getName().equals("help") && this.isUniqueAllowed();
	}

	/**
	 * Specify a function that will be called with the value introduced by the user.
	 */
	public Argument<Type, TInner> onOk(@NotNull Consumer<@NotNull TInner> callback) {
		this.setOnCorrectCallback(callback);
		return this;
	}

	/**
	 * Specify a function that will be called if an error occurs when parsing this argument.
	 */
	public Argument<Type, TInner> onErr(@NotNull Consumer<@NotNull Argument<Type, TInner>> callback) {
		this.setOnErrorCallback(callback);
		return this;
	}

	/**
	 * Pass the specified values array to the argument type to parse it.
	 *
	 * @param tokenIndex This is the global index of the token that is currently being parsed. Used when
	 * dispatching errors.
	 */
	public void parseValues(@NotNull String @NotNull [] values, short tokenIndex) {
		// check if the parent group of this argument is exclusive, and if so, check if any other argument in it has been used
		if (this.parentGroup != null) {
			var exclusivityResult = this.parentGroup.checkExclusivity();
			if (exclusivityResult != null) {
				this.parentCmd.getParser().addError(
					new ParseError(
						ParseError.ParseErrorType.MULTIPLE_ARGS_IN_EXCLUSIVE_GROUP_USED,
						this.parentCmd.getParser().getCurrentTokenIndex(),
						this, values.length
					)
					{{
						this.setArgumentGroup(exclusivityResult);
					}}
				);
				return;
			}
		}

		this.argType.setTokenIndex(tokenIndex);
		this.argType.parseAndUpdateValue(values);
		this.argType.usageCount++;
		if (this.parentGroup != null) {
			this.parentGroup.setArgUsed();
		}
	}

	/**
	 * {@link #parseValues(String[], short)} but passes in an empty values array to parse.
	 */
	public void parseValues() {
		this.parseValues(new String[0], (short)0);
	}

	/**
	 * Returns the final parsed value of this argument.
	 */
	public @Nullable TInner finishParsing() {
		if (this.getUsageCount() == 0) {
			if (this.obligatory && !this.parentCmd.uniqueArgumentReceivedValue()) {
				this.parentCmd.getParser().addError(ParseError.ParseErrorType.OBLIGATORY_ARGUMENT_NOT_USED, this, 0);
				return null;
			}

			// if the argument type has a value defined (even if it wasn't used), use that. Otherwise, use the default value
			TInner value = this.argType.getValue();
			return value == null ? this.defaultValue : value;
		}

		this.argType.getErrorsUnderDisplayLevel().forEach(this.parentCmd.getParser()::addError);
		return this.argType.getFinalValue();
	}

	/**
	 * Checks if this argument matches the given name, including the prefix.
	 */
	public boolean checkMatch(@NotNull String name) {
		return this.names.stream()
			.anyMatch(a -> name.equals(Character.toString(this.prefixChar.character).repeat(2) + a));
	}

	/**
	 * Checks if this argument matches the given single character name.
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
			|| (!this.allowUnique && this.parentCmd.uniqueArgumentReceivedValue())
		) return;

		this.onCorrectCallback.accept((@NotNull TInner)okValue);
	}

	public boolean equals(@NotNull Argument<?, ?> obj) {
		// we just want to check if there's a difference between identifiers and both are part of the same command
		return this.parentCmd == obj.parentCmd || (
			this.getNames().stream().anyMatch(name -> {
				for (var otherName : obj.getNames()) {
					if (name.equals(otherName)) return true;
				}
				return false;
			})
		);
	}

	/**
	 * Compares two arguments by the synopsis view priority order.
	 * <p>
	 * <b>Order:</b>
	 * Positional > Obligatory > Optional.
	 * </p>
	 *
	 * @return 0 if both arguments are equal, -1 if the first argument
	 * goes before the second, 1 if the second goes before the first.
	 */
	public static int compareByPriority(@NotNull Argument<?, ?> first, @NotNull Argument<?, ?> second) {
		if (first.isPositional() && !second.isPositional()) {
			return -1;
		} else if (!first.isPositional() && second.isPositional()) {
			return 1;
		} else if (first.isObligatory() && !second.isObligatory()) {
			return -1;
		} else if (!first.isObligatory() && second.isObligatory()) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * Sorts the given array of arguments by the synopsis view priority order.
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
				this.argType.getClass().getSimpleName(), this.names, this.prefixChar.character, this.obligatory,
				this.positional, this.allowUnique, this.defaultValue
			);
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
	public void setOnErrorCallback(@NotNull Consumer<@NotNull Argument<Type, TInner>> callback) {
		this.onErrorCallback = callback;
	}

	@Override
	public void setOnCorrectCallback(@NotNull Consumer<@NotNull TInner> callback) {
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


interface ArgumentAdder {
	/**
	 * Inserts an argument for this command to be parsed.
	 *
	 * @param argument the argument to be inserted
	 */
	<T extends ArgumentType<TInner>, TInner> void addArgument(@NotNull Argument<T, TInner> argument);

	@NotNull List<@NotNull Argument<?, ?>> getArguments();
}