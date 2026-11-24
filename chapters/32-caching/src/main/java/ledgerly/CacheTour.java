package ledgerly;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Prints timings on startup (mvn spring-boot:run). Finished. */
@Component
public class CacheTour implements CommandLineRunner {

  private final FxRateService fx;
  private final FxUpstream upstream;

  public CacheTour(FxRateService fx, FxUpstream upstream) {
    this.fx = fx;
    this.upstream = upstream;
  }

  @Override
  public void run(String... args) {
    for (String step : new String[] {"USD", "USD", "EUR", "USD"}) {
      long t = System.nanoTime();
      var r = fx.rate(step);
      System.out.printf("rate(%s) = %s  in %3d ms  upstream calls so far: %d%n", step, r, (System.nanoTime() - t) / 1_000_000,
          upstream.calls());
    }
    fx.endOfDay();
    long t = System.nanoTime();
    fx.rate("USD");
    System.out.printf("after endOfDay: rate(USD) in %3d ms  upstream calls: %d%n", (System.nanoTime() - t) / 1_000_000,
        upstream.calls());
  }
}
