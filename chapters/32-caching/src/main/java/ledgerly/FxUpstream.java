package ledgerly;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/** The slow, metered exchange-rate API (each call costs money). Counts calls. Finished. */
@Component
public class FxUpstream {

  private final AtomicInteger calls = new AtomicInteger();
  private final Map<String, BigDecimal> rates = new ConcurrentHashMap<>(Map.of(
      "USD", new BigDecimal("83.12"), "EUR", new BigDecimal("90.45"), "GBP", new BigDecimal("105.30")));

  public BigDecimal fetch(String currency) {
    calls.incrementAndGet();
    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    BigDecimal r = rates.get(currency);
    if (r == null) throw new IllegalArgumentException("unknown currency " + currency);
    return r;
  }

  public int calls() {
    return calls.get();
  }

  public void reset() {
    calls.set(0);
  }
}
