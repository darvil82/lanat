package lanat.test.exampleTests;

import lanat.*;
import lanat.argumentTypes.CounterArgumentType;
import lanat.argumentTypes.IntegerArgumentType;
import lanat.argumentTypes.MultipleStringsArgumentType;
import lanat.argumentTypes.NumberRangeArgumentType;
import lanat.utils.Range;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

public final class ExampleTest {
	@Test
	public void main() {
		Argument.PrefixChar.defaultPrefix = Argument.PrefixChar.MINUS;
//		TextFormatter.enableSequences = false;

		var ap = new ArgumentParser("my-program") {{
			this.setCallbackInvocationOption(CallbacksInvocationOption.NO_ERROR_IN_ARGUMENT);
			this.addHelpArgument();
			this.addArgument(Argument.create(new CounterArgumentType(), "counter", "c").onOk(System.out::println));
			this.addArgument(Argument.create(new Example1Type(), "user", "u").required().positional());
			this.addArgument(Argument.createOfBoolType("t").onOk(v -> System.out.println("present")));
			this.addArgument(Argument.create(new NumberRangeArgumentType<>(0.0, 15.23), "number").onOk(System.out::println));
			this.addArgument(Argument.create(new MultipleStringsArgumentType(Range.from(3).to(5)), "string", "s").onOk(System.out::println).withPrefix(Argument.PrefixChar.PLUS));
			this.addArgument(Argument.create(new IntegerArgumentType(), "test").onOk(System.out::println).allowsUnique());

			this.addCommand(new Command("sub1", "testing") {{
				this.addArgument(Argument.create(new IntegerArgumentType(), "required").required().positional());
				this.addArgument(Argument.create(new NumberRangeArgumentType<>(0.0, 15.23), "number").onOk(System.out::println));
				this.addCommand(new Command("sub2", "testing") {{
					this.addArgument(Argument.create(new IntegerArgumentType(), "required").required().positional());
					this.addArgument(Argument.create(new NumberRangeArgumentType<>(0.0, 15.23), "number").onOk(System.out::println));
				}});
			}});
		}};

		ap.parse(CLInput.from("josh meck --number 2 -cccc ++string [test1 "
				+ "test2 t2] -ccc sub1 --required 1 --number 12 sub2 2 --number [4]"))
			.printErrors()
			.getParsedArguments();
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