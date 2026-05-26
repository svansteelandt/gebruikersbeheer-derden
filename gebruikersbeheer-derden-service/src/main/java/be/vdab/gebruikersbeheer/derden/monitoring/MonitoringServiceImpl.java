package be.vdab.gebruikersbeheer.derden.monitoring;

import io.micrometer.core.instrument.Counter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class MonitoringServiceImpl implements MonitoringService {

	private final Counter totalSessionCounter;
	private final AtomicInteger activeCount;

	public MonitoringServiceImpl(@Qualifier("activeSessionCounter") AtomicInteger activeCount, @Qualifier("sessionCounter") Counter totalSessionCounter) {
		this.activeCount = activeCount;
		this.totalSessionCounter = totalSessionCounter;
	}

	public void sessionCreated() {
		int activeCounter = this.activeCount.incrementAndGet();
		log.debug("*** Session created. Active sessions: {} ***", activeCounter);

		this.totalSessionCounter.increment();
	}

	public void sessionDestroyed() {
		int activeCounter = this.activeCount.decrementAndGet();
		log.debug("*** Session destroyed. Active sessions: {} ***", activeCounter);
	}
}
