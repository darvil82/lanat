package lanat.helpRepresentation.descriptions.tags;

import lanat.helpRepresentation.descriptions.DescriptionParser;
import lanat.helpRepresentation.descriptions.DescriptionUser;
import lanat.helpRepresentation.descriptions.RouteParser;
import lanat.helpRepresentation.descriptions.Tag;
import lanat.helpRepresentation.descriptions.exceptions.InvalidRouteException;
import lanat.helpRepresentation.descriptions.exceptions.NoDescriptionDefinedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Gets the description of the target object specified by the route.
 * <p>
 * Note that targeting the user itself is not allowed, as it would create an infinitely recursive description. Keep in
 * mind that it is still possible to infinitely recurse by implicitly targeting the user, for example, by making an
 * argument show the description of another argument, which in turn shows the description of the first argument too.
 * This would eventually cause a {@link StackOverflowError}.
 * </p>
 *
 * @see RouteParser
 */
public class DescTag extends Tag {
	@Override
	protected @NotNull String parse(@NotNull DescriptionUser user, @Nullable String value) {
		final var target = RouteParser.parse(user, value);
		if (target == user)
			throw new InvalidRouteException("Cannot use desc tag to describe itself");

		return Optional.ofNullable(target.getDescription())
			.map(desc -> DescriptionParser.parse(target, desc))
			.orElseThrow(() -> new NoDescriptionDefinedException(target));
	}
}