package lanat.parsing.errors;

import lanat.Argument;
import lanat.ErrorLevel;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public abstract class TokenizeErrors {
	private TokenizeErrors() {}

	public record TupleAlreadyOpenError(int index) implements ErrorHandler.TokenizeErrorHandler {
		@Override
		public void handle(@NotNull ErrorFormatter fmt, @NotNull TokenizeContext ctx) {
			fmt
				.withContent("Tuple already open.")
				.highlight(this.index, 0, false);
		}
	}

	public record TupleNotClosedError(int index) implements ErrorHandler.TokenizeErrorHandler {
		@Override
		public void handle(@NotNull ErrorFormatter fmt, @NotNull TokenizeContext ctx) {
			fmt
				.withContent("Tuple not closed.")
				.highlight(this.index + 1);
		}
	}

	public record UnexpectedTupleCloseError(int index) implements ErrorHandler.TokenizeErrorHandler {
		@Override
		public void handle(@NotNull ErrorFormatter fmt, @NotNull TokenizeContext ctx) {
			fmt
				.withContent("Unexpected tuple close.")
				.highlight(this.index, 0, false);
		}
	}

	public record StringNotClosedError(int index) implements ErrorHandler.TokenizeErrorHandler {
		@Override
		public void handle(@NotNull ErrorFormatter fmt, @NotNull TokenizeContext ctx) {
			fmt
				.withContent("String not closed.")
				.highlight(this.index + 1);
		}
	}

	public record SimilarArgumentError(
		int index,
		@NotNull String name,
		@NotNull Argument<?, ?> argument
	) implements ErrorHandler.TokenizeErrorHandler {
		@Override
		public void handle(@NotNull ErrorFormatter fmt, @NotNull TokenizeContext ctx) {
			fmt
				.withContent(
					"Found argument with name given, but with a different prefix ("
						+ this.argument.getPrefix().character
						+ ")."
				)
				.highlight(this.index, this.name.length(), false);
		}

		@Override
		public @NotNull ErrorLevel getErrorLevel() {
			return ErrorLevel.WARNING;
		}
	}

	public record SpaceRequiredError(int index) implements ErrorHandler.TokenizeErrorHandler {
		@Override
		public void handle(@NotNull ErrorFormatter fmt, @NotNull TokenizeContext ctx) {
			fmt
				.withContent("A space is required between these tokens.")
				.highlight(this.index, 1, false);
		}
	}
}
