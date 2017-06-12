package de.htwsaar.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Markiert eine Klasse
 *
 * @author hbui
 * @version $Id: $Id
 */
@Target(ElementType.TYPE)
public @interface NeedConfigs {
	NeedConfig[] value();
}
