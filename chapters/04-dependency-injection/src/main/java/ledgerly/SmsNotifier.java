package ledgerly;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** A finished demo. NOT one of your tasks. Order 1 of 3. */
@Component
@Order(1)
public class SmsNotifier implements Notifier {

  @Override
  public String send(String message) {
    return "SMS: " + message;
  }
}
