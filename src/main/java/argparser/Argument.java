package argparser;

import argparser.argumentTypes.BooleanArgument;
import argparser.utils.*;
import argparser.utils.displayFormatter.Color;

import java.util.*;
import java.util.function.Consumer;

public class Argument<Type extends ArgumentType<TInner>, TInner>
	implements MinimumErrorLevelConfig<CustomError>, ErrorCallbacks<TInner, Argument<Type, TInner>>, Resettable,
	ParentCommandGetter
{
	public final Type argType;
	private char prefix = '-';
	private final List<String> names = new ArrayList<>();
	private String description;
	private short usageCount = 0;
	private boolean obligatory = false, positional = false, allowUnique = false;
	private TInner defaultValue;
	private Command parentCmd;
	private ArgumentGroup parentGroup;
	private Consumer<Argument<Type, TInner>> onErrorCallback;
	private Consumer<TInner> onCorrectCallback;
	private final ModifyRecord<Color> representationColor = new ModifyRecord<>(null);


	public Argument(Type argType, String... names) {
		this.addNames(names);
		this.argType = argType;
	}

	public Argument(String name, Type argType) {
		this(argType, name);
	}

	public Argument(String[] name, Type argType) {
		this(argType, name);
	}

	public Argument(char name, Type argType) {
		this(argType, String.valueOf(name));
	}

	public Argument(char charName, String fullName, Type argType) {
		this(argType, String.valueOf(charName), fullName);
	}

	/**
	 * Creates an argument with a {@link BooleanArgument} type.
	 */
	public static Argument<BooleanArgument, Boolean> simple(String name) {
		return new Argument<>(ArgumentType.BOOLEAN(), name);
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
		return obligatory;
	}

	/**
	 * Marks the argument as positional. This means that the value of this argument may be specified directly
	 * without indicating a name of this argument. The positional place where it should be placed is
	 * defined by the order of creation of the argument definitions.
	 * <li>Note that an argument marked as positional can still be used by specifying a name.
	 */
	public Argument<Type, TInner> positional() {
		if (this.argType.getNumberOfArgValues().max == 0) {
			throw new IllegalArgumentException("An argument that does not accept values cannot be positional");
		}
		this.positional = true;
		return this;
	}

	public boolean isPositional() {
		return positional;
	}

	/**
	 * Specify the prefix of this argument. By default, this is <code>'-'</code>. If this argument is used in an
	 * argument name list (-abc), the prefix that will be valid is any against all the arguments specified
	 * in that name list.
	 */
	public Argument<Type, TInner> prefix(char prefix) {
		this.prefix = prefix;
		return this;
	}

	public char getPrefix() {
		return prefix;
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

	public boolean allowsUnique() {
		return allowUnique;
	}

	/**
	 * The value that should be used if the user does not specify a value for this argument. If the argument
	 * does not accept values, this value will be ignored.
	 */
	public Argument<Type, TInner> defaultValue(TInner value) {
		this.defaultValue = Objects.requireNonNull(value);
		return this;
	}

	/**
	 * Add more names to this argument. This is useful if you want the same argument to be used with multiple
	 * different names.
	 */
	public Argument<Type, TInner> addNames(String... names) {
		Objects.requireNonNull(names);

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

	public List<String> getNames() {
		return Collections.unmodifiableList(this.names);
	}

	public String getLongestName() {
		return new ArrayList<>(this.getNames()) {{
			sort((a, b) -> b.length() - a.length());
		}}.get(0);
	}

	public Argument<Type, TInner> description(String description) {
		this.description = description;
		return this;
	}

	public String getDescription() {
		return description;
	}

	void setParentCmd(Command parentCmd) {
		if (this.parentCmd != null) {
			throw new IllegalStateException("Argument already added to a command");
		}
		this.parentCmd = parentCmd;
		this.representationColor.setIfNotModified(parentCmd.colorsPool.next());
	}

	@Override
	public Command getParentCommand() {
		return parentCmd;
	}

	void setParentGroup(ArgumentGroup parentGroup) {
		if (this.parentGroup != null) {
			throw new IllegalStateException("Argument already added to a group");
		}
		this.parentGroup = parentGroup;
	}

	public ArgumentGroup getParentGroup() {
		return parentGroup;
	}

	public short getUsageCount() {
		return usageCount;
	}

	public Color getRepresentationColor() {
		return representationColor.get();
	}

	public void setRepresentationColor(Color color) {
		this.representationColor.set(color);
	}


	public boolean isHelpArgument() {
		return this.getLongestName().equals("help") && this.allowsUnique();
	}

	/**
	 * Specify a function that will be called with the value introduced by the user.
	 */
	public Argument<Type, TInner> onOk(Consumer<TInner> callback) {
		this.setOnCorrectCallback(callback);
		return this;
	}

	/**
	 * Specify a function that will be called if an error occurs when parsing this argument.
	 */
	public Argument<Type, TInner> onErr(Consumer<Argument<Type, TInner>> callback) {
		this.setOnErrorCallback(callback);
		return this;
	}

	/**
	 * Pass the specified values array to the argument type to parse it.
	 *
	 * @param tokenIndex This is the global index of the token that is currently being parsed. Used when
	 * dispatching errors.
	 */
	void parseValues(String[] values, short tokenIndex) {
		// check if the parent group of this argument is exclusive, and if so, check if any other argument in it has been used
		if (this.parentGroup != null) {
			var exclusivityResult = this.parentGroup.checkExclusivity();
			if (exclusivityResult != null) {
				this.parentCmd.parsingState.addError(
					new ParseError(
						ParseError.ParseErrorType.MULTIPLE_ARGS_IN_EXCLUSIVE_GROUP_USED,
						this.parentCmd.parsingState.getCurrentTokenIndex(),
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
		this.argType.parseArgumentValues(values);
		this.usageCount++;
		if (this.parentGroup != null) {
			this.parentGroup.setArgUsed();
		}
	}

	/**
	 * {@link #parseValues(String[], short)} but passes in an empty values array to parse.
	 */
	void parseValues() {
		this.parseValues(new String[0], (short)0);
	}

	/**
	 * Returns the final parsed value of this argument.
	 */
	TInner finishParsing() {
		if (this.usageCount == 0) {
			if (this.obligatory && !this.parentCmd.uniqueArgumentReceivedValue()) {
				this.parentCmd.parsingState.addError(ParseError.ParseErrorType.OBLIGATORY_ARGUMENT_NOT_USED, this, 0);
				return null;
			}

			// if the argument type has a value defined (even if it wasn't used), use that. Otherwise, use the default value
			TInner value = this.argType.getValue();
			return value == null ? this.defaultValue : value;
		}

		this.argType.getErrorsUnderDisplayLevel().forEach(this.parentCmd.parsingState::addError);
		return this.argType.getFinalValue();
	}

	/**
	 * Checks if this argument matches the given name, including the prefix.
	 */
	boolean checkMatch(String name) {
		return this.names.stream().anyMatch(a -> name.equals(Character.toString(this.prefix).repeat(2) + a));
	}

	/**
	 * Checks if this argument matches the given single character name.
	 */
	boolean checkMatch(char name) {
		return this.hasName(Character.toString(name));
	}

	// no worries about casting here, it will always receive the correct type
	@SuppressWarnings("unchecked")
	void invokeCallbacks(Object okValue) {
		if (this.hasExitErrors()) {
			this.invokeCallbacks();
			return;
		}

		if (
			this.onCorrectCallback == null
				|| this.usageCount == 0
				|| (!this.allowUnique && this.parentCmd.uniqueArgumentReceivedValue())
		) return;

		this.onCorrectCallback.accept((TInner)okValue);
	}

	public boolean equals(Argument<?, ?> obj) {
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
	public static int compareByPriority(Argument<?, ?> first, Argument<?, ?> second) {
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
	public static List<Argument<?, ?>> sortByPriority(List<Argument<?, ?>> args) {
		return new ArrayList<>(args) {{
			this.sort(Argument::compareByPriority);
		}};
	}

	@Override
	public void resetState() {
		this.usageCount = 0;
		this.argType.resetState();
	}

	@Override
	public String toString() {
		return String.format(
			"Argument<%s>[names=%s, prefix='%c', obligatory=%b, positional=%b, allowUnique=%b, defaultValue=%s]",
			this.argType.getClass().getSimpleName(), this.names, this.prefix, this.obligatory,
			this.positional, this.allowUnique, this.defaultValue
		);
	}


	// ------------------------------------------------ Error Handling ------------------------------------------------
	// just act as a proxy to the type error handling

	@Override
	public List<CustomError> getErrorsUnderExitLevel() {
		return this.argType.getErrorsUnderExitLevel();
	}

	@Override
	public List<CustomError> getErrorsUnderDisplayLevel() {
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
	public void setMinimumDisplayErrorLevel(ErrorLevel level) {
		this.argType.setMinimumDisplayErrorLevel(level);
	}

	@Override
	public ModifyRecord<ErrorLevel> getMinimumDisplayErrorLevel() {
		return this.argType.getMinimumDisplayErrorLevel();
	}

	@Override
	public void setMinimumExitErrorLevel(ErrorLevel level) {
		this.argType.setMinimumExitErrorLevel(level);
	}

	@Override
	public ModifyRecord<ErrorLevel> getMinimumExitErrorLevel() {
		return this.argType.getMinimumExitErrorLevel();
	}

	@Override
	public void setOnErrorCallback(Consumer<Argument<Type, TInner>> callback) {
		this.onErrorCallback = callback;
	}

	@Override
	public void setOnCorrectCallback(Consumer<TInner> callback) {
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
	 * @param <T> the ArgumentType subclass that will parse the value passed to the argument
	 * @param <TInner> the actual type of the value passed to the argument
	 */
	<T extends ArgumentType<TInner>, TInner> void addArgument(Argument<T, TInner> argument);

	List<Argument<?, ?>> getArguments();
}