package ledgerly;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Task 1: time EVERY method of TransferService without touching TransferService.
 *   1. Make this class an aspect and a bean: @Aspect and @Component.
 *   2. Put @Around("execution(* ledgerly.TransferService.*(..))") on time(...).
 *   3. In time(...): note System.nanoTime(), call pjp.proceed(), and in a finally block
 *      record timings.record(pjp.getSignature().getName(), elapsedNanos). Return what proceed() returned.
 *      A failed call is timed too, and its exception must still reach the caller.
 */
public class TimingAspect {

  private final Timings timings;

  public TimingAspect(Timings timings) {
    this.timings = timings;
  }

  public Object time(ProceedingJoinPoint pjp) throws Throwable {
    return pjp.proceed();
  }
}
