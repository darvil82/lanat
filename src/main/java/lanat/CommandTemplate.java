package lanat;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

public abstract class CommandTemplate {
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface InitDef {
		boolean callSuper() default true;
	}

	public record CommandBuildHelper(@NotNull Command cmd, @NotNull List<Argument.ArgumentBuilder<?, ?>> args) {
		public Argument.ArgumentBuilder<?, ?> getArgument(@NotNull String name) {
			return this.args.stream()
				.filter(a -> a.hasName(name))
				.findFirst()
				.orElse(null);
		}

		public void addArgument(@NotNull Argument.ArgumentBuilder<?, ?> arg) {
			this.args.add(arg);
		}
	}

	@InitDef
	public static void init(@NotNull CommandBuildHelper helper) {}
}