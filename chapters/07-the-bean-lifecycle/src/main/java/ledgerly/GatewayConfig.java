package ledgerly;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Beans for classes you do not own. */
@Configuration
public class GatewayConfig {

  // Task 3: lifecycle methods for a class you cannot annotate
  //
  // PaymentGatewayClient has connect() and disconnect(), but you cannot put
  // @PostConstruct on a vendor class. Tell Spring the method names in the
  // @Bean annotation: connect when the bean is ready, disconnect on shutdown.
  @Bean
  PaymentGatewayClient paymentGateway() {
    return new PaymentGatewayClient();
  }

  // Task 5 (part 2): LifecycleProbe also has customInit() and customDestroy().
  // Name them in this @Bean annotation, the same way as task 3.
  @Bean
  LifecycleProbe lifecycleProbe() {
    return new LifecycleProbe();
  }
}
