package lanat.utils;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface ErrorCallbacks<TOk, TErr> {
	/**
	 * Specify a function that will be called on error.
	 * @param callback The function to be called on error.
	 * */
	void setOnErrorCallback(@NotNull Consumer<@NotNull TErr> callback);

	/**
	 * Specify a function that will be called on success.
	 * @param callback The function to be called on success.
	 * */
	void setOnCorrectCallback(@NotNull Consumer<@NotNull TOk> callback);

	/**Executes the correct or error callback. */
	void invokeCallbacks();
}
