package ledgerly;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/** A finished demo you can run. NOT one of your tasks. See "Try it" in the README. */
public class LifecycleTour {

  public static void main(String[] args) {
    LifecycleLog.clear();
    var ctx = new AnnotationConfigApplicationContext("ledgerly");
    System.out.println("USD rate                  " + ctx.getBean(RateCache.class).rate("USD"));
    ctx.getBean(SessionRegistry.class).open("ACC-1");
    ctx.getBean(SessionRegistry.class).open("ACC-2");
    StatementPrinter printer = ctx.getBean(StatementPrinter.class);
    System.out.println("printer class             " + printer.getClass().getSimpleName());
    System.out.println("printer is a proxy?       " + AopUtils.isAopProxy(printer));
    printer.statement("ACC-1");
    CallCounter c = ctx.getBean(CallCounter.class);
    System.out.println("after statement(): statement=" + c.count("statement") + " line=" + c.count("line"));
    printer.line("ACC-1", 100);
    System.out.println("after line():      statement=" + c.count("statement") + " line=" + c.count("line"));
    System.out.println("-- closing the context --");
    ctx.close();
    System.out.println("Lifecycle log:");
    LifecycleLog.lines().forEach(l -> System.out.println("  " + l));
  }
}
