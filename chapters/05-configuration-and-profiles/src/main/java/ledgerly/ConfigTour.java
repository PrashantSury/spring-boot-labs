package ledgerly;

import java.util.function.Supplier;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** A finished demo you can run. NOT one of your tasks. See "Try it" in the README. */
public class ConfigTour {

  public static void main(String[] args) {
    run("no profile", new String[0]);
    run("dev", new String[0], "dev");
    run("prod", new String[0], "prod");
    run("prod + command line", new String[] {"--ledgerly.transfer.daily-limit=150000"}, "prod");
  }

  private static void run(String title, String[] args, String... profiles) {
    System.out.println("== " + title);
    try (var ctx = new SpringApplicationBuilder(LedgerlyApplication.class)
        .web(WebApplicationType.NONE).profiles(profiles).run(args)) {
      EnvironmentReport env = ctx.getBean(EnvironmentReport.class);
      show("bank name        ", () -> ctx.getBean(BranchInfo.class).bankName());
      show("support phone    ", () -> ctx.getBean(BranchInfo.class).supportPhone());
      show("database url     ", () -> env.value("ledgerly.database.url"));
      show("transfer rules   ", () -> ctx.getBean(TransferProperties.class));
      show("sms gateway      ", () -> ctx.getBean(SmsGateway.class).getClass().getSimpleName());
      show("daily-limit from ", () -> env.whereFrom("ledgerly.transfer.daily-limit"));
    } catch (Exception e) {
      Throwable root = e;
      while (root.getCause() != null) {
        root = root.getCause();
      }
      System.out.println("APPLICATION FAILED TO START: " + root.getMessage());
    }
    System.out.println();
  }

  private static void show(String label, Supplier<Object> what) {
    try {
      System.out.println(label + what.get());
    } catch (Exception e) {
      System.out.println(label + e.getClass().getSimpleName() + ": " + e.getMessage());
    }
  }
}
