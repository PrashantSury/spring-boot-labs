package ledgerly;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * A finished demo component. NOT one of your tasks. It is a singleton:
 * the whole application shares this one instance.
 */
@Component
public class AuditLog {

  private final List<String> lines = new ArrayList<>();

  public void add(String line) {
    lines.add(line);
  }

  public int size() {
    return lines.size();
  }
}
