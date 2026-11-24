package ledgerly;

import io.micrometer.core.instrument.MeterRegistry;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

/** Transfers (the money part is simulated). Task 3 adds the metrics. */
@Service
public class TransferService {

  static final BigDecimal LIMIT = new BigDecimal("50000");

  // Task 3: count transfers with ONE counter name and a tag, so dashboards can add them up or split them:
  //   ok       = Counter.builder("ledgerly.transfers").tag("outcome", "ok").register(registry)
  //   rejected = Counter.builder("ledgerly.transfers").tag("outcome", "rejected").register(registry)
  //   Build both in the constructor, then increment the right one in transfer(...).

  public TransferService(MeterRegistry registry) {
  }

  public String transfer(BigDecimal amount) {
    if (amount.compareTo(LIMIT) > 0) {
      throw new LimitExceeded();
    }
    return "ok";
  }
}
