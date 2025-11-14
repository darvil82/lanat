package io.github.darvil.lanat.tests;

import io.github.darvil.lanat.parsing.errors.handlers.SimpleError;
import io.github.darvil.lanat.utils.errors.ErrorContainerImpl;
import io.github.darvil.lanat.utils.errors.ErrorLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestMisc {
	@Test
	@DisplayName("Test error levels and thresholds")
	public void testErrorLevels() {
		assertFalse(ErrorLevel.INFO.isInThreshold(ErrorLevel.ERROR));
		assertTrue(ErrorLevel.INFO.isInThreshold(ErrorLevel.INFO));
		assertTrue(ErrorLevel.ERROR.isInThreshold(ErrorLevel.WARNING));
		assertTrue(ErrorLevel.ERROR.isInThreshold(ErrorLevel.ERROR));
		assertTrue(ErrorLevel.ERROR.isInThreshold(ErrorLevel.DEBUG));
	}

	@Test
	@DisplayName("Test ErrorContainer and threshold checks")
	public void testErrorContainer() {
		// check illegal thresholds
		assertThrows(IllegalStateException.class, () -> {
			var container = new ErrorContainerImpl<SimpleError>() {};

			container.setErrorExitThreshold(ErrorLevel.WARNING);
			container.setErrorDisplayThreshold(ErrorLevel.ERROR);
		});

		assertDoesNotThrow(() -> {
			var container = new ErrorContainerImpl<SimpleError>() {};

			container.setErrorExitThreshold(ErrorLevel.ERROR);
			container.setErrorDisplayThreshold(ErrorLevel.WARNING);
		});
	}
}