package de.htwsaarland.config;

import de.htwsaarland.config.ConfigEntries;
//use in generated file: import org.openide.util.lookup.ServiceProvider;

/**
 * Diese Klass demonstriert, wie man die ConfigEntry generieren lassen kann.
 * @author hbui
 */

//use in generated file: @ServiceProvider(
//		service = ConfigEntries.class)
public class ExampleConfigEntriesImp implements ConfigEntries{

	@Override
	public Entry[] getEntry() {
		return new Entry[]{
			new Entry() {
				@Override
				public String getName() {
					return "author-root";
				}
				
				{
//					
					useIn.add(new ConfigUser("de.htwsaarland.laplus", "NA"));
//
					suggestValue.add("WEB-INF/virtua-file-system");
				}		
				
				
			},
		};
	}
	
}
