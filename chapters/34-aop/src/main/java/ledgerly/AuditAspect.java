package ledgerly;

import java.util.Arrays;
import org.aspectj.lang.JoinPoint;

/**
 * Tasks 2 and 3: write an audit line for every call of a method marked @Audited.
 * Make this class an aspect and a bean (@Aspect, @Component).
 */
public class AuditAspect {

  private final AuditTrail trail;

  public AuditAspect(AuditTrail trail) {
    this.trail = trail;
  }

  // Task 2: after a SUCCESSFUL call of any @Audited method:
  //   @AfterReturning(pointcut = "@annotation(audited)", returning = "result")
  //   line = "OK " + audited.value() + " " + Arrays.toString(jp.getArgs()) + " -> " + result
  //   e.g. "OK transfer [ACC-1, ACC-2, 100] -> TX-1"
  public void succeeded(JoinPoint jp, Audited audited, Object result) {
  }

  // Task 3: after a FAILED call (the exception must still reach the caller, so don't catch it):
  //   @AfterThrowing(pointcut = "@annotation(audited)", throwing = "ex")
  //   line = "FAILED " + audited.value() + " " + Arrays.toString(jp.getArgs()) + ": " + ex.getMessage()
  //   e.g. "FAILED transfer [ACC-1, ACC-2, 5000]: insufficient funds in ACC-1"
  public void failed(JoinPoint jp, Audited audited, Exception ex) {
  }
}
