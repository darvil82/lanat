package lanat.test.exampleTests;

import lanat.*;
import lanat.argumentTypes.CounterArgumentType;
import lanat.argumentTypes.IntegerArgumentType;
import lanat.argumentTypes.MultipleStringsArgumentType;
import lanat.argumentTypes.NumberRangeArgumentType;
import lanat.helpRepresentation.HelpFormatter;
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

		var ap = new ArgumentParser("my-program") {{
			this.setCallbackInvocationOption(CallbacksInvocationOption.NO_ERROR_IN_ARGUMENT);
			this.setDescription("This is the description of my program. What do you think about it? Oh by the way, don't forget to use the <link=args.user> argument!");
			this.setLicense("Here's some more extra information about my program. Really don't know how to fill this out...");
			this.addHelpArgument();
			this.addArgument(Argument.create(new CounterArgumentType(), "counter", "c").onOk(System.out::println));
			this.addArgument(Argument.create(new Example1Type(), "user", "u").required().positional().withDescription("Specify the user/s to use."));
			this.addArgument(Argument.createOfBoolType("t").onOk(v -> System.out.println("present")));
			this.addArgument(Argument.create(new NumberRangeArgumentType<>(0.0, 15.23), "number").onOk(System.out::println).withDescription("The value that matters the most (not really). Hey, this thing is generated automatically as well!: <desc=!.type>"));
			this.addArgument(Argument.create(new MultipleStringsArgumentType(Range.from(3).to(5)), "string", "s").onOk(System.out::println).withPrefix(Argument.PrefixChar.PLUS));
			this.addArgument(Argument.create(new IntegerArgumentType(), "test").onOk(System.out::println).allowsUnique());

			this.addCommand(new Command("sub1", "testing") {{
				this.addArgument(Argument.create(new IntegerArgumentType(), "required").required().positional());
				this.addArgument(Argument.create(new NumberRangeArgumentType<>(0.0, 15.23), "number").onOk(System.out::println));
				this.setDescription("Now this is the description of the subcommand inside the main command.");
				this.addCommand(new Command("sub2", "testing") {{
					this.addArgument(Argument.create(new IntegerArgumentType(), "required").required().positional());
					this.addArgument(Argument.create(new NumberRangeArgumentType<>(0.0, 15.23), "number").onOk(System.out::println));
				}});
			}});
		}};

		ap.parse(CLInput.from("josh ! --number 2 sub1 --required 1 --number 121"))
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