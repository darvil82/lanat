package lanat.test.exampleTests;

import lanat.*;
import lanat.argumentTypes.CounterArgumentType;
import lanat.argumentTypes.IntegerArgumentType;
import lanat.argumentTypes.MultipleStringsArgumentType;
import lanat.argumentTypes.NumberRangeArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import utils.Range;

public final class ExampleTest {
	@Test
	public void main() {
		Argument.PrefixChar.defaultPrefix = Argument.PrefixChar.MINUS;
//		HelpFormatter.lineWrapMax = 80;
//		TextFormatter.enableSequences = false;
//		ErrorFormatter.errorFormatterClass = SimpleErrorFormatter.class;

		var result = ArgumentParser.parseFromInto(
			CommandTemplateExample.class,
			CLInput.from("--number 12 sub-command -ccc"),
			opts -> opts.printErrors()
		);
	}

	public static class Example1Type extends ArgumentType<String[]> {
		@Override
		public @Nullable String[] parseValues(@NotNull String... args) {
			this.forEachArgValue(args, str -> {
				if (str.equals("!")) {
					this.addError("The user cannot be '!'.", ErrorLevel.ERROR);
				}
			});
			return args;
		}

		@Override
		public @NotNull Range getRequiredArgValueCount() {
			return Range.from(2).toInfinity();
		}
	}
}