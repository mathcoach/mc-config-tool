package de.htwsaar.config;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import org.openide.util.Lookup;

/**
 * collects all configuration entries in classpath using Lookup.
 * @author hbui
 */
public final class ConfigCollector {
	private ConfigCollector(){
		
	}

	/**
	 * @deprecated use {@link ConfigCollector#collectConfigAsMap() }
	 * prefer to use ConfigCollector.collectConfigAsMap
	 */
	@Deprecated
	public static final Collection<ConfigEntries.Entry> collectConfig(){
		return collectConfigAsMap().values();
	}	

	public static final Map<String,ConfigEntries.Entry> collectConfigAsMap(){
		Map<String,ConfigEntries.Entry> configs = new TreeMap<>();
		
		Collection<? extends ConfigEntries> configLookups
				= Lookup.getDefault().lookupAll(ConfigEntries.class);
		configLookups.stream().map( e  -> e.getEntry()).forEach( entries  -> {
			for(ConfigEntries.Entry entry : entries){
				ConfigEntries.Entry inMapEntry = configs.get(entry.getName());
				if (inMapEntry == null){
					configs.put(entry.getName(), entry);
				}else{ //merge it
					entry.useIn().forEach( s  -> inMapEntry.addUseIn(s.name, s.description));
					entry.suggestValue().forEach( s  -> inMapEntry.addSuggestValue(s));
				}
			}
		});
		return configs;
	}	
}
