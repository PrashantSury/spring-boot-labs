package ledgerly;


/**
 * Task 2: tell /actuator/health whether the payments gateway answers.
 *   1. Make it a bean (@Component) that implements HealthIndicator.
 *      The bean name paymentsGatewayHealthIndicator becomes the component "paymentsGateway".
 *   2. health(): gateway.ping() true  -> Health.up().withDetail("latencyMs", gateway.latencyMs()).build()
 *                gateway.ping() false -> Health.down().withDetail("reason", "connection refused").build()
 */
public class PaymentsGatewayHealthIndicator {

  private final PaymentsGateway gateway;

  public PaymentsGatewayHealthIndicator(PaymentsGateway gateway) {
    this.gateway = gateway;
  }

}
