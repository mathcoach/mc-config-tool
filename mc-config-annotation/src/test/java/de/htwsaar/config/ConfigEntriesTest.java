package de.htwsaar.config;

import static de.htwsaar.config.ConfigEntries.*;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import static org.fest.assertions.api.Assertions.*;

/**
 *
 * @author hbui
 */
public class ConfigEntriesTest {
	// Test part for Entry
	@Test
	public void useIn_is_Imutable(){
		final String configUser = "de.htwsaar.config.testing";
		final String configDescription = "a test value";
		Entry entry = new Entry("config-parmeter");
		entry.addUseIn(configUser, configDescription);
		
		//"destroy" useIn
		entry.useIn().clear();
		assertThat(entry.useIn())
				.containsOnly(new ConfigUser(configUser, configDescription));
	}
	@Test
	public void suggestValue_is_Imutable(){
		Entry entry = new Entry("config-parmeter");
		String suggestValue = "1. a posible value";
		final String otherSuggestValue = "2. an other posible value";
		final String thirdSuggestValue = "3. posible value";
		entry.addSuggestValue(suggestValue)
				.addSuggestValue(() -> thirdSuggestValue)
				.addParametrizedSuggestValue( otherSuggestValue, (s) -> s.toString() );
		
		// destroy suggestValue
		entry.suggestValue().clear();
		assertThat(entry.suggestValue())
				.containsOnly(suggestValue, otherSuggestValue, thirdSuggestValue);
		
	}
	
	@Test
	public void onlyConfigNameIsMatterByComparision(){
		String sameConfig = "sameconfig";
		String otherConfig = "otherConfig";
		//using compare
		Set<Entry> configEntries = new TreeSet<>();
		configEntries.add( (new Entry(sameConfig)).setValue("value 1") );
		configEntries.add( (new Entry(sameConfig)).setValue("value 2") );
		configEntries.add( (new Entry(otherConfig)).setValue("value 3") );
		
		assertThat(configEntries).hasSize(2);
		
		// using equals
		Set<Entry> configEntries2 = new HashSet<>();
		configEntries2.add( (new Entry(sameConfig)).setValue("value 1") );
		configEntries2.add( (new Entry(sameConfig)).setValue("value 2") );
		configEntries2.add( (new Entry(otherConfig)).setValue("value 3") );
		
		assertThat(configEntries).hasSize(2);
	}
	
	@Test
	public void noExceptionByCallingToString(){
		try{
			Entry entry = new Entry("test-to-string")
					.setValue("a value")
					.addSuggestValue("value 1").addSuggestValue("value 2")
					.addUseIn("de.test.abc", "just a user")
					.addUseIn("de.test.xyz", "just an other user");
			System.out.println( entry.toString() );
		}catch(Exception ex){
			fail("Not expected any exeption but got " + ex.getClass(), ex);
		}
	}

	@Test
	public void equalityConcernOnlyName() {
		String entryName = "test-entry-name";
		Entry e1 = new Entry(entryName).setValue("e1");
		Entry e2 = new Entry(entryName).setValue("e2");
		assertThat(e1).isEqualTo(e2);
	}

	@Test
	public void notEqualityConcernOnlyName() {
		String entryName = "test-entry-name";
		Entry e1 = new Entry(entryName).setValue("e1");
		Entry e2 = new Entry(entryName+ "-extra").setValue("e1");
		assertThat(e1).isNotEqualTo(e2);
	}

	@Test
	public void compareBadType() {
		String entryName = "test-entry-name";
		Entry e1 = new Entry(entryName).setValue("e1");
		assertThat(e1.equals(entryName)).isFalse();
		assertThat(e1.getName()).isEqualTo(entryName);
	}
	
}
