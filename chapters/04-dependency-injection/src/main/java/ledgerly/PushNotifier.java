package ledgerly;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** A finished demo. NOT one of your tasks. Order 3 of 3. */
@Component
@Order(3)
public class PushNotifier implements Notifier {

  @Override
  public String send(String message) {
    return "PUSH: " + message;
  }
}
