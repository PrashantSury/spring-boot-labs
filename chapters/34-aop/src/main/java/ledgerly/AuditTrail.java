package ledgerly;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/** Where audit lines go (a real bank writes them to a table). Finished. */
@Component
public class AuditTrail {

  private final List<String> lines = new CopyOnWriteArrayList<>();

  public void add(String line) {
    lines.add(line);
  }

  public List<String> lines() {
    return List.copyOf(lines);
  }

  public void clear() {
    lines.clear();
  }
}
