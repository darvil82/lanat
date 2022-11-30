package argparser;

public interface IArgumentAdder {
	/**
	 * Inserts an argument for this command to be parsed.
	 *
	 * @param argument the argument to be inserted
	 * @param <T> the ArgumentType subclass that will parse the value passed to the argument
	 * @param <TInner> the actual type of the value passed to the argument
	 */
	<T extends ArgumentType<TInner>, TInner> void addArgument(Argument<T, TInner> argument);
}
