package org.composer.aspects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.Before;
import org.composer.core.ResourceManager;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 16, 2008
 * Time: 4:15:37 PM
 * To change this template use File | Settings | File Templates.
 */
@Aspect
public class ResourceDomain {

    @Pointcut("execution(* *(..)) && @annotation(domain)")
    public void setResourceDomain(org.composer.annotations.Domain domain) {
    }

    @Before("setResourceDomain(domain)")
    public void setDomain(org.composer.annotations.Domain domain) {
        if (domain.value().length == 2)
            ResourceManager.setDomain(domain.value()[0],domain.value()[1]);
        else if (domain.value().length == 1)
            ResourceManager.setDomain(domain.value()[0]);
        else          
            ResourceManager.setDomain(domain.name(),domain.uri() );
    }
}
