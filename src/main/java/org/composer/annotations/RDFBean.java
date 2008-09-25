package org.composer.annotations;

import org.composer.beans.DomainEntity;
import org.composer.beans.ContextEntity;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Sep 22, 2008
 * Time: 6:29:50 AM
 * To change this template use File | Settings | File Templates.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface RDFBean {
    Domain domain();
    Context context();
    String value();
}
