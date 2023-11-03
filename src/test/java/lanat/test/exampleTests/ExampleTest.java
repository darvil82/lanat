package lanat.test.exampleTests;

import lanat.*;
import lanat.argumentTypes.IntegerArgumentType;
import lanat.argumentTypes.NumberRangeArgumentType;
import lanat.utils.Range;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

public final class ExampleTest {
	@Test
	public void main() {
		Argument.PrefixChar.defaultPrefix = Argument.PrefixChar.MINUS;
		var parsedArgs = new ArgumentParser("my-program") {{
			this.setCallbackInvocationOption(CallbacksInvocationOption.NO_ERROR_IN_ARGUMENT);
			this.addHelpArgument();
			this.addArgument(Argument.create(new Example1Type(), "user").required().positional());
			this.addArgument(Argument.create(new NumberRangeArgumentType<>(0.0, 15.23), "number").onOk(System.out::println));
			this.addArgument(Argument.create(new IntegerArgumentType(), "test").onOk(System.out::println).allowsUnique());
		}}.parse(CLInput.from("-h --number=3' --user [jo test ! hello]"))
			.printErrors()
			.getParsedArguments();

		System.out.println(parsedArgs);
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