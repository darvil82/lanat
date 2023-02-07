package lanat.utils;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface ErrorCallbacks<TOk, TErr> {
	void setOnErrorCallback(@NotNull Consumer<@NotNull TErr> callback);

	void setOnCorrectCallback(@NotNull Consumer<@NotNull TOk> callback);

	void invokeCallbacks();
}
