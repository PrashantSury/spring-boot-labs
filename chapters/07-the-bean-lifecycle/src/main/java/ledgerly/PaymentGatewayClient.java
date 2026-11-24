package ledgerly;

/**
 * Pretend this class is from the payment vendor's jar: you cannot add
 * annotations to it. Finished; NOT one of your tasks.
 */
public class PaymentGatewayClient {

  private boolean connected;

  public void connect() {
    connected = true;
    LifecycleLog.add("gateway connected");
  }

  public void disconnect() {
    connected = false;
    LifecycleLog.add("gateway disconnected");
  }

  public boolean isConnected() {
    return connected;
  }
}
