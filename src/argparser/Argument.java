package argparser;

import argparser.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Argument<Type extends ArgumentType<TInner>, TInner>
	implements IMinimumErrorLevelConfig<CustomError>, IErrorCallbacks<TInner, Argument<Type, TInner>>
{
	final Type argType;
	private char prefix = '-';
	private final List<String> names = new ArrayList<>();
	private short usageCount = 0;
	private boolean obligatory = false, positional = false, allowUnique = false;
	private TInner defaultValue;
	private Command parentCmd;
	private Consumer<Argument<Type, TInner>> onErrorCallback;
	private Consumer<TInner> onCorrectCallback;


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
	 * Creates an argument of type {@link argparser.argumentTypes.BooleanArgument} with the given name.
	 */
	@SuppressWarnings("unchecked cast") // we know for sure type returned by BOOLEAN is compatible
	public Argument(String name) {this(name, (Type)ArgumentType.BOOLEAN());}


	public Argument<Type, TInner> addNames(String... names) {
		Objects.requireNonNull(names);

		Arrays.stream(names)
			.map(UtlString::sanitizeName)
			.forEach(this.names::add);
		return this;
	}

	public boolean hasName(String name) {
		return this.names.contains(name);
	}

	public String getDisplayName() {
		return this.names.get(0);
	}

	public List<String> getNames() {
		return names;
	}

	public char getPrefix() {
		return prefix;
	}

	/**
	 * Marks the argument as obligatory. This means that this argument should <b>always</b> be used
	 * by the user.
	 */
	public Argument<Type, TInner> obligatory() {
		this.obligatory = true;
		return this;
	}

	/**
	 * Marks the argument as positional. This means that the value of this argument may be specified directly
	 * without indicating the name/name of this argument. The positional place where it should be placed is
	 * defined by the order of creation of the argument definitions.
	 * <li>Note that an argument marked as positional can still be used by specifying its name/name.
	 */
	public Argument<Type, TInner> positional() {
		if (this.getNumberOfValues().max == 0) {
			throw new IllegalArgumentException("An argument that does not accept values cannot be positional");
		}
		this.positional = true;
		return this;
	}

	/**
	 * Specify the prefix of this argument. By default, this is <code>'-'</code>. If this argument is used in an
	 * argument name list (-abcd), the prefix that will be valid is any against all the arguments specified
	 * in that name list.
	 */
	public Argument<Type, TInner> prefix(char prefix) {
		this.prefix = prefix;
		return this;
	}

	public Argument<Type, TInner> allowUnique() {
		this.allowUnique = true;
		return this;
	}

	/**
	 * The value that should be used if the user does not specify a value for this argument. If the argument
	 * does not accept values, this value will be ignored.
	 */
	public Argument<Type, TInner> defaultValue(TInner value) {
		this.defaultValue = Objects.requireNonNull(value);
		return this;
	}

	public Argument<Type, TInner> onOk(Consumer<TInner> callback) {
		this.setOnCorrectCallback(callback);
		return this;
	}

	public Argument<Type, TInner> onErr(Consumer<Argument<Type, TInner>> callback) {
		this.setOnErrorCallback(callback);
		return this;
	}

	TInner finishParsing(Command.ParsingState parseState) {
		if (this.usageCount == 0) {
			if (this.obligatory && !this.parentCmd.uniqueArgumentReceivedValue()) {
				parseState.addError(ParseError.ParseErrorType.OBLIGATORY_ARGUMENT_NOT_USED, this, 0);
				return null;
			}
			return this.defaultValue;
		}

		this.argType.getErrorsUnderDisplayLevel().forEach(parseState::addError);
		return this.argType.getFinalValue();
	}

	public void parseValues(String[] value, short tokenIndex) {
		this.argType.setTokenIndex(tokenIndex);
		this.argType.parseArgumentValues(value);
		this.usageCount++;
	}

	public void parseValues() {
		this.parseValues(new String[0], (short)0);
	}

	public ArgValueCount getNumberOfValues() {
		return this.argType.getNumberOfArgValues();
	}

	/**
	 * Checks if this argument matches the given name, including the prefix.
	 *
	 */
	boolean checkMatch(String name) {
		return this.names.stream().anyMatch(a -> name.equals(Character.toString(this.prefix).repeat(2) + a));
	}

	boolean checkMatch(char name) {
		return this.hasName(Character.toString(name));
	}

	public boolean isObligatory() {
		return obligatory;
	}

	public boolean isPositional() {
		return positional;
	}

	public boolean allowsUnique() {
		return allowUnique;
	}

	Command getParentCmd() {
		return parentCmd;
	}

	void setParentCmd(Command parentCmd) {
		if (this.parentCmd != null) {
			throw new IllegalStateException("Argument already added to a command");
		}
		this.parentCmd = parentCmd;
	}

	public boolean equals(Argument<?, ?> obj) {
		// we just want to check if there's a difference between identifiers and both are part of the same command
		return this.parentCmd == obj.parentCmd && (
			this.getNames().stream().anyMatch(name -> obj.getNames().contains(name))
		);
	}

	public short getUsageCount() {
		return usageCount;
	}

	// --------------------------------- just act as a proxy to the type error handling ---------------------------------
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
	public void setOnErrorCallback(Consumer<Argument<Type, TInner>> callback) {
		this.onErrorCallback = callback;
	}



	/**
	 * Specify a function that will be called with the value introduced by the user. This function is only
	 * called if the user used the argument, so it will never be called with a default value, for example.
	 */
	@Override
	public void setOnCorrectCallback(Consumer<TInner> callback) {
		this.onCorrectCallback = callback;
	}

	@Override
	public void invokeCallbacks() {
		if (this.onErrorCallback == null || this.hasExitErrors()) return;
		this.onErrorCallback.accept(this);
	}

	// no worries about casting here, it will always receive the correct type
	@SuppressWarnings("unchecked")
	void invokeCallbacks(Object okValue) {
		this.invokeCallbacks();
		if (this.onCorrectCallback == null || this.usageCount == 0) return;
		this.onCorrectCallback.accept((TInner)okValue);
	}

	@Override
	public String toString() {
		return String.format(
			"Argument<%s>[names=%s, prefix='%c', obligatory=%b, positional=%b]",
			this.argType.getClass().getSimpleName(), this.getNames(),
			this.getPrefix(), this.isObligatory(), this.isPositional()
		);
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
}