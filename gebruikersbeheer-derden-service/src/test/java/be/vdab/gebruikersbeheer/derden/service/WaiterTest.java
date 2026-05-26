package be.vdab.gebruikersbeheer.derden.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

class WaiterTest {

	@Test
	@DisplayName("WHEN max duration is 1 second, " +
			"THEN run time is between 1 and 1.1 seconds.")
	void when() {
		BooleanSupplier supplier = () -> false;
		long start = System.currentTimeMillis();
		Waiter.waitFor(start, 1, supplier);
		long end = System.currentTimeMillis();

		assertThat(end - start).isBetween(1000L, 1100L);
	}
}