package lanat;

import fade.mirror.exception.MirrorException;
import fade.mirror.filter.Filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static fade.mirror.Mirror.mirror;

public abstract class CommandTemplate {
	private Command command;

	protected final Command cmd() {
		return this.command;
	}

	@SuppressWarnings("unchecked")
	final void applyTo(Command command) {
		this.command = command;

		// get all the methods with the @ArgDef annotation, and add them to the command
		mirror(this.getClass())
			.getMethods(Filter.forMethods().withReturnType(Argument.class).withAnnotations(ArgDef.class).withParameters())
			.forEach(method -> {
				try {
					var arg = method.bindToObject(this).invoke();
					if (arg == null) return;
					this.command.addArgument(arg);
				} catch (MirrorException exception) {
					exception.printStackTrace();
				}
			});
	}

	/**
	 * Annotation for methods that are used to define arguments to the command.
	 * The method must return an {@link Argument} object and take no parameters.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	protected @interface ArgDef {}
}