package org.composer.aspects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

@Aspect
public class Logging {
            
    /**
	 * Write START and END markers to the debug log for all application method calls.
	 * Also write method execution time.
	 */

    /*
    @Around("within(org.composer..*) && !initialization(*.new(..))\n" +
            "          && !preinitialization(*.new(..))\n" +
            "          && !handler(*) {")
	public Object intercept(ProceedingJoinPoint pjp) throws Throwable {
        Class caller = pjp.getTarget().getClass();

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

	*/
}
