package argparser.utils;

public class LoopPool<T> {
	private final T[] pool;
	private int index;

	@SafeVarargs
	public LoopPool(int startAt, T... pool) {
		this.pool = pool;
		this.index = startAt < 0 ? Random.randInt(pool.length) : startAt % pool.length;
	}

	@SafeVarargs
	public LoopPool(T... pool) {
		this(0, pool);
	}

	private void setIndex(int index) {
		this.index = index % this.pool.length;
		if (this.index < 0) {
			this.index = this.pool.length - 1;
		}
	}

	public T next() {
		final T value = this.pool[this.index];
		this.setIndex(this.index + 1);
		return value;
	}

	public T prev() {
		this.setIndex(this.index - 1);

		return this.pool[this.index];
	}

	public T current() {
		return this.pool[this.index];
	}
}
