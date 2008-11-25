package org.composer.engine.aspects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.Before;
import org.composer.engine.core.ResourceManager;
import org.composer.engine.annotations.DomainPath;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 16, 2008
 * Time: 4:15:37 PM
 * To change this template use File | Settings | File Templates.
 */
@Aspect
public class ResourceDomain {

    @Pointcut("execution(* *(..)) && @annotation(domainPath)")
    public void setResourceDomain(DomainPath domainPath) {
    }

    @Before("setResourceDomain(domainPath)")
    public void setDomainPath(DomainPath domainPath) {
        ResourceManager.setDomainPath(domainPath.value() );
    }
}
