package lanat.parsing.errors;

import fade.mirror.MClass;
import fade.mirror.MMethod;
import fade.mirror.exception.MirrorException;
import fade.mirror.filter.Filter;
import lanat.ErrorFormatter;
import lanat.ErrorLevel;
import lanat.parsing.Token;
import lanat.utils.ErrorLevelProvider;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.stream.Collectors;

import static fade.mirror.Mirror.mirror;

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
	private final List<MMethod<?>> methods;
	public int tokenIndex;
	private ErrorHandler errorHandler;
	private ErrorFormatter formatter;

	public ParseStateErrorBase(@NotNull T errorType, int tokenIndex) {
		this.errorType = errorType;
		this.tokenIndex = tokenIndex;

		// check if there are methods defined for all error types
		this.methods = this.getAnnotatedMethods();

		for (final var handlerName : this.errorType.getClass().getEnumConstants()) {
			final var handlerNameStr = handlerName.name();

			// make sure there is a method defined for each error type
			assert this.methods.stream().anyMatch(m -> this.isHandlerMethod(m, handlerNameStr))
				: "No method defined for error type " + handlerNameStr;
		}
	}

	private @NotNull List<@NotNull MMethod<?>> getAnnotatedMethods() {
		return mirror(this.getClass())
			.getSuperclassUntil(MClass::hasMethods, MClass.IncludeSelf.Yes)
			.<List<MMethod<?>>>map(objectMClass -> objectMClass.getMethods(Filter.forMethods().withAnnotation(Handler.class))
			.collect(Collectors.toList()))
			.orElseGet(List::of);
	}

	private boolean isHandlerMethod(@NotNull MMethod<?> method, @NotNull String handlerName) {
		return method.getAnnotationOfType(Handler.class)
			.map(handler -> handler.value().equals(handlerName))
			.orElse(false);
	}

	private boolean isHandlerMethod(@NotNull MMethod<?> method) {
		return this.isHandlerMethod(method, this.errorType.name());
	}

	public final @NotNull String handle(@NotNull ErrorHandler handler) {
		this.errorHandler = handler;
		this.formatter = new ErrorFormatter(handler, this.errorType.getErrorLevel());

		// invoke the method if it is defined
		for (final var method : this.methods) {
			if (!this.isHandlerMethod(method)) continue;

			try {
				method.requireAccessible(this).invokeWithInstance(this);
			} catch (MirrorException e) {
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