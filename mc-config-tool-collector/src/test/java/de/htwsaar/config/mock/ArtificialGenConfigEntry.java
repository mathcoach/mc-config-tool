package de.htwsaar.config.mock;

import de.htwsaar.config.ConfigMockGenerator;
import de.htwsaar.config.ConfigEntries;
import org.openide.util.lookup.ServiceProvider;

/**
 * simulate a GenConfigEntry from other library.
 */
@ServiceProvider(
		service = ConfigEntries.class)
public final class ArtificialGenConfigEntry implements ConfigEntries{

	@Override
	public Entry[] getEntry() {
		return 
new Entry[]{
			new Entry(ConfigMockGenerator.ARTIFICIAL_CONFIG)
				.addUseIn(ArtificialGenConfigEntry.class.getName() , "an artificial description about the dummy-config")
				.addSuggestValue("sugguested")
			,
		}
		;
	}
	
}
