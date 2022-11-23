package argparser;

import argparser.argumentTypes.*;
import argparser.utils.ErrorLevel;
import argparser.utils.ErrorsContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class ArgumentType<T> extends ErrorsContainer<CustomError, ArgumentType<T>, ArgumentType<T>> {
	protected T value;
	/**
	 * This is the current index of the value that is being parsed.
	 */
	protected int currentArgValueIndex = 0;
	/**
	 * This is used for storing errors that occur during parsing. We need to keep track of the index of
	 * the token that caused the error. -1 means that this was still not parsed.
	 */
	private short tokenIndex = -1;
	/**
	 * This specifies the number of values that this argument received when being parsed.
	 */
	private int receivedValueCount = 0;
	/**
	 * The parent argument type is the one that wants to listen for errors that occur in this argument type.
	 * This value is set by the parent argument type when it runs the register method.
	 *
	 * @see ArgumentType#registerSubType(ArgumentType)
	 */
	private ArgumentType<?> parentArgType;

	final void parseArgumentValues(String[] args) {
		this.receivedValueCount = args.length;
		this.parseValues(args);
	}

	public abstract void parseValues(String[] args);

	public void parseValues(String arg) {
		this.parseValues(new String[]{arg});
	}

	/**
	 * By registering a subtype, this allows you to listen for errors that occurred in this subtype during
	 * parsing. The <code>onSubTypeError</code> method will be called when an error occurs.
	 *
	 * @see ArgumentType#onSubTypeError(CustomError)
	 */
	protected void registerSubType(ArgumentType<?> subType) {
		subType.tokenIndex = 0; // This is so the subtype will not throw the error that it was not parsed.
		subType.parentArgType = this;
	}

	/**
	 * This is called when a subtype of this argument type has an error.
	 * By default, this adds the error to the list of errors, while also adding
	 * the current value index.
	 *
	 * @param error The error that occurred in the subtype.
	 * @see ArgumentType#currentArgValueIndex
	 */
	protected void onSubTypeError(CustomError error) {
		error.index += this.currentArgValueIndex;
		this.addError(error);
	}

	private void dispatchErrorToParent(CustomError error) {
		if (this.parentArgType != null) {
			this.parentArgType.onSubTypeError(error);
		}
	}

	/**
	 * Specifies the number of values that this argument should receive when being parsed.
	 */
	public ArgValueCount getNumberOfArgValues() {
		return ArgValueCount.ONE;
	}

	public String getRepresentation() {
		return this.getClass().getName();
	}

	public T getFinalValue() {
		return this.value;
	}

	/**
	 * Adds an error to the list of errors that occurred during parsing.
	 *
	 * @param message The message to display related to the error.
	 */
	protected void addError(String message) {
		this.addError(message, this.currentArgValueIndex, ErrorLevel.ERROR);
	}

	/**
	 * Adds an error to the list of errors that occurred during parsing.
	 *
	 * @param message The message to display related to the error.
	 * @param index The index of the value that caused the error.
	 */
	protected void addError(String message, int index) {
		this.addError(message, index, ErrorLevel.ERROR);
	}

	/**
	 * Adds an error to the list of errors that occurred during parsing.
	 *
	 * @param message The message to display related to the error.
	 * @param level The level of the error.
	 */
	protected void addError(String message, ErrorLevel level) {
		this.addError(message, this.currentArgValueIndex, level);
	}

	/**
	 * Adds an error to the list of errors that occurred during parsing.
	 *
	 * @param message The message to display related to the error.
	 * @param index The index of the value that caused the error.
	 * @param level The level of the error.
	 */
	protected void addError(String message, int index, ErrorLevel level) {
		if (!this.getNumberOfArgValues().isInRange(index, true)) {
			throw new IndexOutOfBoundsException("Index " + index + " is out of range for " + this.getClass().getName());
		}

		if (this.tokenIndex == -1) {
			throw new IllegalStateException("Cannot add an error to an argument that has not been parsed yet.");
		}

		var error = new CustomError(
			message,
			this.tokenIndex + Math.min(index + 1, this.receivedValueCount),
			level
		);

		super.addError(error);
		this.dispatchErrorToParent(error);
	}

	@Override
	public void addError(CustomError error) {
		if (!this.getNumberOfArgValues().isInRange(error.index, true)) {
			throw new IndexOutOfBoundsException("Index " + error.index + " is out of range for " + this.getClass().getName());
		}

		error.index = this.tokenIndex + Math.min(error.index + 1, this.receivedValueCount);

		super.addError(error);
		this.dispatchErrorToParent(error);
	}

	protected short getTokenIndex() {
		return tokenIndex;
	}

	void setTokenIndex(short tokenIndex) {
		this.tokenIndex = tokenIndex;
	}

	/**
	 * Iterates over the values that this argument received when being parsed. This also sets
	 * <code>this.currentArgValueIndex</code> to the current index of the value.
	 *
	 * @param args The values that this argument received when being parsed.
	 * @param consumer The consumer that will be called for each value.
	 */
	protected void forEachArgValue(String[] args, Consumer<String> consumer) {
		for (int i = 0; i < args.length; i++) {
			this.currentArgValueIndex = i;
			consumer.accept(args[i]);
		}
	}

	// Easy to access values. These are methods because we don't want to use the same instance everywhere.
	public static IntArgument INTEGER() {return new IntArgument();}

	public static BooleanArgument BOOLEAN() {return new BooleanArgument();}

	public static CounterArgument COUNTER() {return new CounterArgument();}

	public static StringArgument STRING() {return new StringArgument();}

	public static FileArgument FILE() {return new FileArgument();}

	public static <T extends ArgumentType<Ts>, Ts> KeyValuesArgument<T, Ts>
	KEY_VALUES(T valueType) {return new KeyValuesArgument<>(valueType);}
}


