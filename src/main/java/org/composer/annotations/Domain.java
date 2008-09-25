package org.composer.annotations;

import org.composer.interfaces.Entity;
import org.composer.interfaces.Classification;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Sep 21, 2008
 * Time: 9:51:59 PM
 * To change this template use File | Settings | File Templates.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.METHOD,
        ElementType.PARAMETER
        })
@Inherited
public @interface Domain {
	String value() default "composer-lab";
}
