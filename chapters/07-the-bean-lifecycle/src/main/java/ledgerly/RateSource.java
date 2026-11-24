package ledgerly;

import java.util.Map;
import org.springframework.stereotype.Component;

/** A finished demo. Pretend this calls the treasury system for today's rates. */
@Component
public class RateSource {

  public Map<String, Double> todaysRates() {
    return Map.of("USD", 83.25, "EUR", 90.10, "GBP", 105.40);
  }
}
