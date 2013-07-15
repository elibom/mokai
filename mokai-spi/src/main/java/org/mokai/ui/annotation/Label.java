package org.mokai.ui.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies a custom label for the field.
 *
 * @author German Escobar
 */
@Documented
@Retention(value=RUNTIME)
@Target(value=FIELD)
@Inherited
public @interface Label {

	String value();

}
