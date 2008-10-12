package org.composer.annotations;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 7, 2008
 * Time: 10:40:00 AM
 * To change this template use File | Settings | File Templates.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.METHOD,
        ElementType.PARAMETER
        })
@Inherited
public @interface Import {
    String value();
}
