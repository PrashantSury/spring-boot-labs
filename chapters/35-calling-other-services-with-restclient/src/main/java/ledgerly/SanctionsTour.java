package ledgerly;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Starts the fake partner on port 8097 and walks through every case (mvn spring-boot:run). Finished. */
@Component
@ConditionalOnProperty(name = "ledgerly.tour", havingValue = "true", matchIfMissing = true)
public class SanctionsTour implements CommandLineRunner {

  private final SanctionsClient sanctions;

  public SanctionsTour(SanctionsClient sanctions) {
    this.sanctions = sanctions;
  }

  @Override
  public void run(String... args) {
    FakeSanctions partner = new FakeSanctions(8097);
    try {
      step("screen Acme Traders     ", () -> sanctions.screen("Acme Traders"));
      step("screen Evil Corp        ", () -> sanctions.screen("Evil Corp"));
      partner.failNext(2);
      step("screen after two 503s   ", () -> sanctions.screen("Acme Traders"));
      step("screen ''               ", () -> sanctions.screen(""));
      step("screen Slow Co          ", () -> sanctions.screen("Slow Co"));
      step("report TX-1             ", () -> {
        sanctions.report("TX-1", "Acme Traders");
        return "accepted";
      });
      partner.failNext(1);
      step("report TX-2 during a 503", () -> {
        sanctions.report("TX-2", "Acme Traders");
        return "accepted";
      });
      System.out.println("partner saw:");
      partner.log().forEach(l -> System.out.println("  " + l));
    } finally {
      partner.stop();
    }
  }

  private static void step(String label, java.util.function.Supplier<Object> call) {
    long t = System.nanoTime();
    String out;
    try {
      out = String.valueOf(call.get());
    } catch (RuntimeException e) {
      out = e.getClass().getSimpleName() + ": " + e.getMessage();
    }
    System.out.printf("%s -> %s  (%d ms)%n", label, out, (System.nanoTime() - t) / 1_000_000);
  }
}
