package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.ArgumentTypeInfer;
import org.jetbrains.annotations.NotNull;
import utils.Range;

/**
 * An argument type that takes multiple numbers.
 * @param <TInner> The type of number that this argument type is.
 */
public class MultipleNumbersArgumentType<TInner extends Number> extends TupleArgumentType<TInner> {
	/**
	 * Creates a new {@link TupleArgumentType} with the specified range of values that the argument will take.
	 * @param valueCount The range of values that the argument will take.
	 * @param defaultValue The default value of the argument. This will be used if no values are provided.
	 * @throws lanat.exceptions.ArgumentTypeInferException If the type of the default value is not supported.
	 */
	@SuppressWarnings("unchecked")
	public MultipleNumbersArgumentType(@NotNull Range valueCount, @NotNull TInner[] defaultValue) {
		super(
			valueCount,
			// we can infer the type of the argument type from the default value
			(ArgumentType<TInner>)ArgumentTypeInfer.get(defaultValue.getClass().getComponentType()),
			defaultValue
		);
	}
}