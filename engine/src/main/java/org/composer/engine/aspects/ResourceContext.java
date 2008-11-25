package org.composer.engine.aspects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.Before;
import org.composer.engine.core.ResourceManager;
import org.composer.engine.annotations.ContextPath;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 16, 2008
 * Time: 4:16:31 PM
 * To change this template use File | Settings | File Templates.
 */
@Aspect
public class ResourceContext {
    @Pointcut("execution(* *.*(..)) && @annotation(contextPath)")
        public void setResourceContext(ContextPath contextPath) {
    }

    @Before("setResourceContext(contextPath)")
    public void setContextPath(ContextPath contextPath) {
        ResourceManager.setContextPath(contextPath.value());
    }
}
