package de.htwsaar.config.demo;

import de.htwsaar.config.ConfigEntries;

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
			new Entry("example-config")
				.addUseIn("de.htwsaarland.laplus", "NA")
				.addSuggestValue("WEB-INF/virtua-file-system")
			,
		};
	}
	
}
