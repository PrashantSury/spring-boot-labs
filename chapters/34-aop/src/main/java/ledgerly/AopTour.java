package ledgerly;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Prints what the aspects saw on startup (mvn spring-boot:run). Finished. */
@Component
public class AopTour implements CommandLineRunner {

  private final TransferService transfers;
  private final AuditTrail trail;
  private final Timings timings;

  public AopTour(TransferService transfers, AuditTrail trail, Timings timings) {
    this.transfers = transfers;
    this.trail = trail;
    this.timings = timings;
  }

  @Override
  public void run(String... args) {
    System.out.println("bean class : " + transfers.getClass().getSimpleName());
    System.out.println("CGLIB proxy: " + AopUtils.isCglibProxy(transfers));
    transfers.transfer("ACC-1", "ACC-2", new BigDecimal("100"));
    try {
      transfers.transfer("ACC-1", "ACC-2", new BigDecimal("5000"));
    } catch (InsufficientFunds e) {
      System.out.println("caller saw : " + e.getMessage());
    }
    Map<String, BigDecimal> lines = new LinkedHashMap<>();
    lines.put("ACC-2", new BigDecimal("10"));
    lines.put("ACC-3", new BigDecimal("20"));
    transfers.payroll("ACC-1", lines);
    transfers.feeFor(new BigDecimal("100"));
    System.out.println("audit trail:");
    trail.lines().forEach(l -> System.out.println("  " + l));
    System.out.println("timings    : " + timings.counts());
  }
}
