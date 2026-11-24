package dev.ledgerly.fx;

import java.math.BigDecimal;

/** Rupee exchange rates. Part of a small "fx starter" library that many Ledgerly apps share. Finished. */
public interface FxRates {

  BigDecimal rupeesPer(String currency);
}
