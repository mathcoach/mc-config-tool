package de.htwsaarland.config.mock;

import de.htwsaarland.config.ConfigEntries;
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
			new Entry() {
				@Override
				public String getName() {
					return ConfigMockGenerator.ARTIFICIAL_CONFIG;
				}
				{
					useIn.add(new ConfigUser(ArtificialGenConfigEntry.class.getName() , "an artificial description about the dummy-config" ) );
				}		
			}
		}
		;
	}
	
}
