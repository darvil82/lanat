package lanat.utils;

import org.jetbrains.annotations.NotNull;

/**
 * Provides a way to see if the inner value has been modified since the constructor was called.
 *
 * @param <T> The type of the inner value.
 */
public class ModifyRecord<T> {
	private T value;
	private boolean modified;

	private ModifyRecord(T value) {
		this.value = value;
	}

	public static <T> ModifyRecord<T> of(@NotNull T value) {
		return new ModifyRecord<>(value);
	}

	public static <T> ModifyRecord<T> empty() {
		return new ModifyRecord<>(null);
	}

	public T get() {
		return this.value;
	}

	public void set(T value) {
		this.value = value;
		this.modified = true;
	}

	public void set(@NotNull ModifyRecord<T> value) {
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
	 * The value is provided by the specified {@link ModifyRecord}.
	 *
	 * @param value The value to set.
	 */
	public void setIfNotModified(@NotNull ModifyRecord<T> value) {
		if (!this.modified) {
			this.set(value);
		}
	}

	public boolean isModified() {
		return this.modified;
	}

	@Override
	public String toString() {
		return "[" + (this.modified ? "modified" : "clean") + "] " + (this.value == null ? "" : this.value.toString());
	}
}
