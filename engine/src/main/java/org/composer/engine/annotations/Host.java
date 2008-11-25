package org.composer.engine.annotations;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 19, 2008
 * Time: 1:46:17 PM
 * To change this template use File | Settings | File Templates.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Inherited
public @interface Host {
    String value() default "http://composerlab.org";
}
