package de.htwsaarland.config;

import java.util.Collection;
import org.junit.Test;

/**
 *
 * @author hbui
 */
public class ConfigCollectorTest {
	
	public ConfigCollectorTest() {
	}

	@Test
	public void collectAllConfig() {
		Collection<ConfigEntries.Entry> collectConfig = ConfigCollector.collectConfig();
	}
	
}
