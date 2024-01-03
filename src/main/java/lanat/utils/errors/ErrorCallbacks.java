package lanat.utils.errors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Interface for classes that have error and success callbacks.
 * @param <TOk> The type the success callback takes.
 * @param <TErr> The type the error callback takes.
 */
public interface ErrorCallbacks<TOk, TErr> {
	/**
	 * Specify a function that will be called on error.
	 * @param callback The function to be called on error.
	 */
	void setOnErrorCallback(@Nullable Consumer<@NotNull TErr> callback);

	/**
	 * Specify a function that will be called on success.
	 * @param callback The function to be called on success.
	 */
	void setOnOkCallback(@Nullable Consumer<@NotNull TOk> callback);

	/** Executes the correct or error callback. */
	void invokeCallbacks();
}