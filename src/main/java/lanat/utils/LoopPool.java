package lanat.utils;

import org.jetbrains.annotations.NotNull;

/**
 * A sequence of elements that can be looped through. The sequence can be looped through in both directions.
 * This may be done by calling {@link #next()} or {@link #prev()}.
 * <p>
 * An internal index is used to keep track of the current element. This index can be also be set manually by
 * calling {@link #setIndex(int)}.
 * </p>
 * @param <T> The type of the elements in the sequence.
 */
public class LoopPool<T> {
	private final T @NotNull [] pool;
	private int index;

	@SafeVarargs
	private LoopPool(int startAt, T @NotNull ... pool) {
		this.pool = pool;
		this.setIndex(startAt);
	}

	/**
	 * Creates a new  {@link LoopPool} with the given elements. The index will start at 0.
	 * @param pool The elements to loop through.
	 * @return A new  {@link LoopPool} with the given elements.
	 * @param <T> The type of the elements.
	 */
	@SafeVarargs
	public static <T> LoopPool<T> of(T... pool) {
		return new LoopPool<>(0, pool);
	}

	/**
	 * Creates a new  {@link LoopPool} with the given elements. The index will start at the given index.
	 * @param startAt The index to start at.
	 * @param pool The elements to loop through.
	 * @return A new  {@link LoopPool} with the given elements.
	 * @param <T> The type of the elements.
	 */
	@SafeVarargs
	public static <T> LoopPool<T> of(int startAt, T... pool) {
		return new LoopPool<>(startAt, pool);
	}

	/**
	 * Creates a new  {@link LoopPool} with the given elements. The index will start at a random index between 0 and the number
	 * of elements provided.
	 * @param pool The elements to loop through.
	 * @return A new  {@link LoopPool} with the given elements.
	 * @param <T> The type of the elements.
	 */
	@SafeVarargs
	public static <T> LoopPool<T> atRandomIndex(T... pool) {
		return new LoopPool<>(Random.randInt(pool.length), pool);
	}

	/**
	 * Returns a valid index to use in the pool array. If the index goes out of bounds, it will always wrap around.
	 * @param index The index to validate.
	 * @return A valid index to use in the pool array.
	 */
	private int getValidIndex(int index) {
		return index < 0 ? this.pool.length - 1 : index % this.pool.length;
	}

	/**
	 * Sets the internal index to the given index. If the index goes out of bounds, it will always wrap around.
	 * @param index The index to set.
	 */
	public void setIndex(int index) {
		this.index = this.getValidIndex(index);
	}

	/**
	 * Returns the internal index.
	 * @return The internal index.
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * Returns the next element in the sequence and increments the internal index.
	 * @return The next element in the sequence.
	 */
	public T next() {
		this.setIndex(this.index + 1);
		return this.current();
	}

	/**
	 * Returns the previous element in the sequence and decrements the internal index.
	 * @return The previous element in the sequence.
	 */
	public T prev() {
		this.setIndex(this.index - 1);
		return this.current();
	}

	/**
	 * Returns the current element in the sequence.
	 * @return The current element in the sequence.
	 */
	public T current() {
		return this.pool[this.index];
	}

	/**
	 * Returns the element at the given relative index. The index is relative to the internal index.
	 * @param relativeIndex The relative index.
	 * @return The element at the given relative index.
	 */
	public T at(int relativeIndex) {
		return this.pool[this.getValidIndex(this.index + relativeIndex)];
	}
}
