package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.Builder;
import lanat.utils.errors.ErrorLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.TextFormatter;
import utils.Range;

import java.util.Objects;

/**
 * An argument type that allows to specify its behavior with a builder pattern.
 * @param <T> The type of the value that this argument type will parse into.
 */
public final class SimpleArgumentType<T> extends ArgumentType<T> {
	private final @NotNull ParseFunction<T> parseFunction;
	private @Nullable String name;
	private @Nullable String description;
	private @Nullable Range valueCountBounds;
	private @Nullable Range usageCountBounds;

	private SimpleArgumentType(@NotNull ParseFunction<T> parseFunction) {
		this.parseFunction = parseFunction;
	}

	/**
	 * Creates a new {@link SimpleArgumentType} with the specified parse function.
	 * @param parseFunction The function that will be used to parse the values.
	 * @param <T> The type of the value that this argument type will parse into.
	 * @return A new {@link SimpleArgumentTypeBuilder} to specify the behavior of the argument type.
	 * @see ParseFunction#parse(String[], SimpleArgumentType.ErrorProxy)
	 */
	public static <T> @NotNull SimpleArgumentTypeBuilder<T> of(@NotNull ParseFunction<T> parseFunction) {
		return new SimpleArgumentTypeBuilder<>(parseFunction);
	}

	@Override
	public @Nullable T parseValues(@NotNull String @NotNull ... values) {
		return this.parseFunction.parse(values, new ErrorProxy());
	}

	@Override
	public @NotNull String getName() {
		return Objects.requireNonNullElse(this.name, super.getName());
	}

	@Override
	public @Nullable TextFormatter getRepresentation() {
		return this.name == null ? null : super.getRepresentation();
	}

	@Nullable
	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public @NotNull Range getValueCountBounds() {
		return Objects.requireNonNullElse(this.valueCountBounds, super.getValueCountBounds());
	}

	@Override
	public @NotNull Range getUsageCountBounds() {
		return Objects.requireNonNullElse(this.usageCountBounds, super.getUsageCountBounds());

	}


	/**
	 * A proxy class that allows to add errors to the argument type.
	 */
	public final class ErrorProxy {
		private ErrorProxy() {}


		/** @see ArgumentType#addError(String) */
		public void addError(@NotNull String message) {
			SimpleArgumentType.this.addError(message);
		}

		/** @see ArgumentType#addError(String, ErrorLevel) */
		public void addError(@NotNull String message, @NotNull ErrorLevel level) {
			SimpleArgumentType.this.addError(message, level);
		}

		/** @see ArgumentType#addError(String, int) */
		public void addError(@NotNull String message, int index, @NotNull ErrorLevel level) {
			SimpleArgumentType.this.addError(message, index, level);
		}
	}

	/**
	 * A functional interface that allows to specify the parse function of the basic argument type.
	 * @param <T> The type of the value that the argument type will parse into.
	 * @see #parse(String[], SimpleArgumentType.ErrorProxy)
	 */
	@FunctionalInterface
	public interface ParseFunction<T> {
		/**
		 * Parses the received values and returns the result. If the values are invalid,
		 * this method shall return {@code null}.
		 * @param values The values that were received.
		 * @param errorProxy A proxy that allows to add errors to the argument type.
		 * @return The parsed value.
		 * @see ArgumentType#parseValues(String...)
		 */
		@Nullable T parse(@NotNull String @NotNull [] values, @NotNull SimpleArgumentType<T>.ErrorProxy errorProxy);
	}

	/**
	 * A builder for a {@link SimpleArgumentType}.
	 * @param <T> The type of the value that this argument type will parse into.
	 */
	public static final class SimpleArgumentTypeBuilder<T> implements Builder<SimpleArgumentType<T>> {
		private final @NotNull ParseFunction<T> parseFunction;
		private @Nullable String name;
		private @Nullable String description;
		private @Nullable Range valueCountBounds;
		private @Nullable Range usageCountBounds;

		private SimpleArgumentTypeBuilder(@NotNull ParseFunction<T> parseFunction) {
			this.parseFunction = parseFunction;
		}

		/**
		 * Sets the name of the argument type, which will be displayed in the help message.
		 * @see ArgumentType#getName()
		 */
		public @NotNull SimpleArgumentTypeBuilder<T> withName(@NotNull String name) {
			this.name = name;
			return this;
		}

		/** @see ArgumentType#getDescription() */
		public @NotNull SimpleArgumentTypeBuilder<T> withDescription(@NotNull String description) {
			this.description = description;
			return this;
		}

		/** @see Parseable#getValueCountBounds() */
		public @NotNull SimpleArgumentTypeBuilder<T> withValueCountBounds(@NotNull Range valueCount) {
			this.valueCountBounds = valueCount;
			return this;
		}

		/** @see Parseable#getUsageCountBounds() */
		public @NotNull SimpleArgumentTypeBuilder<T> withUsageCountBounds(@NotNull Range usageCount) {
			this.usageCountBounds = usageCount;
			return this;
		}

		@Override
		public @NotNull SimpleArgumentType<T> build() {
			var result = new SimpleArgumentType<>(this.parseFunction);
			result.name = this.name;
			result.description = this.description;
			result.valueCountBounds = this.valueCountBounds;
			result.usageCountBounds = this.usageCountBounds;

			return result;
		}
	}
}