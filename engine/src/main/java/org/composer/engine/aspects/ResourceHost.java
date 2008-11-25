package org.composer.engine.aspects;

import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Aspect;
import org.composer.engine.annotations.DomainPath;
import org.composer.engine.annotations.Host;
import org.composer.engine.core.ResourceManager;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 19, 2008
 * Time: 1:47:11 PM
 * To change this template use File | Settings | File Templates.
 */
@Aspect
public class ResourceHost {

    @Pointcut("execution(* *(..)) && @annotation(host)")
    public void setResourceHost(Host host) {
    }

    @Before("setResourceHost(host)")
    public void setHost(Host host) {
        ResourceManager.setHost(host.value() );
    }
}
