package org.mokai.ui.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to annotate an acceptor or a collection of acceptors. Tells the UI to show a list of acceptors.
 *
 * @author German Escobar
 */
@Documented
@Retention(value=RUNTIME)
@Target(value=FIELD)
@Inherited
public @interface AcceptorsList {

}
