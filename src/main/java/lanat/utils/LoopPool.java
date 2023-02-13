package lanat.utils;

import org.jetbrains.annotations.NotNull;

public class LoopPool<T> {
	private final T @NotNull [] pool;
	private int index;

	@SafeVarargs
	private LoopPool(int startAt, T @NotNull ... pool) {
		this.pool = pool;
		this.setIndex(startAt);
	}

	@SafeVarargs
	public static <T> LoopPool<T> of(T @NotNull ... pool) {
		return new LoopPool<>(0, pool);
	}

	@SafeVarargs
	public static <T> LoopPool<T> of(int startAt, T @NotNull ... pool) {
		return new LoopPool<>(startAt, pool);
	}

	@SafeVarargs
	public static <T> LoopPool<T> atRandomIndex(T @NotNull ... pool) {
		return new LoopPool<>(Random.randInt(pool.length), pool);
	}

	private int getValidIndex(int index) {
		return index < 0 ? this.pool.length - 1 : index % this.pool.length;
	}

	private void setIndex(int index) {
		this.index = this.getValidIndex(index);
	}

	public T next() {
		this.setIndex(this.index + 1);
		return this.current();
	}

	public T prev() {
		this.setIndex(this.index - 1);
		return this.current();
	}

	public T current() {
		return this.pool[this.index];
	}

	public T at(int relativeIndex) {
		return this.pool[this.getValidIndex(this.index + relativeIndex)];
	}
}
