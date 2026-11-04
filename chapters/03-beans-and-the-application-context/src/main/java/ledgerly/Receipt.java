package ledgerly;

import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * A receipt printed for ONE transfer. Every transfer must get its own
 * receipt, with its own id.
 */
// Task 4: one NEW receipt every time
//
// Receipt is already a component, so Spring makes one. But Spring's
// default is ONE shared instance (a singleton), and every transfer
// would print the same receipt id. Change the scope so that asking
// the context for a Receipt gives a new object each time.
@Component
public class Receipt {

  private final String id = UUID.randomUUID().toString().substring(0, 8);

  public String id() {
    return id;
  }
}
