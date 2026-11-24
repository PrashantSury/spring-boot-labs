package ledgerly;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Finished EXAMPLE listener: records every TransferCompleted the moment it is published (no transaction phase). */
@Component
public class EventLog {

  private final List<TransferCompleted> events = new CopyOnWriteArrayList<>();

  @EventListener
  public void on(TransferCompleted event) {
    events.add(event);
  }

  public List<TransferCompleted> events() {
    return List.copyOf(events);
  }

  public void clear() {
    events.clear();
  }
}
