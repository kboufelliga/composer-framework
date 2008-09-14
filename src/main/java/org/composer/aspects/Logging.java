package org.composer.aspects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Aspect
public class Logging {

	/**
	 * Write START and END markers to the debug log for all application method calls.
	 * Also write method execution time.
	 */
	@Around("within(org.composer..*)")
	public Object doStartEndLogging(ProceedingJoinPoint pjp) throws Throwable {

		Log log = LogFactory.getLog(pjp.getTarget().getClass());
		String methodName = pjp.getSignature().getName();

		log.debug("START " + methodName);

		long time = System.currentTimeMillis();
		Object retVal = pjp.proceed();
		time = System.currentTimeMillis() - time;

		log.debug("END " + methodName);
		log.debug(methodName + "() execution time was " + time + "ms");

		return retVal;
	}
}
