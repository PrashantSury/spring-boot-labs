package ledgerly;

import org.springframework.stereotype.Component;

/** The card/UPI payments gateway Ledgerly depends on (simulated). Finished. */
@Component
public class PaymentsGateway {

  private volatile boolean reachable = true;

  /** Simulates a network check; true while the gateway answers. */
  public boolean ping() {
    return reachable;
  }

  public long latencyMs() {
    return 42;
  }

  /** Test and demo switch: pretend the gateway went down or came back. */
  public void setReachable(boolean reachable) {
    this.reachable = reachable;
  }
}
