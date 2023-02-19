package lanat;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface MultipleNamesAndDescription<T> extends NamedWithDescription {
	T addNames(String... names);
	List<String> getNames();

	/** Returns the name of this object. If multiple names are defined, the longest name will be returned. */
	@Override
	default @NotNull String getName() {
		final var names = this.getNames();
		if (names.size() == 1)
			return names.get(0);

		return new ArrayList<>(this.getNames()) {{
			this.sort((a, b) -> b.length() - a.length());
		}}.get(0);
	}

	default boolean hasName(String name) {
		return this.getNames().contains(name);
	}
}
