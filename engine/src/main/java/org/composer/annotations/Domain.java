package org.composer.annotations;

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
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Inherited
public @interface Domain {
	String[] value() default {};
    String name() default "domain";
    String uri() default "/domain";

}
