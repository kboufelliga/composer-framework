package org.composer.annotations;

import org.composer.interfaces.Classification;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Sep 21, 2008
 * Time: 10:28:38 PM
 * To change this template use File | Settings | File Templates.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.METHOD,
        ElementType.PARAMETER
        })
@Inherited
public @interface Context {
	String value() default "semantic framework";
}
