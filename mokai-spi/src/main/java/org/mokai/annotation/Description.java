package org.mokai.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Allows a type (receiver, processor, action or acceptor) to have a description
 * that can be retrieved on runtime. Useful for showing information in GUI's.
 * 
 * @author German Escobar
 */
@Documented
@Retention(value=RUNTIME)
@Target(value=TYPE)
@Inherited
public @interface Description {

	String value();
	
}
