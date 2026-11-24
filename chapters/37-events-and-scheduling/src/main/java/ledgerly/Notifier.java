package ledgerly;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/** The SMS gateway: slow (300 ms per message). Records what was sent and on which thread. Finished. */
@Component
public class Notifier {

  public record Sms(String text, String thread) {}

  private final List<Sms> sent = new CopyOnWriteArrayList<>();

  public void send(String text) {
    try {
      Thread.sleep(300);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    sent.add(new Sms(text, Thread.currentThread().getName()));
  }

  public List<Sms> sent() {
    return List.copyOf(sent);
  }

  public void clear() {
    sent.clear();
  }
}
