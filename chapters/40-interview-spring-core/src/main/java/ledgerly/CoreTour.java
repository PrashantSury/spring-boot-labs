package ledgerly;

import dev.ledgerly.fx.FxRates;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** Prints one line per topic (mvn spring-boot:run, add -Dspring-boot.run.profiles=prod to try the profile). Finished. */
@Component
@ConditionalOnProperty(name = "ledgerly.tour", havingValue = "true", matchIfMissing = true)
public class CoreTour implements CommandLineRunner {

  private final StatementPrinter printer;
  private final ObjectProvider<FxRates> fx;
  private final ObjectProvider<LimitsProperties> limits;
  private final TransferService transfers;
  private final Environment env;

  public CoreTour(StatementPrinter printer, ObjectProvider<FxRates> fx, ObjectProvider<LimitsProperties> limits,
      TransferService transfers, Environment env) {
    this.printer = printer;
    this.fx = fx;
    this.limits = limits;
    this.transfers = transfers;
    this.env = env;
  }

  @Override
  public void run(String... args) {
    System.out.println("1 scopes : " + printer.print("ACC-1", List.of("rent 15000", "salary 90000")));
    System.out.println("1 scopes : " + printer.print("ACC-2", List.of("fd interest 1200")));
    FxRates rates = fx.getIfAvailable();
    System.out.println("2 auto   : FxRates = " + (rates == null ? "none" : rates.getClass().getSimpleName()
        + ", USD " + rates.rupeesPer("USD")));
    System.out.println("3 props  : profiles " + List.of(env.getActiveProfiles()) + ", limits " + limits.getIfAvailable());
    try {
      transfers.transfer("ACC-1", "ACC-2", new BigDecimal("75000"));
    } catch (RuntimeException e) {
      System.out.println("4 tx     : " + e.getClass().getSimpleName() + ": " + e.getMessage());
    }
    System.out.println("4 tx     : transfers rows " + transfers.count("transfers") + ", audit rows "
        + transfers.count("audit"));
  }
}
