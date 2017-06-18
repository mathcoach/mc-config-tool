package de.htwsaar.config;

import de.htwsaar.config.annotation.NeedConfig;

/**
 *
 * @author hbui
 */
@NeedConfig(
	name = ConfigMockGenerator.DUMMY_CONFIG,
	description = "a description \"about\" the dummy-config",
	sugguestValues = "No\thing"
)
@NeedConfig(
	name = ConfigMockGenerator.ARTIFICIAL_CONFIG
)
public class ConfigMockGenerator {
	public static final String DUMMY_CONFIG = "dummy-config";
	public static final String ARTIFICIAL_CONFIG = "artificial-dummy-config";
}
