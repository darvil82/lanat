package lanat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

public abstract class CommandTemplate {
	private Command command;

	protected final Command cmd() {
		return this.command;
	}

	final void applyTo(Command command) {
		this.command = command;

		// get all the methods with the @ArgDef annotation, and add them to the command
		Arrays.stream(this.getClass().getMethods())
			.filter(m -> m.isAnnotationPresent(ArgDef.class))
			.forEach(m -> {
				try {
					var arg = (Argument<?, ?>)m.invoke(this);
					if (arg == null) return;
					this.command.addArgument(arg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	protected @interface ArgDef {}
}
