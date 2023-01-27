package argparser.utils;

import java.util.function.Supplier;

/**
 * Provides a way to see if the inner value has been modified since the constructor was called.
 *
 * @param <T> The type of the inner value.
 */
public class ModifyRecord<T> {
	private T value;
	private boolean modified;

	public ModifyRecord(T value) {
		this.value = value;
	}

	public T get() {
		return this.value;
	}

	public void set(T value) {
		this.value = value;
		this.modified = true;
	}

	public void set(ModifyRecord<T> value) {
		this.set(value.value);
	}

	/**
	 * Sets the value to the specified value if it has not been modified.
	 *
	 * @param value The value to set.
	 */
	public void setIfNotModified(T value) {
		if (!this.modified) {
			this.set(value);
		}
	}

	/**
	 * Sets the value to the specified value if it has not been modified.
	 *
	 * @param value The value to set.
	 */
	public void setIfNotModified(ModifyRecord<T> value) {
		if (!this.modified) {
			this.set(value);
		}
	}

	/**
	 * Sets the value to the supplied value from the callback if it has not been modified.
	 *
	 * @param cb The callback that supplies the value to set.
	 */
	public void setIfNotModified(Supplier<T> cb) {
		if (!this.modified) {
			this.set(cb.get());
		}
	}

	public boolean isModified() {
		return modified;
	}

	@Override
	public String toString() {
		return "[" + (this.modified ? "modified" : "clean") + "] " + this.value.toString();
	}
}
