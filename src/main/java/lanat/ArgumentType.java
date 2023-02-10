package lanat;

import lanat.argumentTypes.*;
import lanat.parsing.errors.CustomError;
import lanat.utils.ErrorsContainer;
import lanat.utils.Resettable;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class ArgumentType<T> extends ErrorsContainer<CustomError> implements Resettable, Parseable<T> {
	private T currentValue;
	private T initialValue;
	/**
	 * This is the current index of the value that is being parsed.
	 */
	private int currentArgValueIndex = 0;
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
	 * This value is set by the parent argument type when it runs {@link ArgumentType#registerSubType(ArgumentType)}.
	 */
	private @Nullable ArgumentType<?> parentArgType;
	private final @NotNull ArrayList<@NotNull ArgumentType<?>> subTypes = new ArrayList<>();

	public ArgumentType(@NotNull T initialValue) {
		this.setValue(this.initialValue = initialValue);
	}

	public ArgumentType() {}

	public final void parseAndUpdateValue(@NotNull String @NotNull [] args) {
		this.receivedValueCount = args.length;
		this.currentValue = this.parseValues(args);
	}

	public final void parseAndUpdateValue(@NotNull String arg) {
		this.receivedValueCount = 1;
		this.currentValue = this.parseValues(arg);
	}

	public final @Nullable T parseValues(@NotNull String arg) {
		return this.parseValues(new String[] { arg });
	}

	@Override
	public abstract @Nullable T parseValues(@NotNull String @NotNull [] args);


	/**
	 * By registering a subtype, this allows you to listen for errors that occurred in this subtype during
	 * parsing. The {@link ArgumentType#onSubTypeError(CustomError)} method will be called when an error occurs.
	 */
	protected final void registerSubType(@NotNull ArgumentType<?> subType) {
		if (subType.parentArgType == this) {
			throw new IllegalArgumentException("The sub type is already registered to this argument type.");
		}
		subType.tokenIndex = 0; // This is so the subtype will not throw the error that it was not parsed.
		subType.parentArgType = this;
		this.subTypes.add(subType);
	}

	/**
	 * This is called when a subtype of this argument type has an error.
	 * By default, this adds the error to the list of errors, while also adding
	 * the {@link ArgumentType#currentArgValueIndex}.
	 *
	 * @param error The error that occurred in the subtype.
	 */
	protected void onSubTypeError(@NotNull CustomError error) {
		error.tokenIndex += this.currentArgValueIndex;
		this.addError(error);
	}

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

	public @Nullable T getInitialValue() {
		return this.initialValue;
	}


	/**
	 * Specifies the number of values that this argument should receive when being parsed.
	 */
	@Override
	public @NotNull ArgValueCount getArgValueCount() {
		return ArgValueCount.ONE;
	}

	@Override
	public @Nullable TextFormatter getRepresentation() {
		return new TextFormatter(this.getClass().getSimpleName());
	}

	public final @Nullable T getFinalValue() {
		return this.currentValue;
	}

	/**
	 * Adds an error to the list of errors that occurred during parsing.
	 *
	 * @param message The message to display related to the error.
	 */
	protected void addError(@NotNull String message) {
		this.addError(message, this.currentArgValueIndex, ErrorLevel.ERROR);
	}

	/**
	 * Adds an error to the list of errors that occurred during parsing.
	 *
	 * @param message The message to display related to the error.
	 * @param index The index of the value that caused the error.
	 */
	protected void addError(@NotNull String message, int index) {
		this.addError(message, index, ErrorLevel.ERROR);
	}

	/**
	 * Adds an error to the list of errors that occurred during parsing.
	 *
	 * @param message The message to display related to the error.
	 * @param level The level of the error.
	 */
	protected void addError(@NotNull String message, @NotNull ErrorLevel level) {
		this.addError(message, this.currentArgValueIndex, level);
	}

	/**
	 * Adds an error to the list of errors that occurred during parsing.
	 *
	 * @param message The message to display related to the error.
	 * @param index The index of the value that caused the error.
	 * @param level The level of the error.
	 */
	protected void addError(@NotNull String message, int index, @NotNull ErrorLevel level) {
		if (!this.getArgValueCount().isIndexInRange(index)) {
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
	public void addError(@NotNull CustomError error) {
		if (!this.getArgValueCount().isIndexInRange(error.tokenIndex)) {
			throw new IndexOutOfBoundsException("Index " + error.tokenIndex + " is out of range for " + this.getClass().getName());
		}

		error.tokenIndex = this.tokenIndex + Math.min(error.tokenIndex + 1, this.receivedValueCount);

		super.addError(error);
		this.dispatchErrorToParent(error);
	}

	protected short getTokenIndex() {
		return this.tokenIndex;
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
	protected final void forEachArgValue(@NotNull String @NotNull [] args, @NotNull Consumer<@NotNull String> consumer) {
		for (int i = 0; i < args.length; i++) {
			this.currentArgValueIndex = i;
			consumer.accept(args[i]);
		}
	}

	@Override
	public void resetState() {
		this.currentValue = this.initialValue;
		this.tokenIndex = -1;
		this.currentArgValueIndex = 0;
		this.receivedValueCount = 0;
		this.subTypes.forEach(ArgumentType::resetState);
	}

	// Easy to access values. These are methods because we don't want to use the same instance everywhere.
	public static IntArgument INTEGER() {
		return new IntArgument();
	}

	public static IntRangeArgument INTEGER_RANGE(int min, int max) {
		return new IntRangeArgument(min, max);
	}

	public static FloatArgument FLOAT() {
		return new FloatArgument();
	}

	public static BooleanArgument BOOLEAN() {
		return new BooleanArgument();
	}

	public static CounterArgument COUNTER() {
		return new CounterArgument();
	}

	public static FileArgument FILE() {
		return new FileArgument();
	}

	public static StringArgument STRING() {
		return new StringArgument();
	}

	public static MultipleStringsArgument STRINGS() {
		return new MultipleStringsArgument();
	}

	public static <T extends ArgumentType<Ti>, Ti> KeyValuesArgument<T, Ti> KEY_VALUES(T valueType) {
		return new KeyValuesArgument<>(valueType);
	}

	public static <T extends Enum<T>> EnumArgument<T> ENUM(T enumDefault) {
		return new EnumArgument<>(enumDefault);
	}

	public static StdinArgument STDIN() {
		return new StdinArgument();
	}

	public static <T extends Parseable<Ti>, Ti> FromParseableArgument<T, Ti> FROM_PARSEABLE(T parseable) {
		return new FromParseableArgument<>(parseable);
	}

	public static <T> TryParseArgument<T> TRY_PARSE(Class<T> type) {
		return new TryParseArgument<>(type);
	}
}