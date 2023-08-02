package lanat;

import lanat.argumentTypes.*;
import lanat.parsing.errors.CustomError;
import lanat.utils.ErrorsContainerImpl;
import lanat.utils.Range;
import lanat.utils.Resettable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * <h2>Argument Type</h2>
 * <p>
 * An Argument Type is a handler in charge of parsing a specific kind of input from the command line. For example, the
 * {@link IntegerArgumentType} is in charge of parsing integers from the command line.
 * </p>
 * <h3>Creating custom Argument Types</h3>
 * <p>
 * Creating new Argument Types is an easy task. Extending this class already provides you with most of the functionality
 * that you need. The minimum method that should be implemented is the {@link ArgumentType#parseValues(String[])} method.
 * Which will be called by the main parser when it needs to parse the values of an argument of this type.
 * </p>
 * The custom Argument Type can push errors to the main parser by using the {@link ArgumentType#addError(String)} method
 * and its overloads.
 * @param <T> The type of the value that this argument type parses.
 */
public abstract class ArgumentType<T>
	extends ErrorsContainerImpl<CustomError>
	implements Resettable, Parseable<T>, ParentElementGetter<ArgumentType<?>>
{
	/** This is the value that this argument type current has while being parsed. */
	private T currentValue;

	/**
	 * This is the value that this argument type had before being parsed. This is used for resetting the
	 * {@link ArgumentType#currentValue} to its initial value.
	 */
	private T initialValue;

	/**
	 * This is the current index of the value that is being parsed.
	 */
	private int currentArgValueIndex = 0;

	/**
	 * This is used for storing errors that occur during parsing. We need to keep track of the index of the token that
	 * caused the error. -1 means that this was still not parsed.
	 */
	private short lastTokenIndex = -1;

	/**
	 * This specifies the number of values that this argument received when being parsed.
	 */
	private int lastReceivedValueCount = 0;

	/** This specifies the number of times this argument type has been used during parsing. */
	short usageCount = 0;

	/**
	 * The parent argument type is the one that wants to listen for errors that occur in this argument type. This value
	 * is set by the parent argument type when it runs {@link ArgumentType#registerSubType(ArgumentType)}.
	 */
	private @Nullable ArgumentType<?> parentArgType;
	private final @NotNull ArrayList<@NotNull ArgumentType<?>> subTypes = new ArrayList<>();

	public ArgumentType(@NotNull T initialValue) {
		this();
		this.setValue(this.initialValue = initialValue);
	}

	public ArgumentType() {
		if (this.getRequiredUsageCount().min() == 0) {
			throw new IllegalArgumentException("The required usage count must be at least 1.");
		}
	}

	public final void parseAndUpdateValue(@NotNull String @NotNull [] args) {
		this.lastReceivedValueCount = args.length;
		this.currentValue = this.parseValues(args);
	}

	public final void parseAndUpdateValue(@NotNull String arg) {
		this.lastReceivedValueCount = 1;
		this.currentValue = this.parseValues(arg);
	}

	public final @Nullable T parseValues(@NotNull String arg) {
		return this.parseValues(new String[] { arg });
	}


	/**
	 * By registering a subtype, this allows you to listen for errors that occurred in this subtype during parsing. The
	 * {@link ArgumentType#onSubTypeError(CustomError)} method will be called when an error occurs.
	 */
	protected final void registerSubType(@NotNull ArgumentType<?> subType) {
		if (subType.parentArgType == this) {
			throw new IllegalArgumentException("The sub type is already registered to this argument type.");
		}
		subType.lastTokenIndex = 0; // This is so the subtype will not throw the error that it was not parsed.
		subType.parentArgType = this;
		this.subTypes.add(subType);
	}

	/**
	 * This is called when a subtype of this argument type has an error. By default, this adds the error to the list of
	 * errors, while also adding the {@link ArgumentType#currentArgValueIndex} to the error's token index.
	 *
	 * @param error The error that occurred in the subtype.
	 */
	protected void onSubTypeError(@NotNull CustomError error) {
		error.tokenIndex += this.currentArgValueIndex;
		this.addError(error);
	}

	/**
	 * Dispatches the error to the parent argument type.
	 * @param error The error to dispatch.
	 */
	private void dispatchErrorToParent(@NotNull CustomError error) {
		if (this.parentArgType != null) {
			this.parentArgType.onSubTypeError(error);
		}
	}

	public T getValue() {
		return this.currentValue;
	}

	/**
	 * Sets the current value of this argument type.
	 */
	public void setValue(@NotNull T value) {
		this.currentValue = value;
	}

	public T getInitialValue() {
		return this.initialValue;
	}

	@Override
	public @NotNull Range getRequiredArgValueCount() {
		return Range.ONE;
	}

	/**
	 * Specifies the number of times this argument type can be used during parsing.
	 * <p>
	 * <strong>Note: </strong> The minimum value must be at least 1.
	 * </p>
	 */
	public @NotNull Range getRequiredUsageCount() {
		return Range.ONE;
	}

	/**
	 * Returns the final value of this argument type. This is the value that this argument type has after parsing.
	 */
	public @Nullable T getFinalValue() {
		return this.currentValue;
	}

	/**
	 * Adds an error to the list of errors that occurred during parsing at the current token index.
	 * @param message The message to display related to the error.
	 */
	protected void addError(@NotNull String message) {
		this.addError(message, this.currentArgValueIndex, ErrorLevel.ERROR);
	}

	/**
	 * Adds an error to the list of errors that occurred during parsing.
	 * @param message The message to display related to the error.
	 * @param index The index of the value that caused the error.
	 */
	protected void addError(@NotNull String message, int index) {
		this.addError(message, index, ErrorLevel.ERROR);
	}

	/**
	 * Adds an error to the list of errors that occurred during parsing.
	 * @param message The message to display related to the error.
	 * @param level The level of the error.
	 */
	protected void addError(@NotNull String message, @NotNull ErrorLevel level) {
		this.addError(message, this.currentArgValueIndex, level);
	}

	/**
	 * Adds an error to the list of errors that occurred during parsing.
	 * @param message The message to display related to the error.
	 * @param index The index of the value that caused the error.
	 * @param level The level of the error.
	 */
	protected void addError(@NotNull String message, int index, @NotNull ErrorLevel level) {
		this.addError(new CustomError(message, index, level));
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <strong>Note: </strong> The error is modified to have the correct token index before being added to the list of
	 * errors.
	 * </p>
	 */
	@Override
	public void addError(@NotNull CustomError error) {
		if (this.lastTokenIndex == -1) {
			throw new IllegalStateException("Cannot add an error to an argument that has not been parsed yet.");
		}

		// the index of the error should never be less than 0 or greater than the max value count
		if (error.tokenIndex < 0 || error.tokenIndex >= this.getRequiredArgValueCount().max()) {
			throw new IndexOutOfBoundsException("Index " + error.tokenIndex + " is out of range for " + this.getClass().getName());
		}

		// the index of the error should be relative to the last token index
		error.tokenIndex = this.lastTokenIndex + Math.min(error.tokenIndex + 1, this.lastReceivedValueCount);

		super.addError(error);
		this.dispatchErrorToParent(error);
	}

	protected short getLastTokenIndex() {
		return this.lastTokenIndex;
	}

	void setLastTokenIndex(short lastTokenIndex) {
		this.lastTokenIndex = lastTokenIndex;
	}

	int getLastReceivedValueCount() {
		return this.lastReceivedValueCount;
	}

	/**
	 * Iterates over the values that this argument received when being parsed. This also sets
	 * <code>this.currentArgValueIndex</code> to the current index of the value.
	 *
	 * @param args The values that this argument received when being parsed.
	 * @param consumer The consumer that will be called for each value.
	 */
	protected final void forEachArgValue(@NotNull String @NotNull [] args, @NotNull Consumer<@NotNull String> consumer) {
		for (int i = 0; i < args.length; i++) {
			this.currentArgValueIndex = i;
			consumer.accept(args[i]);
		}
	}

	@Override
	public void resetState() {
		this.currentValue = this.initialValue;
		this.lastTokenIndex = -1;
		this.currentArgValueIndex = 0;
		this.lastReceivedValueCount = 0;
		this.usageCount = 0;
		this.subTypes.forEach(ArgumentType::resetState);
	}

	@Override
	public @Nullable ArgumentType<?> getParent() {
		return this.parentArgType;
	}

	// Easy to access values. These are methods because we don't want to use the same instance everywhere.
	public static IntegerArgumentType INTEGER() {
		return new IntegerArgumentType();
	}

	public static IntegerRangeArgumentType INTEGER_RANGE(int min, int max) {
		return new IntegerRangeArgumentType(min, max);
	}

	public static FloatArgumentType FLOAT() {
		return new FloatArgumentType();
	}

	public static BooleanArgumentType BOOLEAN() {
		return new BooleanArgumentType();
	}

	public static CounterArgumentType COUNTER() {
		return new CounterArgumentType();
	}

	public static FileArgumentType FILE() {
		return new FileArgumentType();
	}

	public static StringArgumentType STRING() {
		return new StringArgumentType();
	}

	public static MultipleStringsArgumentType STRINGS() {
		return new MultipleStringsArgumentType();
	}

	public static <T extends ArgumentType<Ti>, Ti> KeyValuesArgumentType<T, Ti> KEY_VALUES(T valueType) {
		return new KeyValuesArgumentType<>(valueType);
	}

	public static <T extends Enum<T>> EnumArgumentType<T> ENUM(T enumDefault) {
		return new EnumArgumentType<>(enumDefault);
	}

	public static StdinArgumentType STDIN() {
		return new StdinArgumentType();
	}

	public static <T extends Parseable<Ti>, Ti> FromParseableArgumentType<T, Ti> FROM_PARSEABLE(T parseable) {
		return new FromParseableArgumentType<>(parseable);
	}

	public static <T> TryParseArgumentType<T> TRY_PARSE(Class<T> type) {
		return new TryParseArgumentType<>(type);
	}
}