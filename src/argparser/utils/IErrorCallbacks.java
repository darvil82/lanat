package argparser.utils;

import java.util.function.Consumer;

public interface IErrorCallbacks<TOk, TErr> {
	void setOnErrorCallback(Consumer<TErr> callback);
	void setOnCorrectCallback(Consumer<TOk> callback);
	void invokeCallbacks();
}
