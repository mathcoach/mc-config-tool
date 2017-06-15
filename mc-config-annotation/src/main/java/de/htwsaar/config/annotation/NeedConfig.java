package de.htwsaar.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author hbui
 */
@Repeatable(NeedConfigs.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NeedConfig {
	String name ();
	String [] description() default {};
	String [] sugguestValues() default {""};
}
