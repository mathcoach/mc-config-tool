package de.htwsaarland.config;

import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author hbui
 */
public interface ConfigEntries {
	Entry[] getEntry();
	
	public abstract static class Entry implements Comparable<Entry>{
		protected Set<ConfigUser> useIn = new TreeSet<>();
		protected Set<String> suggestValue = new TreeSet<>();
		private String value;
		
		/**
		 * name der configuration-parameter
		 */
		public abstract String getName();
		
		/**
		 * set the value of this configuration parameter.
		 */
		public Entry setValue(String value){
			this.value = value; 
			return this;
		}
		
		/**
		 * get the value of this configuration, will return null if the parameter
		 * is not configurated.
		 */
		public String getValue(){
			return this.value;
		}
		
		/**
		 * The set of @{ConfigUser}s, which use this configuration parameters.
		 */
		public Set<ConfigUser> useIn(){
			return this.useIn;
		}
		
		public Set<String> suggestValue(){
			return suggestValue;
		}
		
		public Entry addUseIn(String clazz, String description){
			useIn.add( new ConfigUser(clazz, description) );
			return this;
		}

		public Entry addSuggestValue(String value){
			suggestValue.add(value);
			return this;
		}
		
		public Entry addSuggestValue(SuggestValueCalculator cal){
			suggestValue.add(cal.calculate());
			return this;
		}
		
		@Override
		public final boolean equals (Object o){
			if (o instanceof Entry){
				return this.getName().equals(((Entry)o).getName());
			}else{
				return false;
			}
		}

		@Override
		public final int hashCode() {
			return getName().hashCode();
		}

		@Override
		public String toString(){
			StringBuilder b = new StringBuilder();
			b.append(getName()+"\n");
			b.append("  use in:\n");
			for (ConfigUser u : useIn){
				b.append("    "+u.name +" ---> " + u.description.replaceAll("\n+", " ") + "\n");
			}
			b.append("  suggest:\n");
			for (String s: suggestValue){
				b.append("    "+s+"\n");
			}
			return b.toString();
		}

		@Override
		public int compareTo(Entry o) {
			return getName().compareTo(o.getName());
		}
	}
	
	@FunctionalInterface
	public static interface SuggestValueCalculator{
		String calculate();
	}
	
	public static class ConfigUser implements Comparable<ConfigUser>{
		
		public final String name, description;
		
		public ConfigUser(String name, String description){
			this.name=name;
			this.description = description;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}
		

		@Override
		public boolean equals(Object o){
			if (o instanceof ConfigUser){
				return name.equals(((ConfigUser)o).name );
			}else{
				return false;
			}
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 89 * hash + name.hashCode();
			return hash;
		}

		@Override
		public int compareTo(ConfigUser o) {
			return name.compareTo(o.name);
		}
	}
}
