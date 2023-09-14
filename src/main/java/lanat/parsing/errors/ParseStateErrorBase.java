package lanat.parsing.errors;

import lanat.ErrorFormatter;
import lanat.ErrorLevel;
import lanat.parsing.Token;
import lanat.utils.ErrorLevelProvider;
import lanat.utils.UtlReflection;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;


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
	/** The type of the error. */
	public final @NotNull T errorType;

	/** The index of the token that caused the error. */
	public int tokenIndex;

	/** The error handler that handles the error. */
	private ErrorHandler errorHandler;

	/** The error formatter that formats the error message. */
	private ErrorFormatter formatter;


	/**
	 * Creates a new error handler for the specified type of error.
	 * @param errorType The type of the error.
	 * @param tokenIndex The index of the token that caused the error.
	 */
	public ParseStateErrorBase(@NotNull T errorType, int tokenIndex) {
		this.errorType = errorType;
		this.tokenIndex = tokenIndex;
	}

	/**
	 * Returns the method that should be called to handle the error.
	 * @throws RuntimeException If no handler method is defined for the error type.
	 * @return The handler method.
	 */
	private @NotNull Method getHandlerMethod() {
		return UtlReflection.getMethods(this.getClass())
			.filter(m -> Optional.ofNullable(m.getAnnotation(Handler.class))
				.map(a -> a.value().equals(this.errorType.name()))
				.orElse(false)
			)
			.findFirst()
			.orElseThrow(() -> new RuntimeException("No handler method defined for error type " + this.errorType.name()));
	}

	/**
	 * Handles the error by calling the appropriate handler method.
	 * @param handler The error handler.
	 * @return The error message.
	 */
	public final @NotNull String handle(@NotNull ErrorHandler handler) {
		this.errorHandler = handler;
		this.formatter = new ErrorFormatter(handler, this.errorType.getErrorLevel());

		try {
			this.getHandlerMethod().invoke(this);
		} catch (InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		return this.formatter.toString();
	}

	@Override
	public @NotNull ErrorLevel getErrorLevel() {
		return this.errorType.getErrorLevel();
	}

	/**
	 * Returns the token at the index of this error.
	 */
	protected @NotNull Token getCurrentToken() {
		return this.errorHandler.getRelativeToken(this.tokenIndex);
	}

	/**
	 * Returns the current {@link ErrorFormatter} instance that can be configured to display the error.
	 */
	protected @NotNull ErrorFormatter fmt() {
		return this.formatter;
	}

	/** Annotation that defines a handler method for a specific error type. */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD })
	public @interface Handler {
		@NotNull String value();
	}
}