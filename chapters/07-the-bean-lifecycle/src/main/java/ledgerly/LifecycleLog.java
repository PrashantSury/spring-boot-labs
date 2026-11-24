package ledgerly;

import java.util.ArrayList;
import java.util.List;

/** A finished helper. NOT one of your tasks. Every lifecycle callback writes one line here. */
public final class LifecycleLog {

  private static final List<String> LINES = new ArrayList<>();

  private LifecycleLog() {}

  public static synchronized void add(String line) {
    LINES.add(line);
  }

  public static synchronized List<String> lines() {
    return List.copyOf(LINES);
  }

  public static synchronized void clear() {
    LINES.clear();
  }
}
