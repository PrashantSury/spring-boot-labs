package ledgerly;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/** Sends a message on every channel, or on one channel by name. */
// Task 3: inject a List and a Map
//
// Spring can inject ALL beans of a type at once:
//   - List<Notifier>        every Notifier, sorted by @Order
//   - Map<String, Notifier> every Notifier, keyed by bean name
//
// Add a constructor NotificationHub(List<Notifier> all, Map<String, Notifier> byName),
// keep both in final fields, then finish the three methods.
// Adding a fourth Notifier bean later must need NO change here.
@Component
public class NotificationHub {


  /** Every channel's reply, in @Order order: [SMS: hi, EMAIL: hi, PUSH: hi]. */
  public List<String> notifyAll(String message) {
    return List.of(); // TODO task 3
  }

  /** One channel by bean name. Unknown name: IllegalArgumentException("no notifier named fax"). */
  public String notifyVia(String beanName, String message) {
    return null; // TODO task 3
  }

  /** The bean names of all channels, sorted: [emailNotifier, pushNotifier, smsNotifier]. */
  public Set<String> channels() {
    return Set.of(); // TODO task 3
  }
}
