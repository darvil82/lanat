package argparser.utils;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface ErrorCallbacks<TOk, TErr> {
	void setOnErrorCallback(@NotNull Consumer<TErr> callback);

	void setOnCorrectCallback(@NotNull Consumer<TOk> callback);

	void invokeCallbacks();
}
