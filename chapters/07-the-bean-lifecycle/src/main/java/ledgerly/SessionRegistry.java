package ledgerly;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** The customer sessions that are open right now. */
// Task 2: close every open session when the application shuts down
//
// Mark closeAll() so Spring calls it when the context closes
// (jakarta.annotation.PreDestroy). It must run once, before the bean is gone.
@Component
public class SessionRegistry {

  private final List<String> open = new ArrayList<>();

  public void open(String customer) {
    open.add(customer);
  }

  public int openCount() {
    return open.size();
  }

  void closeAll() {
    LifecycleLog.add("sessions closed: " + open.size());
    open.clear();
  }
}
