package ledgerly.app;

import java.util.function.Supplier;
import ledgerly.audit.AuditSink;
import ledgerly.audit.SiemForwarder;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** A finished demo you can run. NOT one of your tasks. See "Try it" in the README. */
public class AutoConfigTour {

  static final String CONFIG = "ledgerly.autoconfigure.LedgerlyAuditAutoConfiguration";

  public static void main(String[] args) {
    run("default");
    run("audit switched off", "--ledgerly.audit.enabled=false");
  }

  private static void run(String title, String... args) {
    System.out.println("== " + title);
    try (var ctx = new SpringApplicationBuilder(LedgerlyApplication.class).web(WebApplicationType.NONE).run(args)) {
      show("audit sink           ", () -> ctx.getBean(AuditSink.class).write("login ACC-1"));
      show("siem forwarder       ", () -> ctx.getBean(SiemForwarder.class).forward("login ACC-1"));
      show("config class         ", () -> ConditionsReader.outcome(ctx, CONFIG));
      show("auditSink method     ", () -> ConditionsReader.outcome(ctx, CONFIG + "#auditSink"));
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
