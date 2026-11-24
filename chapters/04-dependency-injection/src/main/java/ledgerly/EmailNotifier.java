package ledgerly;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** A finished demo. NOT one of your tasks. Order 2 of 3. */
@Component
@Order(2)
public class EmailNotifier implements Notifier {

  @Override
  public String send(String message) {
    return "EMAIL: " + message;
  }
}
