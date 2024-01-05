package lanat.test.exampleTests;

import lanat.*;
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

//		ArgumentParser.parseFromInto(
//			CommandTemplateExample.class,
//			CLInput.from("--number 12 sub-command -ccc"),
//			opts -> opts.printErrors()
//		);

		ArgumentParser.from(CommandTemplateExample.class)
			.parse(CLInput.from("-hest"))
			.withActions(actions -> actions.printErrors())
			.getResult()
			.getUsedResults()
			.forEach(result -> {
				var cmdName = result.getCommand().getName();
				System.out.println(cmdName + " was used!");

				switch (cmdName) {
					case "my-program" -> System.out.println("number value: " + result.get("number"));
					case "sub-command" -> System.out.println("c value: " + result.get("c"));
					case "another-sub-command" -> System.out.println("c value: " + result.get("c"));
				}
			});

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