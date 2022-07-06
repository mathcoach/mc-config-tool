package de.htwsaar.config.processor;

import static de.htwsaar.config.processor.PackageNameHandling.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hbui
 */
class PackageNameHandlingTest {
       
    final StdOutLogWriter lw = new StdOutLogWriter();
    
    @Test
    void testDropPartsOfName() {
        final List<String> packages = new ArrayList<>( Arrays.asList("de", "htwsaar", "mc") );
        final String name = "de.htwsaar.mc.somethingelse.MyTestingClass";
        updatePackage(packages, name, lw);
        assertThat(packages).containsExactly("de", "htwsaar", "mc");
    }

    @Test
    void testDropPartsOfCurrentPackages() {
        final List<String> packages = new ArrayList<>( Arrays.asList("de", "htwsaar", "mc", "somethingelse") );
        final String name = "de.htwsaar.mc.MyTestingClass";
        updatePackage(packages, name, lw);
        assertThat(packages).containsExactly("de", "htwsaar", "mc");
    }
    
    @Test
    void testTakeOnlyCommonPars() {
        final List<String> packages = new ArrayList<>( Arrays.asList("de", "htwsaar", "mc", "somethingelse") );
        final String name = "de.htwsaar.mc.otherting.MyTestingClass";
        updatePackage(packages, name, lw);
        assertThat(packages).containsExactly("de", "htwsaar", "mc");
    }


    @Test
    void testBuildFinalPackageNameWithoutConfig() {
        final List<String> packages = new ArrayList<>( Arrays.asList("de", "htwsaar", "mc", "somethingelse") );
        String configPackageName = null;
        String finalPackageName = buildPackage(packages, configPackageName, lw);
        assertThat(finalPackageName).isEqualTo("de.htwsaar.mc.somethingelse");
    }
    
    @Test
    void testBuildFinalPackageNameWithEmptyConfig() {
        final List<String> packages = new ArrayList<>( Arrays.asList("de", "htwsaar", "mc", "somethingelse") );
        String configPackageName = "    ";
        String finalPackageName = buildPackage(packages, configPackageName, lw);
        assertThat(finalPackageName).isEqualTo("de.htwsaar.mc.somethingelse");
    }

    final class StdOutLogWriter implements LogWriter {

        @Override
        public void error(CharSequence msg) {
            System.err.println("  Error: " + msg);
        }

        @Override
        public void warn(CharSequence msg) {
            System.err.println("   Warn: " + msg);
        }

        @Override
        public void info(CharSequence msg) {
            System.err.println("   Info: " + msg);
        }

        @Override
        public void debug(CharSequence msg) {
            System.err.println("   Debug: " + msg);
        }
        
    }
    
}
