package ledgerly;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** A finished demo. NOT one of your tasks. Remembers one line per transfer. */
@Component
public class AuditTrail {

  private final List<String> lines = new ArrayList<>();

  public void record(String line) {
    lines.add(line);
  }

  public List<String> lines() {
    return List.copyOf(lines);
  }
}
