package ledgerly;

import java.util.function.Supplier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * A finished demo you can run to look inside the application context.
 * NOT one of your tasks. See "Try it" in the chapter README.
 */
public class BeanTour {

  public static void main(String[] args) {
    try (var ctx = new AnnotationConfigApplicationContext("ledgerly")) {
      System.out.println("Beans in the context (Spring's own are hidden):");
      for (String name : ctx.getBeanDefinitionNames()) {
        if (!name.startsWith("org.springframework")) {
          System.out.println("  " + name + "  ->  " + ctx.getType(name).getSimpleName().replaceAll("\\$\\$.*", ""));
        }
      }

      System.out.println();
      show("auditLog twice, same object?   ",
          () -> ctx.getBean(AuditLog.class) == ctx.getBean(AuditLog.class));
      show("otpToken twice, same object?   ",
          () -> ctx.getBean(OtpToken.class) == ctx.getBean(OtpToken.class));
      show("a token value                  ", () -> ctx.getBean(OtpToken.class).value());
      show("rupeeFormat formats 52000      ", () -> ctx.getBean("rupeeFormat",
          java.text.NumberFormat.class).format(52000));
      show("branchCodes                    ", () -> ctx.getBean("branchCodes"));
      System.out.println();
      System.out.println("Your tasks (these print an error until the task is done):");
      show("feeCalculator                  ", () -> ctx.getBean(FeeCalculator.class).feeFor(5000));
      show("bankClock zone                 ", () -> ctx.getBean("bankClock", java.time.Clock.class).getZone());
      show("the InterestPolicy             ", () -> ctx.getBean(InterestPolicy.class));
      show("receipt twice, same object?    ",
          () -> ctx.getBean(Receipt.class) == ctx.getBean(Receipt.class));
    }
  }

  private static void show(String label, Supplier<Object> what) {
    try {
      System.out.println(label + what.get());
    } catch (Exception e) {
      System.out.println(label + e.getClass().getSimpleName() + ": " + e.getMessage());
    }
  }
}
