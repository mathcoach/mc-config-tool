package de.htwsaar.config;

import static de.htwsaar.config.EnvConfiguration.resolveConfigVariables;
import static de.htwsaar.config.EnvConfiguration.resolveImportConfig;
import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author hbui
 */
public final class FileBasedConfig implements EnvConfiguration {

    private final LinkedHashMap<String, String> configTable = new LinkedHashMap<>();

    public FileBasedConfig(Path configPath) {
        File cf = configPath.toFile();
        Map<String, String> configWithResolvedImport = resolveImportConfig(cf, ConfigParserFactory.getParserForFile(cf));
        Map<String, String> configWithResolvedVariables = resolveConfigVariables(configWithResolvedImport);
        configTable.putAll(configWithResolvedVariables);
    }

    @Override
    public String getConfigValue(String configParameter) {
        return configTable.get(configParameter);
    }

    @Override
    public Set<String> getAllConfigKeys() {
        return configTable.keySet();
    }

}
