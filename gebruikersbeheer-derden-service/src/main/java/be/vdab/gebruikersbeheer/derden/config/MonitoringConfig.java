package be.vdab.gebruikersbeheer.derden.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class MonitoringConfig {

	@Bean("sessionCounter")
	public Counter totalSessionCounter(MeterRegistry meterRegistry) {

		return Counter.builder("isim.totalSessionCount")
			.description("Number of sessions created")
			.register(meterRegistry);
	}

	@Bean("activeSessionCounter")
	public AtomicInteger activeSessionCounter(MeterRegistry meterRegistry) {
		AtomicInteger activeSessions= new AtomicInteger(0);

		Gauge.builder("isim.activeSessionCount", () -> activeSessions)
			.description("Number of active sessions").register(meterRegistry);

		return activeSessions;
	}
}
