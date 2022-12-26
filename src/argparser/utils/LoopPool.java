package argparser.utils;

public class LoopPool<T> {
	private final T[] pool;
	private int index = 0;

	@SafeVarargs
	public LoopPool(int startAt, T... pool) {
		this.pool = pool;
		this.index = startAt < 0 ? Random.randInt(pool.length) : startAt % pool.length;
	}

	@SafeVarargs
	public LoopPool(T... pool) {
		this(0, pool);
	}

	public T next() {
		final T value = this.pool[this.index];
		this.index = (this.index + 1) % this.pool.length;
		return value;
	}
}
