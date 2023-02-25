package lanat.argumentTypes;

import fade.mirror.Invokable;
import fade.mirror.MClass;
import fade.mirror.MMethod;
import fade.mirror.Parameterized;
import fade.mirror.exception.MirrorException;
import lanat.ArgumentType;
import lanat.exceptions.ArgumentTypeException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import static fade.mirror.Mirror.mirror;

public class TryParseArgument<T> extends ArgumentType<T> {
	private final Function<String, Object> parseMethod;
	private final @NotNull MClass<T> type;


	public TryParseArgument(@NotNull Class<T> type) {
		this.type = mirror(type);

		if ((this.parseMethod = this.getParseMethod()) == null)
			throw new ArgumentTypeException(
				"Type " + type.getName() + " must have a static valueOf(String), parse(String), "
					+ "or from(String) method, or a constructor that takes a string."
			);
	}

	private <I extends Parameterized & Invokable<?>> boolean isValidMethod(I method) {
		if (method.getParameterCount() != 1) return false;
		if (method.getReturnType() != this.type.getRawClass()) return false;
		return method.getParameter(parameter -> parameter.getType().equals(String.class)).isPresent();
	}

	@Override
	protected void addError(@NotNull String value) {
		super.addError("Unable to parse value '" + value + "' as type " + this.type.getSimpleName() + ".");
	}

	private @Nullable Function<String, Object> getParseMethod() {
		// Get a static valueOf(String), a parse(String), or a from(String) method.
		final var method = this.type.getMethods()
			.filter(MMethod::isStatic)
			.filter(this::isValidMethod)
			.filter(m -> (m.getName().equals("valueOf") || m.getName().equals("from") || m.getName().equals("parse")))
			.findFirst();

		// if we found a method, return that.
		if (method.isPresent()) {
			return input -> {
				try {
					return method.get().invoke(input);
				} catch (MirrorException exception) {
					this.addError(input);
				}
				return null;
			};
		}

		// Otherwise, try to find a constructor that takes a string.
		return this.type.getConstructors()
			.filter(this::isValidMethod)
			.findFirst()
			.<Function<String, Object>>map(constructor -> s -> {
				try {
					return constructor.invoke(s);
				} catch (MirrorException exception) {
					this.addError(s);
				}
				return null;
			}).orElse(null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public @Nullable T parseValues(@NotNull String @NotNull [] args) {
		try {
			return (T) this.parseMethod.apply(args[0]);
		} catch (Exception e) {
			throw new ArgumentTypeException("Unable to cast value '" + args[0] + "' to type " + this.type.getSimpleName() + ".", e);
		}
	}
}