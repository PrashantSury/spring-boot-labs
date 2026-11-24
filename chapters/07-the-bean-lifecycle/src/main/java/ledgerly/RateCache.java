package ledgerly;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Keeps today's exchange rates in memory. */
// Task 1: run load() once, AFTER the RateSource has been injected
//
// The RateSource arrives through a SETTER, so it is still null inside the
// constructor. Calling load() from the constructor would throw a
// NullPointerException. Mark load() so Spring calls it once the bean is
// fully injected (jakarta.annotation.PostConstruct).
@Component
public class RateCache {

  private final Map<String, Double> rates = new HashMap<>();
  private RateSource source;
  private int loads;

  @Autowired
  void setSource(RateSource source) {
    this.source = source;
  }

  void load() {
    rates.putAll(source.todaysRates());
    loads++;
    LifecycleLog.add("rates loaded: " + rates.size());
  }

  public Double rate(String currency) {
    return rates.get(currency);
  }

  public int loads() {
    return loads;
  }
}
