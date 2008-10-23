package org.composer.aspects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.Before;
import org.composer.core.ResourceManager;

import java.sql.Array;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 16, 2008
 * Time: 4:16:31 PM
 * To change this template use File | Settings | File Templates.
 */
@Aspect
public class ResourceContext {
    @Pointcut("execution(* *.*(..)) && @annotation(context)")
        public void setResourceContext(org.composer.annotations.Context context) {
    }

    @Before("setResourceContext(context)")
    public void setContext(org.composer.annotations.Context context) {
        if (context.value().length == 2)
            ResourceManager.setContext(context.value()[0],context.value()[1]);
        else if (context.value().length == 1)
                    ResourceManager.setDomain(context.value()[0]);
        else {
            ResourceManager.setContext(context.name(),context.uri());
        }
    }
}
