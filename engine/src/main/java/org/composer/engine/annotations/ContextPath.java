package org.composer.engine.annotations;

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
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Inherited
public @interface ContextPath {
	String value() default "";
}
