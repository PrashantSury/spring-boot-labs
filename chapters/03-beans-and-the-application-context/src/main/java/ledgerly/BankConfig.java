package ledgerly;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Beans you cannot mark with an annotation, because you do not own the
 * class (Clock is from the JDK), or because you want to build the
 * object yourself. Tasks 2, 3 and 5 are here.
 */
@Configuration
public class BankConfig {

  // Task 2: a Clock bean named bankClock
  //
  // Make this method produce a bean (annotate it) and give it a body:
  // a Clock fixed to the zone Asia/Kolkata. Use Clock.system(ZoneId.of(...)).
  // The bean's name is the method name: bankClock.
  @Bean(name="bankClock")
  Clock bankClock() {
    return Clock.system(ZoneId.of(
            "Asia/Kolkata"
    ));
  }

  // Task 3: two interest policies, with explicit names
  //
  // Make each method a bean, with the name written in the annotation,
  // not taken from the method name:
  //
  //     bean name "savingsPolicy"  ->  new InterestPolicy("savings", 3.5)
  //     bean name "currentPolicy"  ->  new InterestPolicy("current", 0.0)
  //
  // Both beans have the same TYPE, InterestPolicy. That is fine until
  // somebody asks the context for "the" InterestPolicy. See task 5.
  //
  // Task 5: when two beans match, say which one wins
  //
  // After task 3, context.getBean(InterestPolicy.class) fails: Spring
  // finds two candidates and will not guess. Mark the savings policy
  // as the preferred one (the primary). The current policy stays normal.

  @Primary
  @Bean(name="savingsPolicy")
  InterestPolicy savings() {
    return new InterestPolicy("savings", 3.5);
  }
  @Bean(name="currentPolicy")
  InterestPolicy current() {
    return new InterestPolicy("current", 0.0);
  }
}
