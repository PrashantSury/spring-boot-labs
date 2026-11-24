package dev.ledgerly.fx;

import java.math.BigDecimal;
import java.util.Map;

/** The library's default: fixed rates, good enough for tests and demos. Finished. */
public class FixedFxRates implements FxRates {

  private static final Map<String, BigDecimal> RATES = Map.of("USD", new BigDecimal("83.12"), "EUR",
      new BigDecimal("90.45"));

  @Override
  public BigDecimal rupeesPer(String currency) {
    return RATES.get(currency);
  }
}
