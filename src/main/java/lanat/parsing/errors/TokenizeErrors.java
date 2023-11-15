package lanat.parsing.errors;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public abstract class TokenizeErrors {
	private TokenizeErrors() {}

	public record TupleAlreadyOpenError(int index) implements Error.TokenizeError {
		@Override
		public void handle(@NotNull ErrorFormatter fmt, @NotNull TokenizeContext ctx) {
			fmt
				.withContent("Tuple already open.")
				.highlight(this.index, 0, false);
		}
	}

	public record TupleNotClosedError(int index) implements Error.TokenizeError {
		@Override
		public void handle(@NotNull ErrorFormatter fmt, @NotNull TokenizeContext ctx) {
			fmt
				.withContent("Tuple not closed.")
				.highlight(this.index + 1);
		}
	}

	public record UnexpectedTupleCloseError(int index) implements Error.TokenizeError {
		@Override
		public void handle(@NotNull ErrorFormatter fmt, @NotNull TokenizeContext ctx) {
			fmt
				.withContent("Unexpected tuple close.")
				.highlight(this.index, 0, false);
		}
	}

	public record StringNotClosedError(int index) implements Error.TokenizeError {
		@Override
		public void handle(@NotNull ErrorFormatter fmt, @NotNull TokenizeContext ctx) {
			fmt
				.withContent("String not closed.")
				.highlight(this.index + 1);
		}
	}

	public record SpaceRequiredError(int index) implements Error.TokenizeError {
		@Override
		public void handle(@NotNull ErrorFormatter fmt, @NotNull TokenizeContext ctx) {
			fmt
				.withContent("A space is required between these tokens.")
				.highlight(this.index, 1, false);
		}
	}
}
