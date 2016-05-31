package de.htwsaarland.config.mock;

import de.htwsaarland.config.annotation.NeedConfig;

/**
 *
 * @author hbui
 */
@NeedConfig(
	name =  ConfigMockGenerator.DUMMY_CONFIG,
	description = "an other description about the dummy-config",
	sugguestValues = "Quatch!"
)
@NeedConfig(
	name = OtherConfigMockGenerator.OTHER_DUMMY_CONFIG,
	description = "a description of an other dummy config"
)
public class OtherConfigMockGenerator {
	public static final String OTHER_DUMMY_CONFIG = "other-dummy-config";
}
