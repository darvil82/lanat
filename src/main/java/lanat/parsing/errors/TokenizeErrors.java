package lanat.parsing.errors;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public abstract class TokenizeErrors {
	private TokenizeErrors() {}

	public record TupleAlreadyOpenError(int index) implements Error.TokenizeError {
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull TokenizeContext ctx) {
			fmt
				.withContent("Tuple already open.")
				.highlight(this.index, 0, false);
		}
	}

	public record TupleNotClosedError(int index) implements Error.TokenizeError {
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull TokenizeContext ctx) {
			fmt
				.withContent("Tuple not closed.")
				.highlight(this.index, ctx.getCount() - this.index - 1, false);
		}
	}

	public record UnexpectedTupleCloseError(int index) implements Error.TokenizeError {
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull TokenizeContext ctx) {
			fmt
				.withContent("Unexpected tuple close.")
				.highlight(this.index, 0, false);
		}
	}

	public record StringNotClosedError(int index) implements Error.TokenizeError {
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull TokenizeContext ctx) {
			fmt
				.withContent("String not closed.")
				.highlight(this.index, ctx.getCount() - this.index - 1, false);
		}
	}

	public record SpaceRequiredError(int index) implements Error.TokenizeError {
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull TokenizeContext ctx) {
			fmt
				.withContent("A space is required between these characters.")
				.highlight(this.index, 1, false);
		}
	}
}
