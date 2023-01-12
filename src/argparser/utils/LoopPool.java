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
