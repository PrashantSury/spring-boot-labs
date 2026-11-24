package dev.ledgerly.fx;


/**
 * Task 2: turn this into an auto-configuration, the way Boot's own starters work:
 *   1. Annotate the class with @AutoConfiguration.
 *   2. Make fixedFxRates() a @Bean that backs off when the app defines its own FxRates: @ConditionalOnMissingBean.
 *   3. List the class in src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 * (This package is outside ledgerly, so component scanning never sees it: only the imports file brings it in.)
 */
public class FxAutoConfiguration {

  public FxRates fixedFxRates() {
    return new FixedFxRates();
  }
}
