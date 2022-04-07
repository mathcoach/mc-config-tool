package de.htwsaar.config;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.Condition;
/**
 *
 * @author hbui
 */
class ConfigCollectorTest {
	

	@Test
	void validateName() {
		Map<String, ConfigEntries.Entry> configEntryMap = ConfigCollector.collectConfigAsMap();
		assertThat(configEntryMap)
            .containsKey(ConfigMockGenerator.DUMMY_CONFIG)
            .containsKey(OtherConfigMockGenerator.OTHER_DUMMY_CONFIG);

		ConfigEntries.Entry dummyConfigEntry = configEntryMap.get(ConfigMockGenerator.DUMMY_CONFIG);
		Set<ConfigEntries.ConfigUser> configUsers = dummyConfigEntry.useIn();
		assertThat(configUsers)
            .hasSize(2)
            .areExactly(1, new Condition<ConfigEntries.ConfigUser>(){
                @Override
                public boolean matches(ConfigEntries.ConfigUser value) {
                    return value.getName().equals(ConfigMockGenerator.class.getName());
                }
            })
            .areExactly(1, new Condition<ConfigEntries.ConfigUser>(){
                @Override
                public boolean matches(ConfigEntries.ConfigUser value) {
                    return value.name.equals(OtherConfigMockGenerator.class.getName());
                }
            });
	}
	
	@Test
	void configInLibraryAreMerged() {
		Map<String, ConfigEntries.Entry> configEntryMap = ConfigCollector.collectConfigAsMap();
		ConfigEntries.Entry artificialConfig = configEntryMap.get(ConfigMockGenerator.ARTIFICIAL_CONFIG);
		assertThat(artificialConfig.getName()).isEqualTo(ConfigMockGenerator.ARTIFICIAL_CONFIG);
		Set<ConfigEntries.ConfigUser> configUsers = artificialConfig.useIn();
		assertThat(configUsers).hasSize(2);
	}
	
	@Test
	void addAnSugguestValueToAConfig() {
		Map<String, ConfigEntries.Entry> configEntryMap = ConfigCollector.collectConfigAsMap();
		ConfigEntries.Entry artificialConfig = configEntryMap.get(ConfigMockGenerator.ARTIFICIAL_CONFIG);
		assertThat(artificialConfig.getName()).isEqualTo(ConfigMockGenerator.ARTIFICIAL_CONFIG);
		final String suggestValue = "a sugesst value on runtime";
		artificialConfig.addSuggestValue(suggestValue);
		assertThat(artificialConfig.suggestValue()).contains(suggestValue);
	}
	
}
