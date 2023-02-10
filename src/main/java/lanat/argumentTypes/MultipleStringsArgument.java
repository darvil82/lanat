package lanat.argumentTypes;

import lanat.ArgValueCount;
import lanat.ArgumentType;
import org.jetbrains.annotations.NotNull;

public class MultipleStringsArgument extends ArgumentType<String[]> {
	@Override
	public @NotNull ArgValueCount getArgValueCount() {
		return ArgValueCount.AT_LEAST_ONE;
	}

	@Override
	public @NotNull String[] parseValues(@NotNull String @NotNull [] args) {
		return args;
	}
}
