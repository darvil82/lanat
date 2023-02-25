package lanat.parsing.errors;

import lanat.ErrorFormatter;
import lanat.ErrorLevel;
import lanat.parsing.Token;
import lanat.utils.ErrorLevelProvider;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Provides a {@link ParseStateErrorBase#handle(ErrorHandler)} method that when called, automatically invokes the
 * appropriate defined method with the {@link Handler} annotation value that matches the value passed to the
 * constructor. This is used to display the errors on screen.
 *
 * <p>
 * Example class that inherits from this:
 * <pre>
 * {@code
 * class MyHandler extends ParseStateErrorBase<MyHandler.MyErrors> {
 *    public enum MyErrors implements ErrorLevelProvider {
 *       ERROR1,
 *       ERROR2;
 *
 *       @Override
 *       public ErrorLevel getErrorLevel() {
 *          return ErrorLevel.ERROR;
 *       }
 *    }
 *
 *    @Handler("ERROR1")
 *    public void handle1() {
 *       // do something
 *    }
 *
 *    @Handler("ERROR2")
 *    public void handle2() {
 *       // do something
 *    }
 * }
 *
 * ...
 *
 * var handler = new MyHandler(MyHandler.MyErrors.ERROR1);
 * handler.handle(errorHandler); // will call handle1()
 * }
 * </pre>
 * </p>
 * <p>
 * The enum type must implement {@link ErrorLevelProvider}. This allows the error text formatter to color errors
 * according to their severity.
 *
 * @param <T> An enum with the possible error types to handle.
 */
abstract class ParseStateErrorBase<T extends Enum<T> & ErrorLevelProvider> implements ErrorLevelProvider {
	public final @NotNull T errorType;
	public int tokenIndex;
	private ErrorHandler errorHandler;
	private ErrorFormatter formatter;
	private final List<Method> methods;

	public ParseStateErrorBase(@NotNull T errorType, int tokenIndex) {
		this.errorType = errorType;
		this.tokenIndex = tokenIndex;

		// check if there are methods defined for all error types
		this.methods = this.getAnnotatedMethods();

		for (final var handlerName : this.errorType.getClass().getEnumConstants()) {
			final var handlerNameStr = handlerName.name();

			// throw an exception if there is no method defined for the error type
			if (this.methods.stream().noneMatch(m -> this.isHandlerMethod(m, handlerNameStr)))
				throw new IllegalStateException("No method defined for error type " + handlerNameStr);
		}
	}

	private @NotNull List<@NotNull Method> getAnnotatedMethods() {
		Method[] methods;
		Class<?> currentClass = this.getClass();

		// if there are no methods defined, get super class
		// this is done for cases like usage of anonymous classes
		while ((methods = currentClass.getDeclaredMethods()).length == 0)
			currentClass = currentClass.getSuperclass();

		return Arrays.stream(methods).filter(m -> m.isAnnotationPresent(Handler.class)).toList();
	}

	private boolean isHandlerMethod(@NotNull Method method, @NotNull String handlerName) {


		return method.getAnnotation(Handler.class).value().equals(handlerName);
	}

	private boolean isHandlerMethod(@NotNull Method method) {
		return this.isHandlerMethod(method, this.errorType.name());
	}

	public final @NotNull String handle(@NotNull ErrorHandler handler) {
		this.errorHandler = handler;
		this.formatter = new ErrorFormatter(handler, this.errorType.getErrorLevel());

		// invoke the method if it is defined
		for (final var method : this.methods) {
			if (!this.isHandlerMethod(method)) continue;

			try {
				method.invoke(this);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return this.formatter.toString();
		}

		return this.formatter.toString();
	}

	@Override
	public @NotNull ErrorLevel getErrorLevel() {
		return this.errorType.getErrorLevel();
	}

	protected @NotNull Token getCurrentToken() {
		return this.errorHandler.getRelativeToken(this.tokenIndex);
	}

	/**
	 * Returns the current {@link ErrorFormatter} instance that can be configured to display the error.
	 */
	protected @NotNull ErrorFormatter fmt() {
		return this.formatter;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD })
	public @interface Handler {
		@NotNull String value();
	}
}