package be.vdab.gebruikersbeheer.derden.service;

import java.util.function.BooleanSupplier;

final class Waiter {

	private Waiter() {
	}

	static boolean waitFor(long startMillis, int maxDurationSeconds, BooleanSupplier condition) {
		long time = System.currentTimeMillis();

		boolean result = condition.getAsBoolean();
		while (!condition.getAsBoolean() && time - startMillis < maxDurationSeconds * 1000L) {
			try {
				Thread.sleep(500L);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}

			time = System.currentTimeMillis();
			result = condition.getAsBoolean();
		}

		return result;
	}
}
