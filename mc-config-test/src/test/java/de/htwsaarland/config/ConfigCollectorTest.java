package de.htwsaarland.config;

import de.htwsaarland.config.ConfigCollector;
import de.htwsaarland.config.ConfigEntries;
import de.htwsaarland.config.mock.ConfigMockGenerator;
import de.htwsaarland.config.mock.OtherConfigMockGenerator;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import static org.fest.assertions.api.Assertions.*;
import org.fest.assertions.core.Condition;
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
		assertThat(collectConfig).isNotEmpty();
		for (ConfigEntries.Entry e : collectConfig){
			System.out.println(e.toString());
		}
	}
	
	@Test
	public void validateName() {
		Map<String, ConfigEntries.Entry> configEntryMap = ConfigCollector.collectConfigAsMap();
		assertThat(configEntryMap).containsKey(ConfigMockGenerator.DUMMY_CONFIG);
		assertThat(configEntryMap).containsKey(OtherConfigMockGenerator.OTHER_DUMMY_CONFIG);

		ConfigEntries.Entry dummyConfigEntry = configEntryMap.get(ConfigMockGenerator.DUMMY_CONFIG);
		Set<ConfigEntries.ConfigUser> configUsers = dummyConfigEntry.useIn();
		assertThat(configUsers).hasSize(2);
		assertThat(configUsers).areExactly(1, new Condition<ConfigEntries.ConfigUser>(){
			@Override
			public boolean matches(ConfigEntries.ConfigUser value) {
				return value.getName().equals(ConfigMockGenerator.class.getName());
			}
		});
		assertThat(configUsers).areExactly(1, new Condition<ConfigEntries.ConfigUser>(){
			@Override
			public boolean matches(ConfigEntries.ConfigUser value) {
				return value.name.equals(OtherConfigMockGenerator.class.getName());
			}
		});
	}
	
	@Test
	public void configInLibraryAreMerged() {
		Map<String, ConfigEntries.Entry> configEntryMap = ConfigCollector.collectConfigAsMap();
		ConfigEntries.Entry artificialConfig = configEntryMap.get(ConfigMockGenerator.ARTIFICIAL_CONFIG);
		assertThat(artificialConfig.getName()).isEqualTo(ConfigMockGenerator.ARTIFICIAL_CONFIG);
		Set<ConfigEntries.ConfigUser> configUsers = artificialConfig.useIn();
		assertThat(configUsers).hasSize(2);
	}
	
	@Test
	public void addAnSugguestValueToAConfig() {
		Map<String, ConfigEntries.Entry> configEntryMap = ConfigCollector.collectConfigAsMap();
		ConfigEntries.Entry artificialConfig = configEntryMap.get(ConfigMockGenerator.ARTIFICIAL_CONFIG);
		assertThat(artificialConfig.getName()).isEqualTo(ConfigMockGenerator.ARTIFICIAL_CONFIG);
		final String suggestValue = "a sugesst value on runtime";
		artificialConfig.addSuggestValue(suggestValue);
		assertThat(artificialConfig.suggestValue).contains(suggestValue);
	}
	
}
