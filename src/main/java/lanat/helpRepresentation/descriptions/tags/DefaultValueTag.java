package lanat.helpRepresentation.descriptions.tags;

import lanat.Argument;
import lanat.helpRepresentation.descriptions.Tag;
import lanat.helpRepresentation.descriptions.exceptions.MalformedTagException;
import lanat.utils.NamedWithDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Gets the default value of an argument.
 * <p>
 * Returns "Default value: &lt;value&gt;".
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