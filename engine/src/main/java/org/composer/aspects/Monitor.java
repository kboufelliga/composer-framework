package org.composer.aspects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.composer.annotations.Domain;
import org.composer.annotations.Context;
import org.composer.core.ResourceManager;

import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.util.Iterator;

@Aspect
public class Monitor {
            
    /**
	 * Write START and END markers to the debug log for all application method calls.
	 * Also write method execution time.
	 */
	@Around("within(org.composer..*)")
	public Object intercept(ProceedingJoinPoint pjp) throws Throwable {

        Class caller = pjp.getTarget().getClass();
        Domain domain = null;
        Context context = null;

        // Monitor Domain and Context Settings
        for (Annotation annotation: caller.getAnnotations()) {
            System.out.println("found annotations: "+annotation.annotationType().toString());
            if (caller.isAnnotationPresent(Context.class)) {
                context = (Context) caller.getAnnotation(Context.class);
                System.out.println("context value: "+context.value());

                ResourceManager.setContext(context.value());
            }

            if (caller.isAnnotationPresent(Domain.class)) {
                domain = (Domain) caller.getAnnotation(Domain.class);

                ResourceManager.setContext(domain.value());
            }
        }

        // get the name and modifier to match the overides
        // there is probably a better way of doing this
        Signature signature = pjp.getSignature();

        for (Method method: caller.getMethods()) {
            if (method.getName().equals(signature.getName()) && method.getModifiers() == signature.getModifiers()) {
                if (method.isAnnotationPresent(Context.class)) {
                    context = (Context) method.getAnnotation(Context.class);

                    ResourceManager.setContext(context.value());
                }

                if (method.isAnnotationPresent(Domain.class)) {

                    ResourceManager.setDomain(domain.value());
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
