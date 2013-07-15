package org.mokai.ui.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to annotate a connector or a collection of connectors. Tells the UI to show a list of connectors.
 *
 * @author German Escobar
 */
@Documented
@Retention(value=RUNTIME)
@Target(value=FIELD)
@Inherited
public @interface ConnectorsList {

}
