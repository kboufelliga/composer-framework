package org.composer.aspects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.composer.annotations.Domain;
import org.composer.annotations.Context;
import org.composer.core.Manager;
import org.composer.interfaces.Entity;
import org.composer.beans.ContextEntity;
import org.composer.beans.DomainEntity;

@Aspect
public class Logging {

    Manager manager = Manager.getInstance();
    /**
	 * Write START and END markers to the debug log for all application method calls.
	 * Also write method execution time.
	 */
	@Around("within(org.composer..*)")
	public Object doStartEndLogging(ProceedingJoinPoint pjp) throws Throwable {

        Class caller = pjp.getTarget().getClass();

        Log log = LogFactory.getLog(caller);
		String methodName = pjp.getSignature().getName();

		log.info("START " + methodName);

		long time = System.currentTimeMillis();
		Object retVal = pjp.proceed();
		time = System.currentTimeMillis() - time;

		log.info("END " + methodName);
		log.info(methodName + "() execution time was " + time + "ms");


                Domain domain = null;
                Context context = null;

                if (caller.getMethod(pjp.getSignature().getName()).isAnnotationPresent(Domain.class)) {
                    domain = (Domain) caller.getMethod(pjp.getSignature().getName()).getAnnotation(Domain.class);

                    manager.setDomain(new DomainEntity(domain.name()));
                }

                if (caller.getMethod(pjp.getSignature().getName()).isAnnotationPresent(Context.class)) {
                    context = (Context) caller.getMethod(pjp.getSignature().getName()).getAnnotation(Context.class);

                    manager.setContext(new ContextEntity(context.name()));
                }

                if (domain != null)
                    System.out.println("domain name: "+domain.name());

                if (context != null)
                    System.out.println("context name: "+context.name());

        
        return retVal;
	}
}
