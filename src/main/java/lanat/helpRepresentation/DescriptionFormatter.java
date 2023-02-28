package lanat.helpRepresentation;

import lanat.Command;
import org.jetbrains.annotations.NotNull;

public class DescriptionFormatter {
	private final String description;

	private DescriptionFormatter(@NotNull String description) {
		this.description = description;
	}

	public static DescriptionFormatter of(@NotNull String namedWithDescription) {
		return new DescriptionFormatter(namedWithDescription);
	}

	public @NotNull String format(Command command) {
		return this.description;
	}
}
