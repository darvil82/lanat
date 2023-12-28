package lanat;

import lanat.argumentTypes.FromParseableArgumentType;
import lanat.argumentTypes.IntegerArgumentType;
import lanat.argumentTypes.Parseable;
import lanat.parsing.errors.CustomErrorImpl;
import lanat.parsing.errors.Error;
import lanat.utils.ErrorsContainerImpl;
import lanat.utils.Resettable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.Pair;
import utils.Range;

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
 * <p>
 * It is possible to use other Argument Types inside your custom Argument Type. This is done by using the
 * {@link ArgumentType#registerSubType(ArgumentType)} method. This allows you to listen for errors that occur in the
 * subtypes, and to add them to the list of errors of the main parser. {@link ArgumentType#onSubTypeError(Error.CustomError)}
 * is called when an error occurs in a subtype.
 * </p>
 * <p>
 * You can also implement {@link Parseable} to create a basic argument type implementation. Note that in order to
 * use that implementation, you need to wrap it in a {@link FromParseableArgumentType} instance (which provides the
 * necessary internal functionality).
 * </p>
 * @param <T> The type of the value that this argument type parses.
 */
public abstract class ArgumentType<T>
	extends ErrorsContainerImpl<Error.CustomError>
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
	 * caused the error.
	 */
	private int lastTokenIndex = 0;

	/**
	 * This specifies the number of values that this argument received when being parsed.
	 */
	private int lastReceivedValuesNum = 0;

	/** This specifies whether the last value that this argument received was in a tuple. */
	private boolean lastInTuple = false;

	/** This specifies the number of times this argument type has been used during parsing. */
	short usageCount = 0;

	/**
	 * The parent argument type is the one that wants to listen for errors that occur in this argument type. This value
	 * is set by the parent argument type when it runs {@link ArgumentType#registerSubType(ArgumentType)}.
	 */
	private @Nullable ArgumentType<?> parentArgType;
	private final @NotNull ArrayList<@NotNull ArgumentType<?>> subTypes = new ArrayList<>();


	/**
	 * Constructs a new argument type with the specified initial value.
	 * @param initialValue The initial value of this argument type.
	 */
	public ArgumentType(@NotNull T initialValue) {
		this();
		this.setValue(this.initialValue = initialValue);
	}

	/**
	 * Constructs a new argument type.
	 */
	public ArgumentType() {
		if (this.getRequiredUsageCount().start() == 0) {
			throw new IllegalArgumentException("The required usage count must be at least 1.");
		}
	}

	/**
	 * Saves the specified tokenIndex and the number of values received, and then parses the values.
	 * @param tokenIndex The index of the token that caused the parsing of this argument type.
	 * @param inTuple Whether the values were received in a tuple.
	 * @param values The values to parse.
	 */
	public final void parseAndUpdateValue(int tokenIndex, boolean inTuple, @NotNull String... values) {
		this.usageCount++;
		this.lastTokenIndex = tokenIndex;
		this.lastInTuple = inTuple;
		this.lastReceivedValuesNum = values.length;
		this.currentValue = this.parseValues(values);
	}

	/**
	 * By registering a subtype, this allows you to listen for errors that occurred in this subtype during parsing. The
	 * {@link ArgumentType#onSubTypeError(Error.CustomError)} method will be called when an error occurs.
	 */
	protected final void registerSubType(@NotNull ArgumentType<?> subType) {
		if (subType.parentArgType != null) {
			throw new IllegalArgumentException("The argument type specified is already registered to an argument type.");
		}

		subType.parentArgType = this;
		this.subTypes.add(subType);
	}

	/**
	 * This is called when a subtype of this argument type has an error. By default, this adds the error to the list of
	 * errors, while also adding the {@link ArgumentType#currentArgValueIndex} to the error's token index.
	 *
	 * @param error The error that occurred in the subtype.
	 */
	protected void onSubTypeError(@NotNull Error.CustomError error) {
		error.offsetIndex(this.currentArgValueIndex);
		this.addError(error);
	}

	/**
	 * Dispatches the error to the parent argument type.
	 * @param error The error to dispatch.
	 */
	private void dispatchErrorToParent(@NotNull Error.CustomError error) {
		if (this.parentArgType != null) {
			this.parentArgType.onSubTypeError(error);
		}
	}

	/**
	 * Returns the current value of this argument type.
	 * @return The current value of this argument type.
	 */
	public T getValue() {
		return this.currentValue;
	}

	/**
	 * Returns the final value of this argument type. This is the value that this argument type will have after parsing
	 * is done.
	 * @return The final value of this argument type.
	 */
	public T getFinalValue() {
		return this.getValue(); // by default, the final value is just the current value. subclasses can override this.
	}

	/**
	 * Sets the current value of this argument type.
	 */
	protected void setValue(@NotNull T value) {
		this.currentValue = value;
	}

	/**
	 * Returns the initial value of this argument type, if specified.
	 * @return The initial value of this argument type, {@code null} if not specified.
	 */
	public T getInitialValue() {
		return this.initialValue;
	}

	@Override
	public @NotNull Range getRequiredArgValueCount() {
		return Range.ONE;
	}

	/**
	 * Specifies the number of times this argument type can be used during parsing.
	 * By default, this is 1. ({@link Range#ONE}).
	 * <p>
	 * <strong>Note: </strong> The minimum value must be at least 1.
	 * </p>
	 */
	public @NotNull Range getRequiredUsageCount() {
		return Range.ONE;
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
		this.addError(new CustomErrorImpl(message, level, index));
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <strong>Note: </strong> The error is modified to have the correct token index before being added to the list of
	 * errors.
	 * </p>
	 */
	@Override
	public void addError(@NotNull Error.CustomError error) {
		// the index of the error should never be less than 0 or greater than the max value count
		if (error.getIndex() < 0 || error.getIndex() >= this.getRequiredArgValueCount().end()) {
			throw new IndexOutOfBoundsException("Index " + error.getIndex() + " is out of range for " + this.getClass().getName());
		}

		// the index of the error should be relative to the last token index.
		// if this is a subtype, lastTokenIndex will be 0, so nothing will be done here.
		// proper offsetting will be done when the error is dispatched to the parent.
		error.offsetIndex(this.lastTokenIndex);

		if (this.parentArgType != null) {
			this.dispatchErrorToParent(error);
			return;
		}

		super.addError(error);
	}

	/**
	 * Returns the index of the last token that was parsed.
	 */
	protected int getLastTokenIndex() {
		return this.lastTokenIndex;
	}

	/**
	 * Returns the number of values that this argument received when being parsed the last time.
	 */
	int getLastReceivedValuesNum() {
		return this.lastReceivedValuesNum;
	}

	@NotNull Pair<Integer, Integer> getLastTokensIndicesPair() {
		int inTupleOffset = this.lastInTuple ? 1 : 0;

		return new Pair<>(
			this.lastTokenIndex - 1 - inTupleOffset,
			this.lastReceivedValuesNum + inTupleOffset*2
		);
	}

	/**
	 * Iterates over the values that this argument received when being parsed. This also sets
	 * {@code this.currentArgValueIndex} to the current index of the value.
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
		super.resetState();

		this.currentValue = this.initialValue;
		this.lastTokenIndex = 0;
		this.currentArgValueIndex = 0;
		this.lastReceivedValuesNum = 0;
		this.usageCount = 0;
		this.lastInTuple = false;

		// reset the state of the subtypes.
		this.subTypes.forEach(ArgumentType::resetState);
	}

	@Override
	public @Nullable ArgumentType<?> getParent() {
		return this.parentArgType;
	}
}