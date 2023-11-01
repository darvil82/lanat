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

	/**
	 * Creates a new {@link ModifyRecord} with the given value.
	 * @param value The value to store.
	 * @return A new {@link ModifyRecord} with the given value.
	 * @param <T> The type of the value.
	 */
	public static <T> ModifyRecord<T> of(@NotNull T value) {
		return new ModifyRecord<>(value);
	}

	/**
	 * Creates a new empty {@link ModifyRecord}.
	 * @return A new empty {@link ModifyRecord}.
	 * @param <T> The type of the value.
	 */
	public static <T> ModifyRecord<T> empty() {
		return new ModifyRecord<>(null);
	}

	/**
	 * Returns the value stored in the {@link ModifyRecord}.
	 * @return The value stored in the {@link ModifyRecord}.
	 */
	public T get() {
		return this.value;
	}

	/**
	 * Sets the value to the specified value and marks this {@link ModifyRecord} as modified.
	 * @param value The value to set.
	 */
	public void set(T value) {
		this.value = value;
		this.modified = true;
	}

	/**
	 * Sets the value to the value provided by the specified {@link ModifyRecord} and marks this {@link ModifyRecord} as modified.
	 * @param value The value to set.
	 */
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
		this.setIfNotModified(value.value);
	}

	/**
	 * Returns {@code true} if the value has been modified.
	 * @return {@code true} if the value has been modified.
	 */
	public boolean isModified() {
		return this.modified;
	}

	@Override
	public String toString() {
		return "[" + (this.modified ? "modified" : "clean") + "] " + (this.value == null ? "" : this.value);
	}
}
