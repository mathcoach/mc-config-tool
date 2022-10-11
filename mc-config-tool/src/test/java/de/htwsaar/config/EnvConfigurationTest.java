package de.htwsaar.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.data.MapEntry;
import org.junit.jupiter.api.BeforeAll;

/**
 *
 * @author hbui
 */
class EnvConfigurationTest {

    @Test
    void resolveHOME_Variable() {
        String pathWithHomeVar = "${HOME}/mypath/test.xml";
        String resolveSystemProperties = EnvConfiguration.resolveSystemProperties(pathWithHomeVar);
        String home = System.getProperty("user.home");
        assertThat(resolveSystemProperties)
                .startsWith(home)
                .endsWith("/mypath/test.xml")
                .hasSize(home.length() + "/mypath/test.xml".length());
    }

    @Test
    void resolveHOME_Variable2() {
        String pathWithHomeVar = "$HOME/mypath/test.xml";
        String resolveSystemProperties = EnvConfiguration.resolveSystemProperties(pathWithHomeVar);
        String home = System.getProperty("user.home");
        assertThat(resolveSystemProperties)
                .startsWith(home)
                .endsWith("/mypath/test.xml")
                .hasSize(home.length() + "/mypath/test.xml".length());
    }

    @Test
    void resolveVariables() {
        Map<String, String> originConfig = new HashMap<String, String>() {
            {
                put("a", "A");
                put("b", "${a}");
            }
        };
        Map<String, String> config = EnvConfiguration.resolveConfigVariables(originConfig);
        assertThat(config).hasSameSizeAs(originConfig.entrySet())
                .containsEntry("a", "A")
                .containsEntry("b", "A");
    }

    @Test
    void resolveHome_Variable2() {
        Map<String, String> originConfig = new HashMap<String, String>() {
            {
                put("myapp.base", "${HOME}/myapp");
                put("myapp.data", "${myapp.base}/data");
                put("myapp.conf", "${myapp.base}/conf");
            }
        };
        Map<String, String> config = EnvConfiguration.resolveConfigVariables(originConfig);
        String base = System.getProperty("user.home") + "/" + "myapp";
        String data = base + "/data";
        String conf = base + "/conf";
        assertThat(config).hasSameSizeAs(originConfig)
                .containsEntry("myapp.base", base)
                .containsEntry("myapp.data", data)
                .containsEntry("myapp.conf", conf);
    }

    @Test
    void detectCycleInVariableResolution() {
        Map<String, String> originConfig = new HashMap<String, String>() {
            {
                put("a", "A");
                put("b", "${a}");

                put("c", "${d}");
                put("d", "${e}");
                put("e", "${c}");
            }
        };
        try {
            EnvConfiguration.resolveConfigVariables(originConfig);
        } catch (LSConfigException ex) {
            assertThat(ex).hasMessageContaining("Infinite loop in property interpolation of ${d}");
        }
    }

    @Test
    void resolveSystemVar() {
        Map<String, String> originConfig = new HashMap<String, String>() {
            {
                put("home", "${HOME}");
                put("working-dir", "${user.dir}");
            }
        };
        Map<String, String> config = EnvConfiguration.resolveConfigVariables(originConfig);
        assertThat(config).hasSameSizeAs(originConfig.entrySet())
                .contains(MapEntry.entry("home", System.getProperty("user.home")))
                .contains(MapEntry.entry("working-dir", System.getProperty("user.dir")));
    }

    @Test
    void doNotSetNewConfigIfVarNotSolved() {
        Map<String, String> originConfig = new HashMap<String, String>() {
            {
                put("param-a", "a");
                put("pram-b", "b");
            }
        };
        try {
            EnvConfiguration.setConfigValue("new-param", "string mit var ${not-def}", originConfig);
        } catch (LSConfigException ex) {
            assertThat(ex).hasMessageContaining("${not-def}")
                    .hasMessageContaining("Cannot find");
            assertThat(originConfig).doesNotContainKey("new-param");
        }
    }

    @Test
    void doNotSetNewConfigIfVarNotSolved2() {
        Map<String, String> originConfig = new HashMap<String, String>() {
            {
                put("param-a", "a");
                put("param-b", "b");
            }
        };
        try {
            EnvConfiguration.setConfigValue("new-param", "string mit var ${not-def} und var ${param-a}", originConfig);
        } catch (LSConfigException ex) {
            assertThat(ex).hasMessageContaining("${not-def}")
                    .hasMessageContaining("Cannot find");
            assertThat(originConfig).hasSize(2)
                    .doesNotContainKey("new-param")
                    .contains(MapEntry.entry("param-b", "b"))
                    .contains(MapEntry.entry("param-a", "a"));
        }
    }

    @Test
    void setNewConfigIfVarSolved() {
        Map<String, String> originConfig = new HashMap<String, String>() {
            {
                put("param-a", "a");
                put("param-b", "b");
            }
        };
        EnvConfiguration.setConfigValue("new-param", "use param-a |${param-a}|", originConfig);
        assertThat(originConfig)
                .contains(MapEntry.entry("param-a", "a"))
                .contains(MapEntry.entry("param-b", "b"))
                .contains(MapEntry.entry("new-param", "use param-a |a|"));
    }

    @Test
    void setNewConfigIfNoVarRef() {
        Map<String, String> originConfig = new HashMap<String, String>() {
            {
                put("param-a", "a");
                put("param-b", "b");
            }
        };
        EnvConfiguration.setConfigValue("new-param", "new-config", originConfig);
        assertThat(originConfig)
                .contains(MapEntry.entry("param-a", "a"))
                .contains(MapEntry.entry("param-b", "b"))
                .contains(MapEntry.entry("new-param", "new-config"));
    }

    static final Path IMPORT_CONFIG_FILE = Paths.get("./target/import-config.txt");// <= problematic with path
    static final Path MAIN_CONFIG_FILE = Paths.get("./target/main-config");

    @BeforeAll
    static void initDummyConfigFiles() throws IOException {
        //Path configFile = Paths.get(IMPORT_CONFIG_FILE);
        try {
            Files.createFile(IMPORT_CONFIG_FILE);
        } catch (FileAlreadyExistsException ex) {
            // Nothing to do
        }
    }

    @Test
    void parserAConfigFileWithImportRelativeFile() {

        File simpleConfig = MAIN_CONFIG_FILE.toFile();//Dummy file only
        Map<String, String> config = EnvConfiguration.resolveImportConfig(simpleConfig, new DummyConfigParser());

        assertThat(config)
                .containsEntry("param-a", "a") // only main config has param-a
                .containsEntry("import-param-a", "A") //only imported config has import-param-a
                .containsEntry("param-b", "B") //imported config has precedence
                .containsEntry("param-c", "c") //like param-a, other name
                ;
        assertThat(config.get("param-e")).isNull();              //neither nor is configed
    }

    class DummyConfigParser implements ConfigParser {

        final Map<String, String> masterConfig = new HashMap<String, String>() {
            {
                put(EnvConfiguration.IMPORT_KEY, IMPORT_CONFIG_FILE.toString());
                put("param-a", "a");
                put("param-b", "b");
                put("param-c", "c");
            }
        };

        final Map<String, String> importedConfig = new HashMap<String, String>() {
            {
                put("import-param-a", "A");
                put("param-b", "B");
            }
        };

        @Override
        public Map<String, String> parseConfigFile(File configFile) {
            Path normalizeConfig = configFile.toPath().normalize();
            Path normalizeMainCfg = MAIN_CONFIG_FILE.normalize();
            Path normalizeImportCfg = IMPORT_CONFIG_FILE.normalize();

            if (normalizeConfig.endsWith(normalizeMainCfg)) {
                return masterConfig;
            } else if (normalizeConfig.endsWith(normalizeImportCfg)) {
                return importedConfig;
            } else {
                throw new IllegalStateException(configFile.getAbsolutePath() + " not match " + normalizeMainCfg.toString() + " or " + normalizeImportCfg);
            }
        }

        @Override
        public Map<String, String> parseConfigFile(InputStream configFile) {
            throw new UnsupportedOperationException("Not supported yet."); 
        }
    }

    @Test
    void throwExceptionIfImportCycle() {
        File simpleConfig = MAIN_CONFIG_FILE.toFile();
        ConfigParser p = new CycleConfigParser();
        try {
            EnvConfiguration.resolveImportConfig(simpleConfig, p);
            failBecauseExceptionWasNotThrown(LSConfigException.class);
        } catch (LSConfigException ex) {
            assertThat(ex).hasMessageContaining("Import too many level");
        }
    }

    class CycleConfigParser implements ConfigParser {

        final Map<String, String> masterConfig = new HashMap<String, String>() {
            {
                put(EnvConfiguration.IMPORT_KEY, IMPORT_CONFIG_FILE.toString());
            }
        };

        final Map<String, String> importedConfig = new HashMap<String, String>() {
            {
                put(EnvConfiguration.IMPORT_KEY, MAIN_CONFIG_FILE.toString());
            }
        };

        @Override
        public Map<String, String> parseConfigFile(File configFile) {
            Path normalizeConfig = configFile.toPath().normalize();
            Path normalizeMainCfg = MAIN_CONFIG_FILE.normalize();
            Path normalizeImportCfg = IMPORT_CONFIG_FILE.normalize();

            if (normalizeConfig.endsWith(normalizeMainCfg)) {
                return masterConfig;
            } else if (normalizeConfig.endsWith(normalizeImportCfg)) {
                return importedConfig;
            } else {
                throw new IllegalStateException(configFile.getAbsolutePath() + " not match " + normalizeMainCfg.toString() + " or " + normalizeImportCfg);
            }
        }

        @Override
        public Map<String, String> parseConfigFile(InputStream configFile) {
            throw new UnsupportedOperationException("Not supported yet."); 
        }

    }

    @Test
    void useDefaultValueWhenConfigurationNotExist() {
        EnvConfiguration configuration = new DynamicConfig();
        String defaultValue = "myValue";
        String configValue = configuration.getConfigValue("not-exist-config", defaultValue);
        assertThat(configValue).isEqualTo(defaultValue);
    }

    @Test
    void useConfiguredValueWhenConfigurationExist() {
        String myConfig = "my-config";
        String myValue = "my-value";
        EnvConfiguration configuration = new DynamicConfig(
                new HashMap<String, String>() {
            {
                put(myConfig, myValue);
            }
        }
        );
        String defaultValue = "default-value";
        String configValue = configuration.getConfigValue(myConfig, defaultValue);
        assertThat(configValue).isEqualTo(myValue);
    }

    @Test
    void useHandlerWhenConfigurationNotExist() {
        EnvConfiguration configuration = new DynamicConfig();
        String defaultValue = "myValue";
        String configValue = configuration.getConfigValue("not-exist-config", (config) -> defaultValue);
        assertThat(configValue).isEqualTo(defaultValue);
    }

    @Test
    void use_Typed_HandlerWhenConfigurationNotExist() {
        String key = "count";
        String value = "  1  ";
        EnvConfiguration configuration = new DynamicConfig(
                new HashMap<String, String>() {
            {
                put(key, value);
            }
        }
        );

        int configValue = configuration.getConfigValue(key, (p, v) -> Integer.parseInt(v));
        assertThat(configValue).isEqualTo(1);
    }

    @Test
    void doNotCallHandlerWhenConfigurationExist() {
        String myConfig = "my-config";
        String myValue = "my-value";
        EnvConfiguration configuration = new DynamicConfig(
                new HashMap<String, String>() {
            {
                put(myConfig, myValue);
            }
        }
        );
        String defaultValue = "default-value";
        String configValue = configuration.getConfigValue(myConfig, (config) -> defaultValue);
        assertThat(configValue).isEqualTo(myValue);
    }

    @Test
    void useConverterToConvertStringToInt() {
        String myConfig = "my-config";
        String myValue = "5";
        EnvConfiguration configuration = new DynamicConfig(
                new HashMap<String, String>() {
            {
                put(myConfig, myValue);
            }
        }
        );
        int expectedValue = 5;
        int getValue = configuration.getConfigValue(myConfig, (key, value) -> {
            return Integer.parseInt(value);
        });
        assertThat(getValue).isEqualTo(expectedValue);
    }

    @Test
    void useConverterToHandleException() {
        String myConfig = "my-config";
        String myValue = "not-a-number";
        EnvConfiguration configuration = new DynamicConfig(
                new HashMap<String, String>() {
            {
                put(myConfig, myValue);
            }
        }
        );
        final int expectedValue = 1;
        BiFunction<String, String, Integer> fn = (key, value) -> {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                return 1;
            }
        };
        int getValue = configuration.getConfigValue(myConfig, fn);
        assertThat(getValue).isEqualTo(expectedValue);
    }

    @Test
    void raiseExceptionIfImportFileNotExist() {
        final String dummyNotExistFile = "dummy_not_exist_file";
        File ignoredCfgFile = new File("ignore_me");
        ConfigParser p = new ImportNotExistFileParser(dummyNotExistFile);
        try {
            EnvConfiguration.resolveImportConfig(ignoredCfgFile, p);
            failBecauseExceptionWasNotThrown(ImportCfgFileNotFound.class);
        } catch (ImportCfgFileNotFound ex) {
            assertThat(ex).hasMessageContaining(dummyNotExistFile);
            Path dummy = ex.getImportedPath();
            assertThat(dummy).doesNotExist();
        }
    }

    class ImportNotExistFileParser implements ConfigParser {

        Map<String, String> config;

        public ImportNotExistFileParser(String notExistFile) {
            config = Map.of(EnvConfiguration.IMPORT_KEY, notExistFile);
        }

        @Override
        public Map<String, String> parseConfigFile(File configFile) {
            // Ignore the config file
            return config;
        }

        @Override
        public Map<String, String> parseConfigFile(InputStream configFile) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
