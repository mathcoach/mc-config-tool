package de.htwsaarland.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 *
 * @author hbui
 */
@Repeatable(NeedConfigs.class)
@Target(ElementType.TYPE)
public @interface NeedConfig {
	String name ();
	String [] description() default {};
	String [] sugguestValues() default {""};
}
