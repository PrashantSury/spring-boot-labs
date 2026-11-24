package ledgerly;

import java.util.function.Supplier;
import ledgerly.cycle.AccountService;
import ledgerly.cycle.StatementService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/** A finished demo you can run. NOT one of your tasks. See "Try it" in the README. */
public class InjectionTour {

  public static void main(String[] args) {
    System.out.println("Who gets which FeePolicy (tasks 1-2):");
    try (var ctx = new AnnotationConfigApplicationContext("ledgerly")) {
      show("FeePolicy beans              ", () -> String.join(",", ctx.getBeanNamesForType(FeePolicy.class)));
      show("standard transfer of 50000   ", () -> ctx.getBean(TransferService.class).transfer("ACC-1", "ACC-2", 50_000));
      show("premium transfer of 50000    ", () -> ctx.getBean(PremiumTransferService.class).transfer("ACC-1", "ACC-2", 50_000));
      System.out.println();
      System.out.println("All beans of a type (task 3):");
      show("channels                     ", () -> ctx.getBean(NotificationHub.class).channels());
      show("notifyAll                    ", () -> ctx.getBean(NotificationHub.class).notifyAll("Salary credited"));
      show("notifyVia emailNotifier      ", () -> ctx.getBean(NotificationHub.class).notifyVia("emailNotifier", "OTP 4821"));
      System.out.println();
      System.out.println("An optional bean (task 4):");
      show("allowed 50000                ", () -> ctx.getBean(LimitChecker.class).allowed("ACC-1", 50_000));
      show("allowed 250000               ", () -> ctx.getBean(LimitChecker.class).allowed("ACC-1", 250_000));
    } catch (Exception e) {
      System.out.println("context did not start: " + root(e));
    }
    System.out.println();
    System.out.println("A circular dependency (task 5):");
    try (var ctx = new AnnotationConfigApplicationContext(AccountService.class, StatementService.class)) {
      show("close ACC-1                  ", () -> ctx.getBean(AccountService.class).close("ACC-1"));
    } catch (Exception e) {
      System.out.println("context did not start: " + root(e));
    }
  }

  private static String root(Throwable e) {
    while (e.getCause() != null) {
      e = e.getCause();
    }
    return e.getClass().getSimpleName() + ": " + e.getMessage();
  }

  private static void show(String label, Supplier<Object> what) {
    try {
      System.out.println(label + what.get());
    } catch (Exception e) {
      System.out.println(label + e.getClass().getSimpleName() + ": " + e.getMessage());
    }
  }
}
