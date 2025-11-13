package io.github.darvil.lanat.helpgen.descriptions.tags;

import io.github.darvil.lanat.Argument;
import io.github.darvil.lanat.helpgen.descriptions.Tag;
import io.github.darvil.lanat.helpgen.descriptions.exceptions.MalformedTagException;
import io.github.darvil.lanat.utils.NamedWithDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Gets the default value of an argument.
 * <p>
 * Returns {@code Default value: <value>}.
 * </p>
 * If a value is given, it is used. Otherwise, the default value of the argument is used.
 * If neither is available, a {@link MalformedTagException} is thrown.
 */
public class DefaultValueTag extends Tag {
	@Override
	protected @NotNull String parse(@NotNull NamedWithDescription user, @Nullable String value) {
		if (!(user instanceof Argument<?, ?> arg))
			throw new MalformedTagException("The default value tag can only be used on arguments.");

		if (value == null && arg.getDefaultValue() == null)
			throw new MalformedTagException("No value could be found from neither the default argument value nor the tag value.");

		return "Default value: " + Objects.requireNonNullElse(value, arg.getDefaultValue());
	}
}