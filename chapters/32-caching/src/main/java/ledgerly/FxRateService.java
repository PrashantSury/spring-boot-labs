package ledgerly;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;

/** Rupee exchange rates. */
@Service
public class FxRateService {

  private final FxUpstream upstream;

  public FxRateService(FxUpstream upstream) {
    this.upstream = upstream;
  }

  // Task 1 (part 2): cache by currency in the cache "fxRates": @Cacheable("fxRates")
  public BigDecimal rate(String currency) {
    return upstream.fetch(currency);
  }

  // Task 3: the treasury desk sets a rate by hand. Put the NEW value in the cache (no upstream call next time):
  //   @CachePut(cacheNames = "fxRates", key = "#currency")   (the return value is what gets cached)
  public BigDecimal override(String currency, BigDecimal rate) {
    return rate;
  }

  // Task 3: end of day: throw ALL cached rates away: @CacheEvict(cacheNames = "fxRates", allEntries = true)
  public void endOfDay() {}

  /** Finished EXAMPLE of the proxy trap: this.rate(...) skips the cache. */
  public BigDecimal usdTwiceInternally() {
    rate("USD");
    return rate("USD");
  }
}
