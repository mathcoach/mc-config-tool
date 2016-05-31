package de.htwsaarland.config.mock;

import de.htwsaarland.config.annotation.NeedConfig;

/**
 *
 * @author hbui
 */
@NeedConfig(
	name = ConfigMockGenerator.DUMMY_CONFIG,
	description = "a description about the dummy-config",
	sugguestValues = "Nothing"
)
@NeedConfig(
	name = "artificial-dummy-config"
)
public class ConfigMockGenerator {
	public static final String DUMMY_CONFIG = "dummy-config";
	public static final String ARTIFICIAL_CONFIG = "artificial-dummy-config";
}
