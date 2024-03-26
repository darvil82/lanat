package lanat;

import lanat.argumentTypes.FromParseableArgumentType;
import lanat.argumentTypes.IntegerArgumentType;
import lanat.argumentTypes.Parseable;
import lanat.parsing.errors.Error;
import lanat.parsing.errors.handlers.CustomErrorImpl;
import lanat.utils.ParentElementGetter;
import lanat.utils.Resettable;
import lanat.utils.errors.ErrorContainerImpl;
import lanat.utils.errors.ErrorLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.Pair;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

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
 * @param <T> The type of the value that this argument type will parse into.
 */
public abstract class ArgumentType<T>
	extends ErrorContainerImpl<Error.CustomError>
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

	/** A snapshot of the state of the argument type during parsing. */
	public record ParseStateSnapshot(int tokenIndex, int receivedValuesCount, boolean inTuple, boolean positional) {}

	private ParseStateSnapshot lastParseState;

	/** This specifies the number of times this argument type has been used during parsing. */
	int usageCount = 0;

	/**
	 * The parent argument type is the one that wants to listen for errors that occur in this argument type. This value
	 * is set by the parent argument type when it runs {@link ArgumentType#registerSubType(ArgumentType)}.
	 */
	private @Nullable ArgumentType<?> parentArgType;
	private final @NotNull ArrayList<@NotNull ArgumentType<?>> subTypes = new ArrayList<>(0);


	/**
	 * Constructs a new argument type with the specified initial value. Also sets the current value.
	 * @param initialValue The initial value of this argument type.
	 */
	protected ArgumentType(@NotNull T initialValue) {
		this();
		this.initialValue = initialValue;
		this.currentValue = initialValue;
	}

	/**
	 * Constructs a new argument type.
	 */
	protected ArgumentType() {
		ArgumentType.checkValidState(this);
	}

	/**
	 * Constructs a new argument type with the specified parseable.
	 */
	protected ArgumentType(@NotNull Parseable<T> parseable) {
		ArgumentType.checkValidState(parseable);
	}

	/**
	 * Checks if the specified parseable is in a valid state.
	 * @param parseable The parseable to check.
	 */
	private static void checkValidState(@NotNull Parseable<?> parseable) {
		if (parseable.getUsageCountBounds().start() == 0) {
			throw new IllegalArgumentException("The required usage count must be at least 1.");
		}
	}

	/**
	 * Saves the specified tokenIndex and the number of values received, and then parses the values.
	 * @param snapshot The snapshot of this parse operation.
	 * @param values The values to parse.
	 */
	public final void parseAndUpdateValue(@NotNull ArgumentType.ParseStateSnapshot snapshot, @NotNull String... values) {
		this.usageCount++;
		this.lastParseState = snapshot;
		this.currentValue = this.parseValues(values);
	}

	/**
	 * Registers a subtype. This allows you to listen for errors that occurred in this subtype during parsing. The
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
	 * Unregisters the specified subtype from this argument type.
	 */
	protected final void unregisterSubType(@NotNull ArgumentType<?> subType) {
		if (subType.parentArgType != this) {
			throw new IllegalArgumentException("The argument type specified is not registered to this argument type.");
		}

		subType.parentArgType = null;
		this.subTypes.remove(subType);
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
	 * @return {@code true} if the error was dispatched, {@code false} otherwise.
	 */
	private boolean dispatchErrorToParent(@NotNull Error.CustomError error) {
		if (this.parentArgType == null)
			return false;

		this.parentArgType.onSubTypeError(error);
		return true;
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
	 * Returns the initial value of this argument type, if specified.
	 * @return The initial value of this argument type, {@code null} if not specified.
	 */
	public final T getInitialValue() {
		return this.initialValue;
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
		if (error.getIndex() < 0 || error.getIndex() >= this.getValueCountBounds().end()) {
			throw new IndexOutOfBoundsException(
				"Error index " + error.getIndex() + " is out of range for argument type '" + this.getName() + "'."
			);
		}

		if (this.dispatchErrorToParent(error))
			return; // if the error was dispatched to the parent, we don't need to add it to the list of errors.

		// the index of the error should be relative to the last token index.
		error.offsetIndex(this.lastParseState.tokenIndex);
		super.addError(error);
	}

	/**
	 * Returns a pair of two integers. The first integer is the index of the last token that was parsed. The second
	 * integer is the number of values that this argument received when being parsed the last time.
	 * These indices take into account whether the last value was in a tuple or not.
	 * @return The index of the last token that was parsed, and the number of values that this argument received.
	 */
	@NotNull Pair<@NotNull Integer, @NotNull Integer> getLastTokensIndicesPair() {
		int inTupleOffset = this.lastParseState.inTuple ? 1 : 0;
		int positionalOffset = this.lastParseState.positional ? 1 : 0;

		return new Pair<>(
			this.lastParseState.tokenIndex - (1 - positionalOffset) - inTupleOffset,
			this.lastParseState.receivedValuesCount + inTupleOffset*2 - positionalOffset
		);
	}

	/**
	 * @return The parse state snapshot of the last parsing operation.
	 */
	public ParseStateSnapshot getLastParseState() {
		return this.lastParseState;
	}

	/**
	 * Returns a stream of the values that this argument received when being parsed. This also sets
	 * {@code this.currentArgValueIndex} to the current index of the value when the stream is consumed.
	 * @param args The values that this argument received when being parsed.
	 * @return A stream of the values that this argument received when being parsed.
	 */
	protected final Stream<String> getArgValuesStream(@NotNull String @NotNull [] args) {
		var index = new AtomicInteger(0);
		return Stream.of(args).peek(arg -> this.currentArgValueIndex = index.getAndIncrement());
	}

	@Override
	public void resetState() {
		super.resetState();

		this.currentValue = this.initialValue;
		this.currentArgValueIndex = 0;
		this.usageCount = 0;

		// reset the state of the subtypes.
		this.subTypes.forEach(ArgumentType::resetState);
	}

	@Override
	public @Nullable ArgumentType<?> getParent() {
		return this.parentArgType;
	}
}