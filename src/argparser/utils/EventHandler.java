package argparser.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventHandler<T> {
	private final List<Consumer<T>> listeners = new ArrayList<>();

	public void addListener(Consumer<T> listener) {
		this.listeners.add(listener);
	}

	public void removeListener(Consumer<T> listener) {
		this.listeners.remove(listener);
	}

	public void invoke(T value) {
		this.listeners.forEach(c -> c.accept(value));
	}

	public void removeAll() {
		this.listeners.clear();
	}
}
