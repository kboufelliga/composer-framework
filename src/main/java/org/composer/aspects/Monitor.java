package org.composer.aspects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.composer.annotations.Domain;
import org.composer.annotations.Context;
import org.composer.core.Manager;
import org.composer.interfaces.Entity;
import org.composer.beans.ContextEntity;
import org.composer.beans.DomainEntity;

import java.lang.reflect.Method;

@Aspect
public class Monitor {
    Manager manager = Manager.getInstance();
            
    /**
	 * Write START and END markers to the debug log for all application method calls.
	 * Also write method execution time.
	 */
	@Around("within(org.composer..*)")
	public Object intercept(ProceedingJoinPoint pjp) throws Throwable {

        Class caller = pjp.getTarget().getClass();

        // Monitor Domain and Context Settings

        Domain domain = null;
        Context context = null;

        // get the name and modifier to match the overwritten signatures
        // there is probably a better way of doing this
        Signature signature = pjp.getSignature();

        for (Method method: caller.getMethods()) {
            if (method.getName().equals(signature.getName()) && method.getModifiers() == signature.getModifiers()) {
                if (method.isAnnotationPresent(Context.class)) {
                    context = (Context) method.getAnnotation(Context.class);

                    manager.setContext(new ContextEntity(context.value()));
                }

                if (method.isAnnotationPresent(Domain.class)) {
                    domain = (Domain) method.getAnnotation(Domain.class);

                    manager.setDomain(new DomainEntity(domain.value()));
                }
            }
        }

        // Logging
        Log log = LogFactory.getLog(caller);
		String methodName = pjp.getSignature().getName();

		log.info("START " + methodName);

		long time = System.currentTimeMillis();
		Object retVal = pjp.proceed();
		time = System.currentTimeMillis() - time;

		log.info("END " + methodName);
		log.info(methodName + "() execution time was " + time + "ms");
        
        return retVal;
	}
}
